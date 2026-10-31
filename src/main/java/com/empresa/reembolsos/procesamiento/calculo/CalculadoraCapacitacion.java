package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CalculadoraCapacitacion implements CalculadoraReembolso {

    private static final BigDecimal PORCENTAJE_REEMBOLSO = new BigDecimal("0.8");

    @Override
    public CategoriaGasto getCategoria() {
        return CategoriaGasto.CAPACITACION;
    }

    @Override
    public BigDecimal calcular(SolicitudReembolso solicitud) {
        var reembolsoParcial = solicitud.getMontoSolicitado().multiply(PORCENTAJE_REEMBOLSO);
        return reembolsoParcial.min(solicitud.topeAplicable());
    }
}
