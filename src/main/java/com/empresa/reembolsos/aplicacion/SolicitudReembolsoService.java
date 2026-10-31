package com.empresa.reembolsos.aplicacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.infraestructura.RepositorioSolicitudesJpa;
import com.empresa.reembolsos.procesamiento.calculo.SelectorCalculadora;
import com.empresa.reembolsos.procesamiento.notificacion.ObservadorCambioEstado;
import com.empresa.reembolsos.procesamiento.validacion.CadenaValidadores;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SolicitudReembolsoService {

    private final CadenaValidadores validadores;
    private final SelectorCalculadora calculadoras;
    private final List<ObservadorCambioEstado> observadores;
    private final RepositorioSolicitudesJpa repositorio;

    public SolicitudReembolsoService(CadenaValidadores validadores, SelectorCalculadora calculadoras,
                                     List<ObservadorCambioEstado> observadores, RepositorioSolicitudesJpa repositorio) {
        this.validadores = validadores;
        this.calculadoras = calculadoras;
        this.observadores = observadores;
        this.repositorio = repositorio;
    }

    public SolicitudReembolso crear(SolicitudReembolso solicitud) {
        // La consulta solo se ejecuta si la solicitud llega a ValidadorDuplicados (README, Decision 1)
        var resultado = validadores.validar(solicitud,
                () -> repositorio.findByEmpleadoIdAndEstado(solicitud.getEmpleadoId(), EstadoSolicitud.PENDIENTE));
        if (!resultado.valida()) {
            throw new SolicitudInvalidaException(resultado.motivoRechazo());
        }
        solicitud.setMontoAprobado(calculadoras.obtener(solicitud.getCategoria()).calcular(solicitud));
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        return repositorio.save(solicitud);
    }

    public SolicitudReembolso cambiarEstado(Long id, EstadoSolicitud nuevoEstado) {
        var solicitud = obtener(id);
        var estadoAnterior = solicitud.getEstado();
        solicitud.cambiarEstado(nuevoEstado);
        var guardada = repositorio.save(solicitud);
        observadores.forEach(o -> o.onCambioEstado(guardada, estadoAnterior));
        return guardada;
    }

    @Transactional(readOnly = true)
    public SolicitudReembolso obtener(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new SolicitudNoEncontradaException(id));
    }
}
