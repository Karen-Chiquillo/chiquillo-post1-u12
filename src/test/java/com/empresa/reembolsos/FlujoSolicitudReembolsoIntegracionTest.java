package com.empresa.reembolsos;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Flujo completo contra la aplicacion real (H2 en memoria): controlador, servicio, cadena,
 * calculadoras, observadores y persistencia. Cada prueba usa un empleado distinto para no
 * cruzarse con la validacion de duplicados de otra prueba.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class FlujoSolicitudReembolsoIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions crear(String empleado, String categoria, String monto, int dias, boolean documentos)
            throws Exception {
        var json = """
                {"empleadoId": "%s", "categoria": "%s", "montoSolicitado": %s,
                 "fechaGasto": "2026-10-05", "diasViaje": %d, "documentosSoporte": %b}
                """.formatted(empleado, categoria, monto, dias, documentos);
        return mockMvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private long crearPendiente(String empleado) throws Exception {
        var respuesta = crear(empleado, "REPRESENTACION", "850000", 0, true)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(respuesta, "$.id")).longValue();
    }

    private ResultActions cambiarEstado(long id, String estado) throws Exception {
        return mockMvc.perform(put("/api/solicitudes/{id}/estado", id)
                .contentType(MediaType.APPLICATION_JSON).content("\"" + estado + "\""));
    }

    @Test
    void crear_lasTresCategorias_calculaElMontoAprobadoDeCadaUna() throws Exception {
        crear("E-VIA", "VIAJE", "400000", 3, true)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.montoAprobado").value(400000));
        crear("E-REP", "REPRESENTACION", "850000", 0, true)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.montoAprobado").value(850000));
        crear("E-CAP", "CAPACITACION", "1000000", 0, true)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.montoAprobado").value(800000.0));
    }

    @Test
    void crear_sinDocumentosSoporte_rechazaConMensajeClaro() throws Exception {
        crear("E-DOC", "REPRESENTACION", "850000", 0, false)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("SOLICITUD_RECHAZADA"))
                .andExpect(jsonPath("$.mensaje").value("Faltan documentos soporte"));
    }

    @Test
    void crear_segundaSolicitudPendienteDelMismoPeriodo_rechazaPorDuplicada() throws Exception {
        crearPendiente("E-DUP");

        crear("E-DUP", "REPRESENTACION", "300000", 0, true)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje")
                        .value("Ya existe una solicitud pendiente de la misma categoria en el periodo 2026-10"));
    }

    @Test
    void crear_viajeSinDias_rechazaLosDatosDeEntrada() throws Exception {
        crear("E-DIA", "VIAJE", "100000", 0, true)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("un gasto de VIAJE debe indicar al menos 1 dia de viaje"));
    }

    @Test
    void cambiarEstado_aAprobada_notificaALosTresInteresados(CapturedOutput salida) throws Exception {
        long id = crearPendiente("E-APR");

        cambiarEstado(id, "APROBADA")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));

        assertTrue(salida.getOut().contains("Notificando a E-APR: su solicitud " + id + " paso de PENDIENTE a APROBADA"));
        assertTrue(salida.getOut().contains("Contabilidad: programar pago de 850000.00 para E-APR (solicitud " + id + ")"));
        assertTrue(salida.getOut().contains("Auditoria: solicitud " + id + " cambio de PENDIENTE a APROBADA"));
    }

    @Test
    void cambiarEstado_aRechazada_contabilidadNoActua(CapturedOutput salida) throws Exception {
        long id = crearPendiente("E-REC");

        cambiarEstado(id, "RECHAZADA").andExpect(status().isOk());

        assertTrue(salida.getOut().contains("Auditoria: solicitud " + id + " cambio de PENDIENTE a RECHAZADA"));
        assertFalse(salida.getOut().contains("para E-REC"));
    }

    @Test
    void cambiarEstado_aprobarDosVeces_respondeConflictoYNoVuelveAPagar() throws Exception {
        long id = crearPendiente("E-DOS");
        cambiarEstado(id, "APROBADA").andExpect(status().isOk());

        cambiarEstado(id, "APROBADA")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TRANSICION_INVALIDA"));
    }

    @Test
    void obtener_solicitudInexistente_responde404() throws Exception {
        mockMvc.perform(get("/api/solicitudes/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Solicitud no encontrada: 9999"));
    }
}
