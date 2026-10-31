package com.empresa.reembolsos.dominio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SolicitudReembolsoTest {

    private static SolicitudReembolso pendiente() {
        var solicitud = new SolicitudReembolso("E-001", CategoriaGasto.REPRESENTACION,
                new BigDecimal("100000"), LocalDate.of(2026, 10, 5), 0, true);
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        return solicitud;
    }

    @ParameterizedTest
    @EnumSource(value = EstadoSolicitud.class, names = {"APROBADA", "RECHAZADA", "REQUIERE_CORRECCION"})
    void cambiarEstado_desdePendiente_aceptaLosTresResultados(EstadoSolicitud nuevo) {
        var solicitud = pendiente();

        solicitud.cambiarEstado(nuevo);

        assertEquals(nuevo, solicitud.getEstado());
    }

    @Test
    void cambiarEstado_solicitudYaAprobada_noSePuedeAprobarOtraVez() {
        var solicitud = pendiente();
        solicitud.cambiarEstado(EstadoSolicitud.APROBADA);

        assertThrows(TransicionEstadoInvalidaException.class,
                () -> solicitud.cambiarEstado(EstadoSolicitud.APROBADA));
    }

    @Test
    void cambiarEstado_dePendienteAPendiente_esInvalido() {
        assertThrows(TransicionEstadoInvalidaException.class,
                () -> pendiente().cambiarEstado(EstadoSolicitud.PENDIENTE));
    }

    @Test
    void topeAplicable_enViaje_esTopeDiarioPorDias() {
        var viaje = new SolicitudReembolso("E-001", CategoriaGasto.VIAJE,
                new BigDecimal("100000"), LocalDate.of(2026, 10, 5), 4, true);

        assertEquals(new BigDecimal("600000"), viaje.topeAplicable());
    }
}
