package com.empresa.reembolsos.dominio;

import java.math.BigDecimal;

/**
 * Categorias de gasto reembolsable. Cada categoria guarda su tope, que es la unica fuente
 * de ese valor: lo usan tanto la validacion de monto maximo como el calculo del reembolso.
 */
public enum CategoriaGasto {

    /** Viaticos: el tope es diario y se multiplica por los dias del viaje. */
    VIAJE(new BigDecimal("150000")) {
        @Override
        public BigDecimal topePara(int diasViaje) {
            return getTope().multiply(BigDecimal.valueOf(diasViaje));
        }
    },

    /** Gastos de representacion: tope mensual. */
    REPRESENTACION(new BigDecimal("2000000")),

    /** Capacitacion: tope anual. */
    CAPACITACION(new BigDecimal("3000000"));

    private final BigDecimal tope;

    CategoriaGasto(BigDecimal tope) {
        this.tope = tope;
    }

    public BigDecimal getTope() {
        return tope;
    }

    /**
     * Tope que aplica a una solicitud concreta. Solo VIAJE depende de los dias; las demas
     * categorias devuelven su tope fijo.
     */
    public BigDecimal topePara(int diasViaje) {
        return tope;
    }
}
