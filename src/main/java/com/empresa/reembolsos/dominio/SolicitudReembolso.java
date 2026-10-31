package com.empresa.reembolsos.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Solicitud de reembolso de un empleado. Es Java puro: no importa Spring ni JPA.
 */
public class SolicitudReembolso {

    private Long id;
    private String empleadoId;
    private CategoriaGasto categoria;
    private BigDecimal montoSolicitado;
    private BigDecimal montoAprobado;
    private LocalDate fechaGasto;
    private int diasViaje;
    private boolean documentosSoporte;
    private EstadoSolicitud estado;

    public SolicitudReembolso() {
    }

    public SolicitudReembolso(String empleadoId, CategoriaGasto categoria, BigDecimal montoSolicitado,
                              LocalDate fechaGasto, int diasViaje, boolean documentosSoporte) {
        this.empleadoId = empleadoId;
        this.categoria = categoria;
        this.montoSolicitado = montoSolicitado;
        this.fechaGasto = fechaGasto;
        this.diasViaje = diasViaje;
        this.documentosSoporte = documentosSoporte;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmpleadoId() {
        return empleadoId;
    }

    public void setEmpleadoId(String empleadoId) {
        this.empleadoId = empleadoId;
    }

    public CategoriaGasto getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaGasto categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getMontoSolicitado() {
        return montoSolicitado;
    }

    public void setMontoSolicitado(BigDecimal montoSolicitado) {
        this.montoSolicitado = montoSolicitado;
    }

    public BigDecimal getMontoAprobado() {
        return montoAprobado;
    }

    public void setMontoAprobado(BigDecimal montoAprobado) {
        this.montoAprobado = montoAprobado;
    }

    public LocalDate getFechaGasto() {
        return fechaGasto;
    }

    public void setFechaGasto(LocalDate fechaGasto) {
        this.fechaGasto = fechaGasto;
    }

    public int getDiasViaje() {
        return diasViaje;
    }

    public void setDiasViaje(int diasViaje) {
        this.diasViaje = diasViaje;
    }

    public boolean isDocumentosSoporte() {
        return documentosSoporte;
    }

    public void setDocumentosSoporte(boolean documentosSoporte) {
        this.documentosSoporte = documentosSoporte;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    /** Tope de la categoria aplicado a esta solicitud (en viajes depende de los dias). */
    public BigDecimal topeAplicable() {
        return categoria.topePara(diasViaje);
    }

    /**
     * Resuelve una solicitud pendiente. Una solicitud ya resuelta no se puede volver a cambiar:
     * aprobarla dos veces haria que Contabilidad programara el pago dos veces.
     */
    public void cambiarEstado(EstadoSolicitud nuevoEstado) {
        if (estado != EstadoSolicitud.PENDIENTE || nuevoEstado == EstadoSolicitud.PENDIENTE) {
            throw new TransicionEstadoInvalidaException(estado, nuevoEstado);
        }
        this.estado = nuevoEstado;
    }
}
