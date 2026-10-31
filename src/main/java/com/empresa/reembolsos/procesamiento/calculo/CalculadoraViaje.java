package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CalculadoraViaje implements CalculadoraReembolso {

    @Override
    public CategoriaGasto getCategoria() {
        return CategoriaGasto.VIAJE;
    }

    @Override
    public BigDecimal calcular(SolicitudReembolso solicitud) {
        // topeAplicable() ya es tope diario x dias del viaje
        return solicitud.getMontoSolicitado().min(solicitud.topeAplicable());
    }
}
