package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SelectorCalculadoraTest {

    @Test
    void obtener_devuelveLaCalculadoraDeCadaCategoria() {
        var selector = new SelectorCalculadora(List.of(
                new CalculadoraViaje(), new CalculadoraRepresentacion(), new CalculadoraCapacitacion()));

        assertInstanceOf(CalculadoraViaje.class, selector.obtener(CategoriaGasto.VIAJE));
        assertInstanceOf(CalculadoraRepresentacion.class, selector.obtener(CategoriaGasto.REPRESENTACION));
        assertInstanceOf(CalculadoraCapacitacion.class, selector.obtener(CategoriaGasto.CAPACITACION));
    }

    @Test
    void constructor_categoriaSinCalculadora_fallaAlArrancar() {
        var lista = List.<CalculadoraReembolso>of(new CalculadoraViaje(), new CalculadoraRepresentacion());

        var error = assertThrows(IllegalStateException.class, () -> new SelectorCalculadora(lista));
        assertEquals("Categorias sin calculadora: [CAPACITACION]", error.getMessage());
    }

    @Test
    void constructor_dosCalculadorasParaLaMismaCategoria_fallaAlArrancar() {
        var lista = List.<CalculadoraReembolso>of(new CalculadoraViaje(), new CalculadoraViaje(),
                new CalculadoraRepresentacion(), new CalculadoraCapacitacion());

        assertThrows(IllegalStateException.class, () -> new SelectorCalculadora(lista));
    }
}
