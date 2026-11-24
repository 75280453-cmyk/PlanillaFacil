package pe.edu.continental.planilla.dominio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculadoraPlanillaTest {
    private final CalculadoraPlanilla calculadora = new CalculadoraPlanilla();

    @Test
    void descuentoAfpIntegraFlujoSobre3000EsS387_60() {
        BigDecimal descuento = calculadora.descuentoAfp(
                new BigDecimal("3000"),
                new BigDecimal("10"),
                new BigDecimal("1.37"),
                new BigDecimal("1.55"));

        assertEquals(new BigDecimal("387.60"), descuento);
    }

    @Test
    void descuentoOnpSobre3000EsS390() {
        BigDecimal descuento = calculadora.descuentoOnp(
                new BigDecimal("3000"),
                new BigDecimal("13"));

        assertEquals(new BigDecimal("390.00"), descuento);
    }

    @Test
    void aporteEssaludSobre3000EsS270() {
        BigDecimal aporte = calculadora.aporteEssalud(
                new BigDecimal("3000"),
                new BigDecimal("9"));

        assertEquals(new BigDecimal("270.00"), aporte);
    }
}
