package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraViajeTest {

    private final CalculadoraViaje calculadora = new CalculadoraViaje();

    private static SolicitudReembolso viaje(String monto, int dias) {
        return new SolicitudReembolso("E-001", CategoriaGasto.VIAJE, new BigDecimal(monto),
                LocalDate.of(2026, 10, 5), dias, true);
    }

    @Test
    void calcular_montoDentroDelTopeDiarioPorDias_apruebaElMontoCompleto() {
        assertEquals(new BigDecimal("400000"), calculadora.calcular(viaje("400000", 3)));
    }

    @Test
    void calcular_montoMayorAlTopeDiarioPorDias_apruebaSoloElTope() {
        // 2 dias x 150.000 = 300.000
        assertEquals(new BigDecimal("300000"), calculadora.calcular(viaje("400000", 2)));
    }
}
