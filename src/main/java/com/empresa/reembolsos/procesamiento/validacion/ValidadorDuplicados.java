package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.infraestructura.RepositorioSolicitudesJpa;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;
import java.util.function.Supplier;

// El periodo es mes y anio: comparar solo el mes cruzaria solicitudes de anios distintos
@Component
public class ValidadorDuplicados extends ValidadorSolicitud {

    // Violacion intencional de la Regla 1: procesamiento depende de infraestructura
    private RepositorioSolicitudesJpa repositorio;

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud,
                                                Supplier<List<SolicitudReembolso>> pendientes) {
        var periodo = YearMonth.from(solicitud.getFechaGasto());
        boolean existeDuplicado = pendientes.get().stream()
                .anyMatch(p -> p.getCategoria() == solicitud.getCategoria()
                        && YearMonth.from(p.getFechaGasto()).equals(periodo));
        return existeDuplicado
                ? ResultadoValidacion.rechazar("Ya existe una solicitud pendiente de la misma categoria en el periodo "
                        + periodo)
                : ResultadoValidacion.ok();
    }
}
