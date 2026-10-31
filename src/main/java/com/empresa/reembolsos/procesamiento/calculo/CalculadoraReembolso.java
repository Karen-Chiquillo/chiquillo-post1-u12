package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.math.BigDecimal;

/**
 * Estrategia de calculo del monto aprobado (Strategy). Hay una implementacion por categoria de
 * gasto; agregar una categoria nueva es agregar una clase que implemente esta interfaz.
 */
public interface CalculadoraReembolso {

    /** Categoria que atiende esta estrategia; el selector la usa como clave. */
    CategoriaGasto getCategoria();

    /** Monto que se reembolsa segun la formula de la categoria. */
    BigDecimal calcular(SolicitudReembolso solicitud);
}
