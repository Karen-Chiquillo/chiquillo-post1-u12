package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;

import java.util.List;
import java.util.function.Supplier;

/**
 * Eslabon de la cadena de validacion (Chain of Responsibility). Cada validador revisa una regla
 * y, si la solicitud la cumple, la pasa al siguiente sin saber cuantos eslabones vienen despues.
 */
public abstract class ValidadorSolicitud {

    private ValidadorSolicitud siguiente;

    /** Enlaza el siguiente eslabon y lo devuelve para encadenar las llamadas. */
    public ValidadorSolicitud siguiente(ValidadorSolicitud siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    /**
     * @param pendientesDelEmpleado se recibe como Supplier para que la consulta solo se haga
     *                              si algun eslabon la necesita
     * @return el primer rechazo encontrado, o ok si pasa todos los eslabones
     */
    public ResultadoValidacion validar(SolicitudReembolso solicitud,
                                       Supplier<List<SolicitudReembolso>> pendientesDelEmpleado) {
        ResultadoValidacion propio = validarPropio(solicitud, pendientesDelEmpleado);
        if (!propio.valida() || siguiente == null) {
            return propio;
        }
        return siguiente.validar(solicitud, pendientesDelEmpleado);
    }

    /** Regla propia del eslabon. Solo debe llamar a pendientesDelEmpleado.get() si la necesita. */
    protected abstract ResultadoValidacion validarPropio(SolicitudReembolso solicitud,
                                                         Supplier<List<SolicitudReembolso>> pendientesDelEmpleado);
}
