package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Component
public class SelectorCalculadora {

    private final Map<CategoriaGasto, CalculadoraReembolso> calculadoras = new EnumMap<>(CategoriaGasto.class);

    public SelectorCalculadora(List<CalculadoraReembolso> lista) {
        for (var calculadora : lista) {
            var anterior = calculadoras.put(calculadora.getCategoria(), calculadora);
            if (anterior != null) {
                throw new IllegalStateException("Dos calculadoras para la categoria " + calculadora.getCategoria());
            }
        }
        // Una categoria sin calculadora se detecta al arrancar la aplicacion, no con la primera solicitud
        var sinCalculadora = EnumSet.allOf(CategoriaGasto.class);
        sinCalculadora.removeAll(calculadoras.keySet());
        if (!sinCalculadora.isEmpty()) {
            throw new IllegalStateException("Categorias sin calculadora: " + sinCalculadora);
        }
    }

    public CalculadoraReembolso obtener(CategoriaGasto categoria) {
        return calculadoras.get(categoria);
    }
}
