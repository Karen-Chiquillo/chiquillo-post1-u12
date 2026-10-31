package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

/**
 * Interesado en los cambios de estado de una solicitud (Observer). El servicio avisa a todos los
 * observadores registrados y cada uno decide si el cambio le interesa y como reaccionar.
 */
public interface ObservadorCambioEstado {

    /**
     * @param solicitud       solicitud ya guardada con su nuevo estado
     * @param estadoAnterior  estado que tenia antes del cambio
     */
    void onCambioEstado(SolicitudReembolso solicitud, EstadoSolicitud estadoAnterior);
}
