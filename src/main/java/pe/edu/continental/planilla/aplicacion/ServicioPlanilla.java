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
import java.util.function.Function;

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
    private final UnidadDeTrabajo unidad;
    private final Clock reloj;

    public ServicioPlanilla(TrabajadorRepositorio trabajadores,
                            PeriodoRepositorio periodos,
                            AsistenciaRepositorio asistencias,
                            TasaPensionRepositorio tasas,
                            ParametroRepositorio parametros,
                            BoletaRepositorio boletas,
                            CalculadoraPlanilla calculadora,
                            UnidadDeTrabajo unidad,
                            Clock reloj) {
        this.trabajadores = trabajadores;
        this.periodos = periodos;
        this.asistencias = asistencias;
        this.tasas = tasas;
        this.parametros = parametros;
        this.boletas = boletas;
        this.calculadora = calculadora;
        this.unidad = unidad;
        this.reloj = reloj;
    }

    // ---------- Trabajadores (HU-01 a HU-04) ----------

    public Trabajador registrarTrabajador(Trabajador trabajador) {
        return unidad.ejecutar(() -> {
            if (trabajadores.buscarPorDni(trabajador.dni()).isPresent()) {
                throw new ReglaPlanillaException("Ya existe un trabajador registrado con el DNI " + trabajador.dni());
            }
            return trabajadores.guardar(trabajador);
        });
    }

    public Trabajador actualizarTrabajador(Trabajador trabajador) {
        return unidad.ejecutar(() -> {
            if (trabajador.idTrabajador() == null
                    || trabajadores.buscarPorId(trabajador.idTrabajador()).isEmpty()) {
                throw new TrabajadorNoEncontradoException(trabajador.idTrabajador());
            }
            trabajadores.buscarPorDni(trabajador.dni())
                    .filter(otro -> !otro.idTrabajador().equals(trabajador.idTrabajador()))
                    .ifPresent(otro -> {
                        throw new ReglaPlanillaException("Ya existe otro trabajador con el DNI " + trabajador.dni());
                    });
            return trabajadores.guardar(trabajador);
        });
    }

    public List<Trabajador> listarTrabajadoresActivos() {
        return unidad.ejecutar(trabajadores::listarActivos);
    }

    // ---------- Asistencia (HU-05 a HU-07) ----------

    public AsistenciaMensual registrarAsistencia(Long idTrabajador, Long idPeriodo,
                                                 int diasFalta, int minutosTardanza) {
        return unidad.ejecutar(() -> {
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
        });
    }

    // ---------- Cálculo de planilla (HU-10 a HU-13) ----------

    public List<BoletaPago> calcularPlanilla(Long idPeriodo) {
        return unidad.ejecutar(() -> {
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
        });
    }

    // ---------- Reporte mensual (HU-16) ----------

    public ResumenPlanilla resumenMensual(Long idPeriodo) {
        return unidad.ejecutar(() -> {
            PeriodoPlanilla periodo = periodos.buscarPorId(idPeriodo)
                    .orElseThrow(() -> new PeriodoNoEncontradoException(idPeriodo));
            List<BoletaPago> lista = boletas.listarPorPeriodo(idPeriodo);

            return new ResumenPlanilla(
                    periodo.anio(), periodo.mes(), lista.size(),
                    sumar(lista, BoletaPago::remuneracionBruta),
                    sumar(lista, BoletaPago::totalDescuentos),
                    sumar(lista, BoletaPago::netoPagar),
                    sumar(lista, BoletaPago::aporteEssalud));
        });
    }
    // ---------- Períodos ----------

    public PeriodoPlanilla abrirPeriodo(int anio, int mes) {
        return unidad.ejecutar(() -> {
            boolean yaExiste = periodos.listarTodos().stream()
                    .anyMatch(p -> p.anio() == anio && p.mes() == mes);
            if (yaExiste) {
                throw new ReglaPlanillaException("Ya existe un período registrado para " + mes + "/" + anio);
            }
            return periodos.guardar(new PeriodoPlanilla(null, anio, mes, EstadoPeriodo.ABIERTO));
        });
    }

    public List<PeriodoPlanilla> listarPeriodos() {
        return unidad.ejecutar(periodos::listarTodos);
    }

    public PeriodoPlanilla cerrarPeriodo(Long idPeriodo) {
        return unidad.ejecutar(() -> {
            PeriodoPlanilla periodo = periodos.buscarPorId(idPeriodo)
                    .orElseThrow(() -> new PeriodoNoEncontradoException(idPeriodo));
            if (!periodo.estaAbierto()) {
                throw new ReglaPlanillaException("El período ya se encuentra cerrado");
            }
            return periodos.guardar(
                    new PeriodoPlanilla(periodo.idPeriodo(), periodo.anio(), periodo.mes(), EstadoPeriodo.CERRADO));
        });
    }

    // ---------- Consultas y configuración (HU-03, HU-07, HU-08, HU-09, HU-15) ----------

    public List<SistemaPension> listarSistemasPension() {
        return unidad.ejecutar(tasas::listarSistemasPension);
    }

    public List<AsistenciaMensual> listarAsistencias(Long idPeriodo) {
        return unidad.ejecutar(() -> {
            if (periodos.buscarPorId(idPeriodo).isEmpty()) {
                throw new PeriodoNoEncontradoException(idPeriodo);
            }
            return asistencias.listarPorPeriodo(idPeriodo);
        });
    }

    public List<BoletaPago> listarBoletas(Long idPeriodo) {
        return unidad.ejecutar(() -> {
            if (periodos.buscarPorId(idPeriodo).isEmpty()) {
                throw new PeriodoNoEncontradoException(idPeriodo);
            }
            return boletas.listarPorPeriodo(idPeriodo);
        });
    }

    public record DatosBoletaPdf(BoletaPago boleta, Empresa empresa) {}

    public DatosBoletaPdf obtenerDatosBoleta(Long idBoleta) {
        return unidad.ejecutar(() -> {
            BoletaPago boleta = boletas.buscarPorId(idBoleta)
                    .orElseThrow(() -> new ReglaPlanillaException("No existe la boleta con id " + idBoleta));
            boleta.detalles().size(); // carga los conceptos mientras la sesión JPA está abierta
            Empresa empresa = boletas.obtenerEmpresa()
                    .orElseGet(() -> new Empresa(null, "20601234567",
                            "MI MICROEMPRESA S.A.C.", "Huancayo, Junín - Perú"));
            return new DatosBoletaPdf(boleta, empresa);
        });
    }

    public List<ParametroGeneral> listarParametros() {
        return unidad.ejecutar(parametros::listarTodos);
    }

    public ParametroGeneral actualizarParametro(String codigo, BigDecimal nuevoValor, String nuevaDescripcion) {
        return unidad.ejecutar(() -> {
            if (codigo == null || codigo.isBlank()) {
                throw new ReglaPlanillaException("El código del parámetro es obligatorio");
            }
            String codigoNormalizado = codigo.trim().toUpperCase();
            ParametroGeneral actual = parametros.buscarVigente(codigoNormalizado, LocalDate.now(reloj))
                    .orElseThrow(() -> new ReglaPlanillaException("No existe el parámetro " + codigoNormalizado));
            String descripcionFinal = (nuevaDescripcion == null || nuevaDescripcion.isBlank())
                    ? actual.descripcion()
                    : nuevaDescripcion.trim();
            return parametros.guardar(new ParametroGeneral(
                    actual.idParametro(), actual.codigo(), nuevoValor, descripcionFinal, actual.vigenteDesde()));
        });
    }

    public List<TasaPension> listarTasasPension() {
        return unidad.ejecutar(tasas::listarTodas);
    }

    public TasaPension actualizarTasaPension(Long idTasa, BigDecimal pctAporte,
                                             BigDecimal pctPrima, BigDecimal pctComision) {
        return unidad.ejecutar(() -> {
            TasaPension actual = tasas.buscarPorId(idTasa)
                    .orElseThrow(() -> new ReglaPlanillaException("No existe la tasa de pensión con id " + idTasa));
            return tasas.guardar(new TasaPension(
                    actual.idTasa(), actual.sistemaPension(), actual.tipoComision(),
                    pctAporte, pctPrima, pctComision, actual.vigenteDesde(), actual.vigenteHasta()));
        });
    }

    // ---------- Auxiliares ----------

    private BigDecimal porcentajeVigente(String codigo, LocalDate fecha) {
        return parametros.buscarVigente(codigo, fecha)
                .orElseThrow(() -> new ReglaPlanillaException("Falta el parámetro " + codigo))
                .valor();
    }

    private BigDecimal sumar(List<BoletaPago> lista, Function<BoletaPago, BigDecimal> campo) {
        return lista.stream().map(campo).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }
}
