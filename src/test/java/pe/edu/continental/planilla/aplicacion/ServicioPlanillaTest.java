package pe.edu.continental.planilla.aplicacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.continental.planilla.dominio.CalculadoraPlanilla;
import pe.edu.continental.planilla.dominio.excepcion.ReglaPlanillaException;
import pe.edu.continental.planilla.dominio.modelo.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServicioPlanillaTest {
    private final SistemaPension afp = new SistemaPension(1L, "AFP Integra", TipoSistemaPension.AFP);
    private final SistemaPension onp = new SistemaPension(2L, "ONP", TipoSistemaPension.ONP);
    private final Clock reloj = Clock.fixed(Instant.parse("2026-11-30T12:00:00Z"), ZoneOffset.UTC);
    private final UnidadDeTrabajoEnMemoria unidad = new UnidadDeTrabajoEnMemoria();

    private RepositoriosEnMemoria.Trabajadores trabajadores;
    private RepositoriosEnMemoria.Periodos periodos;
    private RepositoriosEnMemoria.Asistencias asistencias;
    private RepositoriosEnMemoria.Tasas tasas;
    private RepositoriosEnMemoria.Parametros parametros;
    private RepositoriosEnMemoria.Boletas boletas;
    private ServicioPlanilla servicio;
    private PeriodoPlanilla periodoNoviembre;

    @BeforeEach
    void preparar() {
        trabajadores = new RepositoriosEnMemoria.Trabajadores();
        periodos = new RepositoriosEnMemoria.Periodos();
        asistencias = new RepositoriosEnMemoria.Asistencias();
        tasas = new RepositoriosEnMemoria.Tasas();
        parametros = new RepositoriosEnMemoria.Parametros();
        boletas = new RepositoriosEnMemoria.Boletas();
        servicio = crearServicio(tasas);

        tasas.agregar(afp, new TasaPension(1L, TipoComision.FLUJO,
                new BigDecimal("10"), new BigDecimal("1.37"), new BigDecimal("1.55"),
                LocalDate.of(2026, 1, 1), null));
        parametros.guardar(new ParametroGeneral(null, "ONP", new BigDecimal("13"),
                "Aporte ONP", LocalDate.of(2026, 1, 1)));
        parametros.guardar(new ParametroGeneral(null, "ESSALUD", new BigDecimal("9"),
                "Aporte EsSalud", LocalDate.of(2026, 1, 1)));

        periodoNoviembre = periodos.guardar(new PeriodoPlanilla(null, 2026, 11, EstadoPeriodo.ABIERTO));
    }

    private ServicioPlanilla crearServicio(RepositoriosEnMemoria.Tasas tasasAUsar) {
        return new ServicioPlanilla(trabajadores, periodos, asistencias, tasasAUsar,
                parametros, boletas, new CalculadoraPlanilla(), unidad, reloj);
    }

    private Trabajador nuevoAfp(String dni) {
        return servicio.registrarTrabajador(new Trabajador(null, dni, "Joe", "Prueba", "Analista",
                LocalDate.of(2024, 1, 1), new BigDecimal("3000"), afp,
                TipoComision.FLUJO, EstadoTrabajador.ACTIVO));
    }

    private Trabajador nuevoOnp(String dni) {
        return servicio.registrarTrabajador(new Trabajador(null, dni, "Ana", "Prueba", "Asistente",
                LocalDate.of(2024, 1, 1), new BigDecimal("3000"), onp,
                null, EstadoTrabajador.ACTIVO));
    }

    private BoletaPago boletaDe(List<BoletaPago> lista, String dni) {
        return lista.stream().filter(b -> b.trabajador().dni().equals(dni)).findFirst().orElseThrow();
    }

    private void assertMonto(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real),
                "Esperado " + esperado + " pero fue " + real);
    }

    @Test
    void calculaUnaBoletaPorTrabajadorActivoSinAsistenciaRegistrada() {
        nuevoAfp("11111111");
        nuevoOnp("22222222");

        List<BoletaPago> resultado = servicio.calcularPlanilla(periodoNoviembre.idPeriodo());

        assertEquals(2, resultado.size());
        assertMonto("2612.40", boletaDe(resultado, "11111111").netoPagar());
        assertMonto("2610.00", boletaDe(resultado, "22222222").netoPagar());
    }

    @Test
    void asistenciaRegistradaSeAplicaAlCalculo() {
        Trabajador t = nuevoAfp("11111111");
        servicio.registrarAsistencia(t.idTrabajador(), periodoNoviembre.idPeriodo(), 2, 30);

        List<BoletaPago> resultado = servicio.calcularPlanilla(periodoNoviembre.idPeriodo());

        assertMonto("567.20", resultado.get(0).totalDescuentos());
        assertMonto("2432.80", resultado.get(0).netoPagar());
        assertMonto("251.44", resultado.get(0).aporteEssalud());
    }

    @Test
    void registrarAsistenciaDosVecesActualizaEnVezDeDuplicar() {
        Trabajador t = nuevoAfp("11111111");
        servicio.registrarAsistencia(t.idTrabajador(), periodoNoviembre.idPeriodo(), 1, 0);
        servicio.registrarAsistencia(t.idTrabajador(), periodoNoviembre.idPeriodo(), 3, 0);

        assertEquals(3, asistencias.buscar(t.idTrabajador(), periodoNoviembre.idPeriodo())
                .orElseThrow().diasFalta());
    }

    @Test
    void recalcularNoDuplicaLasBoletas() {
        nuevoAfp("11111111");

        servicio.calcularPlanilla(periodoNoviembre.idPeriodo());
        servicio.calcularPlanilla(periodoNoviembre.idPeriodo());

        assertEquals(1, servicio.resumenMensual(periodoNoviembre.idPeriodo()).cantidadBoletas());
    }

    @Test
    void resumenMensualSumaLasBoletas() {
        nuevoAfp("11111111");
        nuevoOnp("22222222");
        servicio.calcularPlanilla(periodoNoviembre.idPeriodo());

        ResumenPlanilla resumen = servicio.resumenMensual(periodoNoviembre.idPeriodo());

        assertEquals(2, resumen.cantidadBoletas());
        assertMonto("6000.00", resumen.totalRemuneraciones());
        assertMonto("777.60", resumen.totalDescuentos());
        assertMonto("5222.40", resumen.totalNeto());
        assertMonto("540.00", resumen.totalEssalud());
    }

    @Test
    void trabajadorCesadoNoEntraEnLaPlanilla() {
        nuevoAfp("11111111");
        servicio.registrarTrabajador(new Trabajador(null, "33333333", "Luis", "Cesado", "Vigilante",
                LocalDate.of(2023, 1, 1), new BigDecimal("1500"), onp,
                null, EstadoTrabajador.CESADO));

        List<BoletaPago> resultado = servicio.calcularPlanilla(periodoNoviembre.idPeriodo());

        assertEquals(1, resultado.size());
    }

    @Test
    void periodoCerradoNoPermiteCalcular() {
        PeriodoPlanilla cerrado = periodos.guardar(new PeriodoPlanilla(null, 2026, 10, EstadoPeriodo.CERRADO));
        nuevoAfp("11111111");

        assertThrows(ReglaPlanillaException.class, () -> servicio.calcularPlanilla(cerrado.idPeriodo()));
    }

    @Test
    void periodoCerradoNoPermiteRegistrarAsistencia() {
        PeriodoPlanilla cerrado = periodos.guardar(new PeriodoPlanilla(null, 2026, 10, EstadoPeriodo.CERRADO));
        Trabajador t = nuevoAfp("11111111");

        assertThrows(ReglaPlanillaException.class,
                () -> servicio.registrarAsistencia(t.idTrabajador(), cerrado.idPeriodo(), 1, 0));
    }

    @Test
    void asistenciaDeTrabajadorInexistenteLanzaError() {
        assertThrows(TrabajadorNoEncontradoException.class,
                () -> servicio.registrarAsistencia(99L, periodoNoviembre.idPeriodo(), 0, 0));
    }

    @Test
    void calcularPeriodoInexistenteLanzaError() {
        assertThrows(PeriodoNoEncontradoException.class, () -> servicio.calcularPlanilla(99L));
    }

    @Test
    void actualizarTrabajadorInexistenteLanzaError() {
        Trabajador fantasma = new Trabajador(99L, "44444444", "No", "Existe", "Ninguno",
                LocalDate.of(2024, 1, 1), new BigDecimal("1000"), onp,
                null, EstadoTrabajador.ACTIVO);

        assertThrows(TrabajadorNoEncontradoException.class, () -> servicio.actualizarTrabajador(fantasma));
    }

    @Test
    void sinTasaAfpVigenteNoSePuedeCalcular() {
        ServicioPlanilla sinTasas = crearServicio(new RepositoriosEnMemoria.Tasas());
        nuevoAfp("11111111");

        assertThrows(ReglaPlanillaException.class,
                () -> sinTasas.calcularPlanilla(periodoNoviembre.idPeriodo()));
    }

    @Test
    void sinParametroEssaludNoSePuedeCalcular() {
        ServicioPlanilla sinParametros = new ServicioPlanilla(trabajadores, periodos, asistencias, tasas,
                new RepositoriosEnMemoria.Parametros(), boletas, new CalculadoraPlanilla(), unidad, reloj);
        nuevoAfp("11111111");

        assertThrows(ReglaPlanillaException.class,
                () -> sinParametros.calcularPlanilla(periodoNoviembre.idPeriodo()));
    }

    @Test
    void calcularPlanillaSeEjecutaDentroDeLaUnidadDeTrabajo() {
        nuevoAfp("11111111");
        int antes = unidad.ejecuciones();

        servicio.calcularPlanilla(periodoNoviembre.idPeriodo());

        assertEquals(antes + 1, unidad.ejecuciones());
    }
}
