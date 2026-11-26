package pe.edu.continental.planilla.infraestructura.jpa;


import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import pe.edu.continental.planilla.aplicacion.ResumenPlanilla;
import pe.edu.continental.planilla.aplicacion.ServicioPlanilla;
import pe.edu.continental.planilla.dominio.CalculadoraPlanilla;
import pe.edu.continental.planilla.dominio.excepcion.ReglaPlanillaException;
import pe.edu.continental.planilla.dominio.modelo.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class PersistenciaJpaIT {
    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17")
            .withInitScript("db/esquema.sql");

    private static EntityManagerFactory fabrica;
    private JpaUnidadDeTrabajo unidad;
    private RepositoriosJpa.Periodos periodos;
    private ServicioPlanilla servicio;

    private final Clock reloj = Clock.fixed(Instant.parse("2026-11-30T12:00:00Z"), ZoneOffset.UTC);

    @BeforeAll
    static void iniciarFabrica() {
        ConfiguracionBd config = new ConfiguracionBd(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword());
        fabrica = FabricaPersistencia.crear(config, false);
    }

    @AfterAll
    static void cerrarFabrica() {
        if (fabrica != null) {
            fabrica.close();
        }
    }

    @BeforeEach
    void limpiarYPreparar() {
        unidad = new JpaUnidadDeTrabajo(fabrica);
        // Limpia datos transaccionales entre pruebas conservando catálogos iniciales de esquema.sql
        unidad.ejecutar(() -> {
            unidad.entityManager().createNativeQuery(
                            "TRUNCATE TABLE detalle_boleta, boleta_pago, asistencia_mensual, periodo_planilla, trabajador RESTART IDENTITY CASCADE")
                    .executeUpdate();
            return null;
        });

        var trabajadores = new RepositoriosJpa.Trabajadores(unidad);
        periodos = new RepositoriosJpa.Periodos(unidad);
        var asistencias = new RepositoriosJpa.Asistencias(unidad);
        var tasas = new RepositoriosJpa.Tasas(unidad);
        var parametros = new RepositoriosJpa.Parametros(unidad);
        var boletas = new RepositoriosJpa.Boletas(unidad);

        servicio = new ServicioPlanilla(trabajadores, periodos, asistencias, tasas,
                parametros, boletas, new CalculadoraPlanilla(), unidad, reloj);
    }

    private SistemaPension buscarSistema(String nombre) {
        return unidad.ejecutar(() -> unidad.entityManager()
                .createQuery("SELECT s FROM SistemaPension s WHERE s.nombre = :nombre", SistemaPension.class)
                .setParameter("nombre", nombre)
                .getSingleResult());
    }

    private PeriodoPlanilla abrirPeriodo(int anio, int mes) {
        return unidad.ejecutar(() -> periodos.guardar(new PeriodoPlanilla(null, anio, mes, EstadoPeriodo.ABIERTO)));
    }

    @Test
    void guardaActualizaYListaSoloTrabajadoresActivos() {
        SistemaPension afp = buscarSistema("AFP Integra");
        SistemaPension onp = buscarSistema("ONP");

        Trabajador activo = servicio.registrarTrabajador(new Trabajador(null, "70112233", "Ana", "Torres",
                "Analista", LocalDate.of(2024, 1, 10), new BigDecimal("3000.00"),
                afp, TipoComision.FLUJO, EstadoTrabajador.ACTIVO));

        Trabajador cesado = servicio.registrarTrabajador(new Trabajador(null, "45667788", "Luis", "Rojas",
                "Soporte", LocalDate.of(2023, 5, 1), new BigDecimal("1800.00"),
                onp, null, EstadoTrabajador.ACTIVO));

        // Actualizamos al segundo trabajador a estado CESADO
        servicio.actualizarTrabajador(new Trabajador(cesado.idTrabajador(), cesado.dni(), cesado.nombres(),
                cesado.apellidos(), cesado.cargo(), cesado.fechaIngreso(), cesado.sueldoBasico(),
                cesado.sistemaPension(), null, EstadoTrabajador.CESADO));

        List<Trabajador> activos = servicio.listarTrabajadoresActivos();

        assertEquals(1, activos.size());
        assertEquals(activo.idTrabajador(), activos.getFirst().idTrabajador());
        assertEquals("70112233", activos.getFirst().dni());
    }

    @Test
    void calculaYRecalculaPlanillaPersistiendoBoletasYDetalles() {
        SistemaPension afp = buscarSistema("AFP Integra");
        SistemaPension onp = buscarSistema("ONP");
        PeriodoPlanilla noviembre = abrirPeriodo(2026, 11);

        Trabajador tAfp = servicio.registrarTrabajador(new Trabajador(null, "11111111", "Joe", "Prueba",
                "Analista", LocalDate.of(2024, 1, 1), new BigDecimal("3000.00"),
                afp, TipoComision.FLUJO, EstadoTrabajador.ACTIVO));

        servicio.registrarTrabajador(new Trabajador(null, "22222222", "Marina", "Prueba",
                "Asistente", LocalDate.of(2024, 1, 1), new BigDecimal("3000.00"),
                onp, null, EstadoTrabajador.ACTIVO));

        // 2 faltas y 30 min de tardanza para el trabajador AFP
        servicio.registrarAsistencia(tAfp.idTrabajador(), noviembre.idPeriodo(), 2, 30);

        List<BoletaPago> primeraEjecucion = servicio.calcularPlanilla(noviembre.idPeriodo());
        assertEquals(2, primeraEjecucion.size());
        assertFalse(primeraEjecucion.getFirst().detalles().isEmpty());

        // Recalcular el mismo período debe reemplazar las boletas sin violar UNIQUE (id_trabajador, id_periodo)
        List<BoletaPago> segundaEjecucion = servicio.calcularPlanilla(noviembre.idPeriodo());
        assertEquals(2, segundaEjecucion.size());

        ResumenPlanilla resumen = servicio.resumenMensual(noviembre.idPeriodo());
        assertEquals(2, resumen.cantidadBoletas());
        assertEquals(0, new BigDecimal("6000.00").compareTo(resumen.totalRemuneraciones()));
        assertEquals(0, new BigDecimal("5042.80").compareTo(resumen.totalNeto()));
    }

    @Test
    void periodoCerradoNoPermiteRegistrarAsistenciaNiCalcular() {
        SistemaPension onp = buscarSistema("ONP");
        PeriodoPlanilla cerrado = unidad.ejecutar(
                () -> periodos.guardar(new PeriodoPlanilla(null, 2026, 10, EstadoPeriodo.CERRADO)));

        Trabajador t = servicio.registrarTrabajador(new Trabajador(null, "33333333", "Gabriel", "Prueba",
                "Técnico", LocalDate.of(2024, 2, 1), new BigDecimal("2000.00"),
                onp, null, EstadoTrabajador.ACTIVO));

        assertThrows(ReglaPlanillaException.class,
                () -> servicio.registrarAsistencia(t.idTrabajador(), cerrado.idPeriodo(), 1, 0));
        assertThrows(ReglaPlanillaException.class,
                () -> servicio.calcularPlanilla(cerrado.idPeriodo()));
    }
}
