package com.empresa.reembolsos.adaptadores.rest;

import com.empresa.reembolsos.aplicacion.SolicitudReembolsoService;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudReembolsoController {

    private final SolicitudReembolsoService servicio;

    public SolicitudReembolsoController(SolicitudReembolsoService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(@RequestBody @Valid CrearSolicitudRequest request) {
        var creada = servicio.crear(request.aDominio());
        return ResponseEntity.created(URI.create("/api/solicitudes/" + creada.getId()))
                .body(SolicitudResponse.desde(creada));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(SolicitudResponse.desde(servicio.obtener(id)));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<SolicitudResponse> cambiarEstado(@PathVariable Long id, @RequestBody EstadoSolicitud nuevoEstado) {
        return ResponseEntity.ok(SolicitudResponse.desde(servicio.cambiarEstado(id, nuevoEstado)));
    }
}
