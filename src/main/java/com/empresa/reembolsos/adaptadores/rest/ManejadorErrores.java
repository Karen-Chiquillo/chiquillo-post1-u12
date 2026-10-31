package com.empresa.reembolsos.adaptadores.rest;

import com.empresa.reembolsos.aplicacion.SolicitudInvalidaException;
import com.empresa.reembolsos.aplicacion.SolicitudNoEncontradaException;
import com.empresa.reembolsos.dominio.TransicionEstadoInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ManejadorErrores {

    // La solicitud llego bien formada pero no paso la cadena de validacion (RF1)
    @ExceptionHandler(SolicitudInvalidaException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse solicitudRechazada(SolicitudInvalidaException ex) {
        return new ErrorResponse("SOLICITUD_RECHAZADA", ex.getMessage());
    }

    @ExceptionHandler(SolicitudNoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse noEncontrada(SolicitudNoEncontradaException ex) {
        return new ErrorResponse("NO_ENCONTRADA", ex.getMessage());
    }

    @ExceptionHandler(TransicionEstadoInvalidaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse transicionInvalida(TransicionEstadoInvalidaException ex) {
        return new ErrorResponse("TRANSICION_INVALIDA", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse datosInvalidos(MethodArgumentNotValidException ex) {
        var detalle = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return new ErrorResponse("DATOS_INVALIDOS", detalle);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse cuerpoIlegible(HttpMessageNotReadableException ex) {
        return new ErrorResponse("CUERPO_INVALIDO",
                "El cuerpo de la peticion no es un JSON valido o tiene un valor no permitido (categoria o estado)");
    }
}
