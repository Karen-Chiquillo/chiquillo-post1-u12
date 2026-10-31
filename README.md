# Post-contenido — Proyecto Integrador: Sistema de Solicitudes de Reembolso de Gastos Corporativos

## Descripción

Repositorio del post-contenido de la Unidad 12 de Patrones de Diseño de Software. Contiene un único proyecto Spring Boot (`reembolsos-integrado`, en la raíz del repositorio) que reemplaza la hoja de cálculo que usa el área de Finanzas para las solicitudes de reembolso de gastos: el empleado registra la categoría, el monto, la fecha del gasto y si adjuntó los documentos soporte; el sistema valida la solicitud, calcula el monto aprobado y avisa a los interesados cada vez que la solicitud cambia de estado.

El enunciado no dice qué patrones usar, así que la primera sección de este README es el análisis de los requisitos, escrito y versionado antes de implementar cualquier patrón.

---

## Diseño Propuesto

Para cada requisito se identificó qué es lo que cambia (el punto de variabilidad), qué patrón lo resuelve y una sola oración que explica por qué ese patrón y no otro, siguiendo el test de explicabilidad de la Guía (sección 1.2).

| Requisito | Qué varía | Patrón | Justificación (una oración) |
|-----------|-----------|--------|-----------------------------|
| RF1 — Validación en varias etapas | La lista de verificaciones: hoy son tres (tope, documentos, duplicados) y Finanzas ya anunció una cuarta (límite anual acumulado). La primera que falla debe detener el proceso. | Chain of Responsibility | Cada verificación es un eslabón que aprueba o rechaza sin saber cuántos eslabones vienen después, así que el límite anual se agrega como un eslabón nuevo sin abrir los tres que ya existen. |
| RF2 — Cálculo por categoría | La fórmula del monto aprobado: tope diario × días, monto completo hasta un tope mensual, 80 % hasta un tope anual, y en el futuro "bienestar". | Strategy | Las tres fórmulas se excluyen entre sí y por cada solicitud se aplica exactamente una según la categoría, que es el caso de Strategy: una clase por fórmula, y una categoría nueva es una clase nueva. |
| RF3 — Notificación a varios interesados | Quiénes se enteran de un cambio de estado y qué hace cada uno: el empleado recibe el resultado, Contabilidad solo reacciona a APROBADA, Auditoría registra todo, y viene un panel de métricas. | Observer | El servicio solo anuncia "esta solicitud pasó de un estado a otro" y cada suscriptor decide qué hacer con eso, así que el panel de métricas se suma como un suscriptor más sin tocar el servicio ni a los otros tres. |

### Por qué no otro patrón

- **RF1:** Strategy no sirve porque aquí no se elige una verificación, se aplican todas en orden. Una `List<Predicate<SolicitudReembolso>>` evaluaría todas aunque la primera ya haya fallado y perdería el mensaje de rechazo propio de cada verificación. Un bloque de `if` dentro del servicio obligaría a modificar el servicio con cada verificación nueva, que es justo lo que RF1 pide evitar.
- **RF2:** un `switch` sobre `CategoriaGasto` obliga a editar el mismo método que ya contiene las otras fórmulas cada vez que llega una categoría. Chain of Responsibility no aplica porque no hay secuencia: solo una fórmula corresponde a cada solicitud. Template Method tampoco, porque las fórmulas no comparten un esqueleto de pasos (una multiplica por días, otra aplica un porcentaje).
- **RF3:** que el servicio llame directamente a los tres notificadores lo acopla a cada interesado y obliga a modificarlo para agregar el panel de métricas. Se usa una interfaz propia (`ObservadorCambioEstado`) y no el bus de eventos de Spring; esa comparación queda documentada en "Qué se descartó y por qué".

### Cómo se combinan los tres patrones

Los tres resuelven preguntas distintas y no se pisan: la cadena decide **si** la solicitud se acepta, la estrategia decide **cuánto** se aprueba y los observadores deciden **a quién** se avisa cuando cambia el estado. `SolicitudReembolsoService` es el único punto que los coordina: la cadena produce un resultado de validación que el servicio consume, y la estrategia solo recibe solicitudes que ya pasaron la cadena (regla de la interfaz compartida, Guía 1.2). Para elegir la calculadora no se escribe una Factory manual: Spring ya inyecta todas las implementaciones como lista, y la Guía (1.2) advierte que una Factory sobre la inyección de dependencias suele ser sobreingeniería.

