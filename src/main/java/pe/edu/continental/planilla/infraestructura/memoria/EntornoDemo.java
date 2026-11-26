package pe.edu.continental.planilla.infraestructura.memoria;

import pe.edu.continental.planilla.aplicacion.ServicioPlanilla;
import pe.edu.continental.planilla.dominio.CalculadoraPlanilla;
import pe.edu.continental.planilla.dominio.modelo.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Entorno de práctica: un ServicioPlanilla ya armado, con repositorios en memoria,
 * tasas y parámetros cargados, un período abierto (el mes de hoy) y dos trabajadores
 * de ejemplo. Los porcentajes de AFP son valores de ejemplo, no oficiales.
 *
 * Uso:  EntornoDemo entorno = EntornoDemo.crear();
 *       entorno.servicio().listarTrabajadoresActivos();
 */
public final class EntornoDemo {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final LocalDate VIGENTE_DESDE = LocalDate.of(2020, 1, 1);

    private final ServicioPlanilla servicio;
    private final PeriodoPlanilla periodoActual;
    private final SistemaPension onp;
    private final SistemaPension afp;

    private EntornoDemo(ServicioPlanilla servicio, PeriodoPlanilla periodoActual,
                        SistemaPension onp, SistemaPension afp) {
        this.servicio = servicio;
        this.periodoActual = periodoActual;
        this.onp = onp;
        this.afp = afp;
    }

    public static EntornoDemo crear() {
        return crear(Clock.system(LIMA));
    }

    public static EntornoDemo crear(Clock reloj) {
        var trabajadores = new RepositoriosEnMemoria.Trabajadores();
        var periodos = new RepositoriosEnMemoria.Periodos();
        var asistencias = new RepositoriosEnMemoria.Asistencias();
        var tasas = new RepositoriosEnMemoria.Tasas();
        var parametros = new RepositoriosEnMemoria.Parametros();
        var boletas = new RepositoriosEnMemoria.Boletas();

        SistemaPension onp = new SistemaPension(1L, "ONP", TipoSistemaPension.ONP);
        SistemaPension afp = new SistemaPension(2L, "AFP Integra", TipoSistemaPension.AFP);

        tasas.agregar(afp, new TasaPension(1L, afp, TipoComision.FLUJO,
                new BigDecimal("10"), new BigDecimal("1.37"), new BigDecimal("1.55"), VIGENTE_DESDE, null));
        tasas.agregar(afp, new TasaPension(2L, afp, TipoComision.MIXTA,
                new BigDecimal("10"), new BigDecimal("1.37"), new BigDecimal("1.20"), VIGENTE_DESDE, null));

        parametros.guardar(new ParametroGeneral(null, ServicioPlanilla.COD_ONP,
                new BigDecimal("13"), "Aporte ONP", VIGENTE_DESDE));
        parametros.guardar(new ParametroGeneral(null, ServicioPlanilla.COD_ESSALUD,
                new BigDecimal("9"), "Aporte EsSalud", VIGENTE_DESDE));

        LocalDate hoy = LocalDate.now(reloj);
        PeriodoPlanilla periodo = periodos.guardar(
                new PeriodoPlanilla(null, hoy.getYear(), hoy.getMonthValue(), EstadoPeriodo.ABIERTO));

        ServicioPlanilla servicio = new ServicioPlanilla(trabajadores, periodos, asistencias, tasas,
                parametros, boletas, new CalculadoraPlanilla(), new UnidadDeTrabajoEnMemoria(), reloj);

        servicio.registrarTrabajador(new Trabajador(null, "70112233", "Ana", "Torres", "Asistente",
                LocalDate.of(2024, 3, 1), new BigDecimal("1500"), onp, null, EstadoTrabajador.ACTIVO));
        servicio.registrarTrabajador(new Trabajador(null, "45678912", "Luis", "Rojas", "Vendedor",
                LocalDate.of(2023, 8, 15), new BigDecimal("2200"), afp, TipoComision.FLUJO,
                EstadoTrabajador.ACTIVO));

        return new EntornoDemo(servicio, periodo, onp, afp);
    }

    /** El servicio ya armado. */
    public ServicioPlanilla servicio() {
        return servicio;
    }

    /** Período abierto del mes de hoy, para calcular. */
    public PeriodoPlanilla periodoActual() {
        return periodoActual;
    }

    /** Sistema de pensión ONP, para registrar trabajadores. */
    public SistemaPension onp() {
        return onp;
    }

    /** Sistema de pensión AFP, para registrar trabajadores. */
    public SistemaPension afp() {
        return afp;
    }
}
