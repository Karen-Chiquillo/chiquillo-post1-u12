package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

// Orden por costo: duplicados va al final porque es el unico que consulta la base de datos (README, Decision 1)
@Component
public class CadenaValidadores {

    private final ValidadorSolicitud primero;

    public CadenaValidadores(ValidadorDocumentosSoporte documentos,
                             ValidadorMontoMaximo monto,
                             ValidadorDuplicados duplicados) {
        documentos.siguiente(monto).siguiente(duplicados);
        this.primero = documentos;
    }

    public ResultadoValidacion validar(SolicitudReembolso solicitud,
                                       Supplier<List<SolicitudReembolso>> pendientes) {
        return primero.validar(solicitud, pendientes);
    }
}
