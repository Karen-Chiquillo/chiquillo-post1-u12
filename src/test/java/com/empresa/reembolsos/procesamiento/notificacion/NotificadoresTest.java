package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(OutputCaptureExtension.class)
class NotificadoresTest {

    private static SolicitudReembolso solicitudEn(EstadoSolicitud estado) {
        var solicitud = new SolicitudReembolso("E-001", CategoriaGasto.REPRESENTACION,
                new BigDecimal("850000"), LocalDate.of(2026, 10, 5), 0, true);
        solicitud.setId(7L);
        solicitud.setMontoAprobado(new BigDecimal("850000"));
        solicitud.setEstado(estado);
        return solicitud;
    }

    @Test
    void notificadorEmpleado_informaElResultadoAlEmpleado(CapturedOutput salida) {
        new NotificadorEmpleado().onCambioEstado(solicitudEn(EstadoSolicitud.RECHAZADA), EstadoSolicitud.PENDIENTE);

        assertTrue(salida.getOut().contains("Notificando a E-001: su solicitud 7 paso de PENDIENTE a RECHAZADA"));
    }

    @Test
    void notificadorContabilidad_conSolicitudAprobada_programaElPago(CapturedOutput salida) {
        new NotificadorContabilidad().onCambioEstado(solicitudEn(EstadoSolicitud.APROBADA), EstadoSolicitud.PENDIENTE);

        assertTrue(salida.getOut().contains("Contabilidad: programar pago de 850000 para E-001 (solicitud 7)"));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoSolicitud.class, names = {"RECHAZADA", "REQUIERE_CORRECCION"})
    void notificadorContabilidad_conEstadoDistintoDeAprobada_noHaceNada(EstadoSolicitud estado, CapturedOutput salida) {
        new NotificadorContabilidad().onCambioEstado(solicitudEn(estado), EstadoSolicitud.PENDIENTE);

        assertFalse(salida.getOut().contains("Contabilidad"));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoSolicitud.class, names = {"APROBADA", "RECHAZADA", "REQUIERE_CORRECCION"})
    void notificadorAuditoria_registraTodoCambioDeEstado(EstadoSolicitud estado, CapturedOutput salida) {
        new NotificadorAuditoria().onCambioEstado(solicitudEn(estado), EstadoSolicitud.PENDIENTE);

        assertTrue(salida.getOut().contains("Auditoria: solicitud 7 cambio de PENDIENTE a " + estado));
    }
}
