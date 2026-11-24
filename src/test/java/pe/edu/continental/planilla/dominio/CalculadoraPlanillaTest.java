package pe.edu.continental.planilla.dominio;

import org.junit.jupiter.api.Test;
import pe.edu.continental.planilla.dominio.excepcion.ReglaPlanillaException;
import pe.edu.continental.planilla.dominio.modelo.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    private Trabajador trabajadorAfp() {
        return new Trabajador(1L, "12345678", "Joe", "Prueba", "Analista",
                LocalDate.of(2024, 1, 1), new BigDecimal("3000"),
                new SistemaPension(1L, "AFP Integra", TipoSistemaPension.AFP),
                TipoComision.FLUJO, EstadoTrabajador.ACTIVO);
    }

    private Trabajador trabajadorOnp() {
        return new Trabajador(2L, "87654321", "Ana", "Prueba", "Asistente",
                LocalDate.of(2024, 1, 1), new BigDecimal("3000"),
                new SistemaPension(2L, "ONP", TipoSistemaPension.ONP),
                null, EstadoTrabajador.ACTIVO);
    }

    private final PeriodoPlanilla periodo = new PeriodoPlanilla(1L, 2026, 11, EstadoPeriodo.ABIERTO);

    private final TasaPension tasaIntegraFlujo = new TasaPension(1L, TipoComision.FLUJO,
            new BigDecimal("10"), new BigDecimal("1.37"), new BigDecimal("1.55"),
            LocalDate.of(2026, 1, 1), null);

    @Test
    void boletaAfpSinFaltasDa2612_40() {
        var asistencia = new AsistenciaMensual(1L, trabajadorAfp(), periodo, 0, 0);

        BoletaPago boleta = calculadora.calcularBoleta(asistencia, tasaIntegraFlujo,
                new BigDecimal("13"), new BigDecimal("9"), LocalDate.of(2026, 11, 30));

        assertEquals(0, new BigDecimal("387.60").compareTo(boleta.totalDescuentos()));
        assertEquals(0, new BigDecimal("2612.40").compareTo(boleta.netoPagar()));
        assertEquals(0, new BigDecimal("270.00").compareTo(boleta.aporteEssalud()));
    }

    @Test
    void boletaOnpSinFaltasDa2610() {
        var asistencia = new AsistenciaMensual(2L, trabajadorOnp(), periodo, 0, 0);

        BoletaPago boleta = calculadora.calcularBoleta(asistencia, null,
                new BigDecimal("13"), new BigDecimal("9"), LocalDate.of(2026, 11, 30));

        assertEquals(0, new BigDecimal("390.00").compareTo(boleta.totalDescuentos()));
        assertEquals(0, new BigDecimal("2610.00").compareTo(boleta.netoPagar()));
    }

    @Test
    void boletaAfpConDosFaltasY30MinutosDeTardanza() {
        var asistencia = new AsistenciaMensual(3L, trabajadorAfp(), periodo, 2, 30);

        BoletaPago boleta = calculadora.calcularBoleta(asistencia, tasaIntegraFlujo,
                new BigDecimal("13"), new BigDecimal("9"), LocalDate.of(2026, 11, 30));

        // faltas 200.00 + tardanza 6.25 + AFP 360.95 = 567.20
        assertEquals(0, new BigDecimal("567.20").compareTo(boleta.totalDescuentos()));
        assertEquals(0, new BigDecimal("2432.80").compareTo(boleta.netoPagar()));
        assertEquals(0, new BigDecimal("251.44").compareTo(boleta.aporteEssalud()));
    }

    @Test
    void periodoCerradoNoSeCalcula() {
        var cerrado = new PeriodoPlanilla(2L, 2026, 10, EstadoPeriodo.CERRADO);
        var asistencia = new AsistenciaMensual(4L, trabajadorAfp(), cerrado, 0, 0);

        assertThrows(ReglaPlanillaException.class, () ->
                calculadora.calcularBoleta(asistencia, tasaIntegraFlujo,
                        new BigDecimal("13"), new BigDecimal("9"), LocalDate.of(2026, 11, 30)));
    }

}
