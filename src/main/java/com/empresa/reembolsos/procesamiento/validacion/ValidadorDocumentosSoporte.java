package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
public class ValidadorDocumentosSoporte extends ValidadorSolicitud {

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud,
                                                Supplier<List<SolicitudReembolso>> pendientes) {
        return solicitud.isDocumentosSoporte()
                ? ResultadoValidacion.ok()
                : ResultadoValidacion.rechazar("Faltan documentos soporte");
    }
}
