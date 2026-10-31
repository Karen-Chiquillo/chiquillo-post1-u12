package com.empresa.reembolsos.dominio;

public class TransicionEstadoInvalidaException extends RuntimeException {

    public TransicionEstadoInvalidaException(EstadoSolicitud actual, EstadoSolicitud nuevo) {
        super("No se puede pasar una solicitud de " + actual + " a " + nuevo
                + ": solo una solicitud PENDIENTE puede aprobarse, rechazarse o devolverse para correccion");
    }
}
