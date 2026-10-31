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

class ValidadorDuplicadosTest {

    private final ValidadorDuplicados validador = new ValidadorDuplicados();

    private static SolicitudReembolso solicitud(CategoriaGasto categoria, LocalDate fecha) {
        return new SolicitudReembolso("E-001", categoria, new BigDecimal("100000"), fecha, 1, true);
    }

    @Test
    void validar_pendienteMismaCategoriaMismoMesYAnio_rechaza() {
        var pendiente = solicitud(CategoriaGasto.REPRESENTACION, LocalDate.of(2026, 10, 2));
        var nueva = solicitud(CategoriaGasto.REPRESENTACION, LocalDate.of(2026, 10, 28));

        var resultado = validador.validar(nueva, () -> List.of(pendiente));

        assertFalse(resultado.valida());
        assertEquals("Ya existe una solicitud pendiente de la misma categoria en el periodo 2026-10",
                resultado.motivoRechazo());
    }

    @Test
    void validar_pendienteMismoMesDeOtroAnio_esValida() {
        // Comparando solo getMonth(), octubre de 2025 bloquearia octubre de 2026
        var pendiente = solicitud(CategoriaGasto.REPRESENTACION, LocalDate.of(2025, 10, 2));
        var nueva = solicitud(CategoriaGasto.REPRESENTACION, LocalDate.of(2026, 10, 28));

        assertTrue(validador.validar(nueva, () -> List.of(pendiente)).valida());
    }

    @Test
    void validar_pendienteDeOtraCategoriaEnElMismoPeriodo_esValida() {
        var pendiente = solicitud(CategoriaGasto.CAPACITACION, LocalDate.of(2026, 10, 2));
        var nueva = solicitud(CategoriaGasto.REPRESENTACION, LocalDate.of(2026, 10, 28));

        assertTrue(validador.validar(nueva, () -> List.of(pendiente)).valida());
    }
}
