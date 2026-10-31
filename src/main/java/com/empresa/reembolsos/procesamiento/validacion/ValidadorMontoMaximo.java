package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
public class ValidadorMontoMaximo extends ValidadorSolicitud {

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud,
                                                Supplier<List<SolicitudReembolso>> pendientes) {
        var tope = solicitud.topeAplicable();
        return solicitud.getMontoSolicitado().compareTo(tope) > 0
                ? ResultadoValidacion.rechazar("Monto excede el tope de la categoria "
                        + solicitud.getCategoria() + " (" + tope.toPlainString() + ")")
                : ResultadoValidacion.ok();
    }
}
