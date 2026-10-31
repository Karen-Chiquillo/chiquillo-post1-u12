package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraCapacitacionTest {

    private final CalculadoraCapacitacion calculadora = new CalculadoraCapacitacion();

    private static SolicitudReembolso capacitacion(String monto) {
        return new SolicitudReembolso("E-001", CategoriaGasto.CAPACITACION, new BigDecimal(monto),
                LocalDate.of(2026, 10, 5), 0, true);
    }

    @Test
    void calcular_reembolsaElOchentaPorCiento() {
        // compareTo: 1.000.000 x 0.8 da 800000.0, que es el mismo valor que 800000
        assertEquals(0, new BigDecimal("800000").compareTo(calculadora.calcular(capacitacion("1000000"))));
    }

    @Test
    void calcular_ochentaPorCientoMayorAlTopeAnual_apruebaSoloElTope() {
        // 5.000.000 x 0.8 = 4.000.000, que supera el tope anual de 3.000.000
        assertEquals(0, new BigDecimal("3000000").compareTo(calculadora.calcular(capacitacion("5000000"))));
    }
}