### Lo que no varía y por eso no lleva patrón

- **La persistencia:** hay un solo motor (H2 con JPA) y ningún plan de cambiarlo, así que no se diseña un puerto de persistencia desde ahora (YAGNI). La decisión formal queda para el ADR de la Parte 2.
- **Los estados:** son cuatro y fijos (`PENDIENTE`, `APROBADA`, `RECHAZADA`, `REQUIERE_CORRECCION`), por lo que basta un `enum`.

### Un dato que comparten RF1 y RF2: el tope de cada categoría

RF1 rechaza una solicitud cuyo monto supere "el tope de la categoría" y RF2 usa esos mismos topes para calcular el monto aprobado. Si el validador y cada calculadora guardan su propia copia de los números, basta con cambiar uno para que queden desincronizados. Además, en viajes el tope depende de los días, así que el validador también tiene que usar tope diario × días: si usara un tope fijo de 150.000, un viaje de 3 días por 400.000 se rechazaría aunque la fórmula de RF2 lo aprueba completo (3 × 150.000 = 450.000). Por eso el tope vive en un solo lugar, el `enum` `CategoriaGasto` del dominio, y tanto la validación como el cálculo lo leen de ahí.

### Modelo de dominio

`SolicitudReembolso`, `CategoriaGasto` y `EstadoSolicitud` están en `dominio/` y no importan ninguna clase de Spring ni de JPA. El mapeo de la solicitud a una tabla se hará fuera del dominio, en `infraestructura/`, cuando se implemente la persistencia.

---

## Decisiones de diseño

### Decisión de diseño 1 — Orden de la cadena de validadores

**Decisión:** `CadenaValidadores` enlaza los eslabones en el orden documentos → monto → duplicados, dejando al final el más caro. **Alternativa descartada:** poner `ValidadorDuplicados` primero.

**Criterio:** ordenar por costo de ejecución, de menor a mayor. `ValidadorDocumentosSoporte` solo lee un `boolean` de la solicitud; `ValidadorMontoMaximo` compara el monto contra el tope del dominio (en viajes, una multiplicación); `ValidadorDuplicados` necesita las solicitudes pendientes del empleado, es decir, una consulta a la base de datos.

| Orden | Qué se gana | Qué se pierde |
|-------|-------------|---------------|
| **Duplicados al final (elegido)** | Una solicitud sin documentos o con monto excesivo se rechaza sin consultar la base de datos. | Si una solicitud tiene varios problemas, el empleado se entera de uno a la vez: corrige los documentos, la reenvía y recién ahí descubre que estaba duplicada. |
| Duplicados primero | El empleado sabe de entrada que la solicitud sobra, antes de buscar documentos que no le van a servir. | Toda solicitud paga la consulta, incluso las que se iban a rechazar por algo que se sabía sin consultar nada. |

**Justificación:** (1) los dos primeros eslabones son verificaciones en memoria y descartan solicitudes sin costo, así que tiene sentido que filtren antes del eslabón que va a la base de datos; (2) el caso "duplicada y además mal diligenciada" es el menos frecuente, porque el empleado normalmente sabe si ya envió algo ese mes, mientras que olvidar un documento o pasarse del tope son errores comunes; (3) el orden es fácil de revertir: está en tres líneas del constructor de `CadenaValidadores` y ningún validador depende de su posición.

**Ajuste propio respecto a la guía:** en el código de referencia el servicio carga las pendientes **antes** de entrar a la cadena (`findByEmpleadoIdAndEstado` y luego `validadores.validar(...)`), así que la consulta se hace siempre y el orden de los eslabones no ahorra nada. Para que el criterio de eficiencia tenga efecto real, `ValidadorSolicitud.validar` recibe un `Supplier<List<SolicitudReembolso>>` en vez de la lista ya cargada, y solo `ValidadorDuplicados` llama a `get()`. `CadenaValidadoresTest` lo comprueba contando las consultas: 0 cuando falla documentos, 0 cuando falla monto y 1 cuando la solicitud llega al último eslabón.
