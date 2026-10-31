package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CadenaValidadoresTest {

    private final CadenaValidadores cadena = new CadenaValidadores(
            new ValidadorDocumentosSoporte(), new ValidadorMontoMaximo(), new ValidadorDuplicados());

    private final AtomicInteger consultasPendientes = new AtomicInteger();

    private final Supplier<List<SolicitudReembolso>> pendientesContados = () -> {
        consultasPendientes.incrementAndGet();
        return List.of();
    };

    private static SolicitudReembolso solicitud(String monto, boolean documentos) {
        return new SolicitudReembolso("E-001", CategoriaGasto.REPRESENTACION, new BigDecimal(monto),
                LocalDate.of(2026, 10, 5), 0, documentos);
    }

    @Test
    void validar_sinDocumentos_cortaEnElPrimerEslabonSinConsultarPendientes() {
        var resultado = cadena.validar(solicitud("100000", false), pendientesContados);

        assertFalse(resultado.valida());
        assertEquals("Faltan documentos soporte", resultado.motivoRechazo());
        assertEquals(0, consultasPendientes.get());
    }

    @Test
    void validar_montoExcesivo_cortaEnElSegundoEslabonSinConsultarPendientes() {
        var resultado = cadena.validar(solicitud("5000000", true), pendientesContados);

        assertFalse(resultado.valida());
        assertEquals(0, consultasPendientes.get());
    }

    @Test
    void validar_solicitudCorrecta_recorreLosTresEslabonesYConsultaPendientesUnaVez() {
        var resultado = cadena.validar(solicitud("100000", true), pendientesContados);

        assertTrue(resultado.valida());
        assertEquals(1, consultasPendientes.get());
    }
}
