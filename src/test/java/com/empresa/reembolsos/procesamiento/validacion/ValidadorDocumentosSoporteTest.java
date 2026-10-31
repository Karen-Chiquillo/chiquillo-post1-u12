package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorDocumentosSoporteTest {

    private final ValidadorDocumentosSoporte validador = new ValidadorDocumentosSoporte();

    @Test
    void validar_sinDocumentosSoporte_rechazaConMotivo() {
        var solicitud = new SolicitudReembolso("E-001", CategoriaGasto.REPRESENTACION,
                new BigDecimal("100000"), LocalDate.of(2026, 10, 5), 0, false);

        var resultado = validador.validar(solicitud, List::of);

        assertFalse(resultado.valida());
        assertEquals("Faltan documentos soporte", resultado.motivoRechazo());
    }

    @Test
    void validar_conDocumentosSoporte_esValida() {
        var solicitud = new SolicitudReembolso("E-001", CategoriaGasto.REPRESENTACION,
                new BigDecimal("100000"), LocalDate.of(2026, 10, 5), 0, true);

        assertTrue(validador.validar(solicitud, List::of).valida());
    }
}
