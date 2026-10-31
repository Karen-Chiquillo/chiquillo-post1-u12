package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorMontoMaximoTest {

    private final ValidadorMontoMaximo validador = new ValidadorMontoMaximo();

    private static SolicitudReembolso solicitud(CategoriaGasto categoria, String monto, int dias) {
        return new SolicitudReembolso("E-001", categoria, new BigDecimal(monto),
                LocalDate.of(2026, 10, 5), dias, true);
    }

    @Test
    void validar_viajeDeTresDiasPor400Mil_esValidaPorqueElTopeEsDiarioPorDias() {
        // Con un tope fijo de 150.000 este viaje se rechazaria; el tope real es 3 x 150.000 = 450.000
        assertTrue(validador.validar(solicitud(CategoriaGasto.VIAJE, "400000", 3), List::of).valida());
    }

    @Test
    void validar_viajeQueSuperaTopeDiarioPorDias_rechazaConElTopeEnElMotivo() {
        var resultado = validador.validar(solicitud(CategoriaGasto.VIAJE, "450001", 3), List::of);

        assertFalse(resultado.valida());
        assertEquals("Monto excede el tope de la categoria VIAJE (450000)", resultado.motivoRechazo());
    }

    @ParameterizedTest
    @CsvSource({
            "REPRESENTACION, 2000000, true",
            "REPRESENTACION, 2000001, false",
            "CAPACITACION,   3000000, true",
            "CAPACITACION,   3000001, false"
    })
    void validar_categoriasConTopeFijo_respetanElLimite(CategoriaGasto categoria, String monto, boolean esperado) {
        assertEquals(esperado, validador.validar(solicitud(categoria, monto, 0), List::of).valida());
    }
}
