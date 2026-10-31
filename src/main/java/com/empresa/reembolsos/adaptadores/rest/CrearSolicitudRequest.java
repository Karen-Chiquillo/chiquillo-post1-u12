package com.empresa.reembolsos.adaptadores.rest;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

// No incluye id, estado ni montoAprobado: esos los define el sistema, no el cliente
public record CrearSolicitudRequest(
        @NotBlank(message = "es obligatorio") String empleadoId,
        @NotNull(message = "es obligatoria") CategoriaGasto categoria,
        @NotNull(message = "es obligatorio") @Positive(message = "debe ser mayor que cero") BigDecimal montoSolicitado,
        @NotNull(message = "es obligatoria") @PastOrPresent(message = "no puede ser futura") LocalDate fechaGasto,
        @PositiveOrZero(message = "no puede ser negativo") int diasViaje,
        boolean documentosSoporte) {

    @AssertTrue(message = "un gasto de VIAJE debe indicar al menos 1 dia de viaje")
    public boolean isDiasViajeCoherentes() {
        return categoria != CategoriaGasto.VIAJE || diasViaje >= 1;
    }

    public SolicitudReembolso aDominio() {
        return new SolicitudReembolso(empleadoId, categoria, montoSolicitado, fechaGasto, diasViaje, documentosSoporte);
    }
}
