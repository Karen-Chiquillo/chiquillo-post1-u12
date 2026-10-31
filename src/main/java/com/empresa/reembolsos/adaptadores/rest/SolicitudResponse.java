package com.empresa.reembolsos.adaptadores.rest;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SolicitudResponse(
        Long id,
        String empleadoId,
        CategoriaGasto categoria,
        BigDecimal montoSolicitado,
        BigDecimal montoAprobado,
        LocalDate fechaGasto,
        int diasViaje,
        boolean documentosSoporte,
        EstadoSolicitud estado) {

    public static SolicitudResponse desde(SolicitudReembolso s) {
        return new SolicitudResponse(s.getId(), s.getEmpleadoId(), s.getCategoria(), s.getMontoSolicitado(),
                s.getMontoAprobado(), s.getFechaGasto(), s.getDiasViaje(), s.isDocumentosSoporte(), s.getEstado());
    }
}
