package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraRepresentacionTest {

    private final CalculadoraRepresentacion calculadora = new CalculadoraRepresentacion();

    private static SolicitudReembolso representacion(String monto) {
        return new SolicitudReembolso("E-001", CategoriaGasto.REPRESENTACION, new BigDecimal(monto),
                LocalDate.of(2026, 10, 5), 0, true);
    }

    @Test
    void calcular_montoMenorAlTopeMensual_apruebaElMontoCompleto() {
        assertEquals(new BigDecimal("850000"), calculadora.calcular(representacion("850000")));
    }

    @Test
    void calcular_montoMayorAlTopeMensual_apruebaSoloElTope() {
        assertEquals(new BigDecimal("2000000"), calculadora.calcular(representacion("2500000")));
    }
}
