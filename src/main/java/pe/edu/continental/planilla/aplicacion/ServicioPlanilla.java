package pe.edu.continental.planilla.aplicacion;

import pe.edu.continental.planilla.aplicacion.puerto.*;
import pe.edu.continental.planilla.dominio.CalculadoraPlanilla;
import pe.edu.continental.planilla.dominio.excepcion.ReglaPlanillaException;
import pe.edu.continental.planilla.dominio.modelo.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServicioPlanilla {
    public static final String COD_ONP = "ONP";
    public static final String COD_ESSALUD = "ESSALUD";

    private final TrabajadorRepositorio trabajadores;
    private final PeriodoRepositorio periodos;
    private final AsistenciaRepositorio asistencias;
    private final TasaPensionRepositorio tasas;
    private final ParametroRepositorio parametros;
    private final BoletaRepositorio boletas;
    private final CalculadoraPlanilla calculadora;
    private final Clock reloj;

    public ServicioPlanilla(TrabajadorRepositorio trabajadores,
                            PeriodoRepositorio periodos,
                            AsistenciaRepositorio asistencias,
                            TasaPensionRepositorio tasas,
                            ParametroRepositorio parametros,
                            BoletaRepositorio boletas,
                            CalculadoraPlanilla calculadora,
                            Clock reloj) {
        this.trabajadores = trabajadores;
        this.periodos = periodos;
        this.asistencias = asistencias;
        this.tasas = tasas;
        this.parametros = parametros;
        this.boletas = boletas;
        this.calculadora = calculadora;
        this.reloj = reloj;
    }

    // ---------- Trabajadores (HU-01 a HU-04) ----------

    public Trabajador registrarTrabajador(Trabajador trabajador) {
        return trabajadores.guardar(trabajador);
    }

    public Trabajador actualizarTrabajador(Trabajador trabajador) {
        if (trabajador.idTrabajador() == null
                || trabajadores.buscarPorId(trabajador.idTrabajador()).isEmpty()) {
            throw new TrabajadorNoEncontradoException(trabajador.idTrabajador());
        }
        return trabajadores.guardar(trabajador);
    }

    public List<Trabajador> listarTrabajadoresActivos() {
        return trabajadores.listarActivos();
    }

    // ---------- Asistencia (HU-05 a HU-07) ----------

    public AsistenciaMensual registrarAsistencia(Long idTrabajador, Long idPeriodo,
                                                 int diasFalta, int minutosTardanza) {
        Trabajador trabajador = trabajadores.buscarPorId(idTrabajador)
                .orElseThrow(() -> new TrabajadorNoEncontradoException(idTrabajador));
        PeriodoPlanilla periodo = periodos.buscarPorId(idPeriodo)
                .orElseThrow(() -> new PeriodoNoEncontradoException(idPeriodo));

        if (!periodo.estaAbierto()) {
            throw new ReglaPlanillaException("El período está cerrado, no se puede registrar asistencia");
        }

        // si ya había registro de ese mes, se actualiza en vez de duplicar
        Long idExistente = asistencias.buscar(idTrabajador, idPeriodo)
                .map(AsistenciaMensual::idAsistencia)
                .orElse(null);

        return asistencias.guardar(
                new AsistenciaMensual(idExistente, trabajador, periodo, diasFalta, minutosTardanza));
    }

    // ---------- Cálculo de planilla (HU-10 a HU-13) ----------

    public List<BoletaPago> calcularPlanilla(Long idPeriodo) {
        PeriodoPlanilla periodo = periodos.buscarPorId(idPeriodo)
                .orElseThrow(() -> new PeriodoNoEncontradoException(idPeriodo));
        if (!periodo.estaAbierto()) {
            throw new ReglaPlanillaException("El período está cerrado, no se puede calcular");
        }

        LocalDate fechaEmision = LocalDate.now(reloj);
        LocalDate fechaVigencia = LocalDate.of(periodo.anio(), periodo.mes(), 1);

        BigDecimal pctOnp = porcentajeVigente(COD_ONP, fechaVigencia);
        BigDecimal pctEssalud = porcentajeVigente(COD_ESSALUD, fechaVigencia);

        boletas.eliminarPorPeriodo(idPeriodo);   // recalcular = reemplazar

        List<BoletaPago> resultado = new ArrayList<>();
        for (Trabajador trabajador : trabajadores.listarActivos()) {
            // sin registro de asistencia = mes sin faltas ni tardanzas
            AsistenciaMensual asistencia = asistencias.buscar(trabajador.idTrabajador(), idPeriodo)
                    .orElseGet(() -> new AsistenciaMensual(null, trabajador, periodo, 0, 0));

            TasaPension tasaAfp = null;
            if (trabajador.sistemaPension().esAfp()) {
                tasaAfp = tasas.buscarVigente(trabajador.sistemaPension(),
                                trabajador.tipoComision(), fechaVigencia)
                        .orElseThrow(() -> new ReglaPlanillaException(
                                "No hay tasa vigente para " + trabajador.sistemaPension().nombre()
                                        + " (" + trabajador.tipoComision() + ")"));
            }

            BoletaPago boleta = calculadora.calcularBoleta(asistencia, tasaAfp,
                    pctOnp, pctEssalud, fechaEmision);
            resultado.add(boletas.guardar(boleta));
        }
        return resultado;
    }

    // ---------- Reporte mensual (HU-16) ----------

    public ResumenPlanilla resumenMensual(Long idPeriodo) {
        PeriodoPlanilla periodo = periodos.buscarPorId(idPeriodo)
                .orElseThrow(() -> new PeriodoNoEncontradoException(idPeriodo));
        List<BoletaPago> lista = boletas.listarPorPeriodo(idPeriodo);

        return new ResumenPlanilla(
                periodo.anio(), periodo.mes(), lista.size(),
                sumar(lista, BoletaPago::remuneracionBruta),
                sumar(lista, BoletaPago::totalDescuentos),
                sumar(lista, BoletaPago::netoPagar),
                sumar(lista, BoletaPago::aporteEssalud));
    }

    // ---------- Auxiliares ----------

    private BigDecimal porcentajeVigente(String codigo, LocalDate fecha) {
        return parametros.buscarVigente(codigo, fecha)
                .orElseThrow(() -> new ReglaPlanillaException("Falta el parámetro " + codigo))
                .valor();
    }

    private BigDecimal sumar(List<BoletaPago> lista,
                             java.util.function.Function<BoletaPago, BigDecimal> campo) {
        return lista.stream().map(campo).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }
}
