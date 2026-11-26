package pe.edu.continental.planilla.infraestructura.jpa;

import jakarta.persistence.EntityManager;
import pe.edu.continental.planilla.aplicacion.puerto.*;
import pe.edu.continental.planilla.dominio.modelo.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class RepositoriosJpa {
    private RepositoriosJpa() {
    }

    public static class Trabajadores implements TrabajadorRepositorio {
        private final JpaUnidadDeTrabajo unidad;

        public Trabajadores(JpaUnidadDeTrabajo unidad) {
            this.unidad = unidad;
        }

        @Override
        public Trabajador guardar(Trabajador trabajador) {
            EntityManager em = unidad.entityManager();
            if (trabajador.sistemaPension() != null && trabajador.sistemaPension().idSistemaPension() != null) {
                SistemaPension existente = em.find(
                        SistemaPension.class, trabajador.sistemaPension().idSistemaPension());
                if (existente == null) {
                    throw new pe.edu.continental.planilla.dominio.excepcion.DatoInvalidoException(
                            "No existe el sistema de pensión con id " + trabajador.sistemaPension().idSistemaPension());
                }
            }
            return em.merge(trabajador);
        }

        @Override
        public Optional<Trabajador> buscarPorId(Long id) {
            return Optional.ofNullable(unidad.entityManager().find(Trabajador.class, id));
        }

        @Override
        public List<Trabajador> listarActivos() {
            return unidad.entityManager()
                    .createQuery("SELECT t FROM Trabajador t WHERE t.estado = :estado ORDER BY t.apellidos, t.nombres",
                            Trabajador.class)
                    .setParameter("estado", EstadoTrabajador.ACTIVO)
                    .getResultList();
        }
        @Override
        public Optional<Trabajador> buscarPorDni(String dni) {
            return unidad.entityManager()
                    .createQuery("SELECT t FROM Trabajador t WHERE t.dni = :dni", Trabajador.class)
                    .setParameter("dni", dni)
                    .getResultStream()
                    .findFirst();
        }

    }

    public static class Periodos implements PeriodoRepositorio {
        private final JpaUnidadDeTrabajo unidad;

        public Periodos(JpaUnidadDeTrabajo unidad) {
            this.unidad = unidad;
        }

        @Override
        public PeriodoPlanilla guardar(PeriodoPlanilla periodo) {
            EntityManager em = unidad.entityManager();
            if (periodo.idPeriodo() == null) {
                em.persist(periodo);
                return periodo;
            }
            return em.merge(periodo);
        }

        @Override
        public Optional<PeriodoPlanilla> buscarPorId(Long id) {
            return Optional.ofNullable(unidad.entityManager().find(PeriodoPlanilla.class, id));
        }

        @Override
        public List<PeriodoPlanilla> listarTodos() {
            return unidad.entityManager()
                    .createQuery("SELECT p FROM PeriodoPlanilla p ORDER BY p.anio DESC, p.mes DESC", PeriodoPlanilla.class)
                    .getResultList();
        }
    }

    public static class Asistencias implements AsistenciaRepositorio {
        private final JpaUnidadDeTrabajo unidad;

        public Asistencias(JpaUnidadDeTrabajo unidad) {
            this.unidad = unidad;
        }

        @Override
        public AsistenciaMensual guardar(AsistenciaMensual asistencia) {
            EntityManager em = unidad.entityManager();
            if (asistencia.idAsistencia() == null) {
                em.persist(asistencia);
                return asistencia;
            }
            return em.merge(asistencia);
        }

        @Override
        public Optional<AsistenciaMensual> buscar(Long idTrabajador, Long idPeriodo) {
            return unidad.entityManager()
                    .createQuery("SELECT a FROM AsistenciaMensual a WHERE a.trabajador.idTrabajador = :idTrab AND a.periodo.idPeriodo = :idPer",
                            AsistenciaMensual.class)
                    .setParameter("idTrab", idTrabajador)
                    .setParameter("idPer", idPeriodo)
                    .getResultStream()
                    .findFirst();
        }

        @Override
        public List<AsistenciaMensual> listarPorPeriodo(Long idPeriodo) {
            return unidad.entityManager()
                    .createQuery("SELECT a FROM AsistenciaMensual a WHERE a.periodo.idPeriodo = :idPeriodo ORDER BY a.trabajador.apellidos, a.trabajador.nombres",
                            AsistenciaMensual.class)
                    .setParameter("idPeriodo", idPeriodo)
                    .getResultList();
        }
    }

    public static class Tasas implements TasaPensionRepositorio {
        private final JpaUnidadDeTrabajo unidad;

        public Tasas(JpaUnidadDeTrabajo unidad) {
            this.unidad = unidad;
        }

        @Override
        public Optional<TasaPension> buscarVigente(SistemaPension sistema, TipoComision tipoComision, LocalDate fecha) {
            return unidad.entityManager()
                    .createQuery("""
                            SELECT t FROM TasaPension t
                            WHERE t.sistemaPension = :sistema
                              AND t.tipoComision = :tipo
                              AND t.vigenteDesde <= :fecha
                              AND (t.vigenteHasta IS NULL OR t.vigenteHasta >= :fecha)
                            ORDER BY t.vigenteDesde DESC
                            """, TasaPension.class)
                    .setParameter("sistema", sistema)
                    .setParameter("tipo", tipoComision)
                    .setParameter("fecha", fecha)
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst();
        }
        @Override
        public TasaPension guardar(TasaPension tasa) {
            EntityManager em = unidad.entityManager();
            if (tasa.idTasa() == null) {
                em.persist(tasa);
                return tasa;
            }
            return em.merge(tasa);
        }

        @Override
        public Optional<TasaPension> buscarPorId(Long id) {
            return Optional.ofNullable(unidad.entityManager().find(TasaPension.class, id));
        }

        @Override
        public List<TasaPension> listarTodas() {
            return unidad.entityManager()
                    .createQuery("SELECT t FROM TasaPension t ORDER BY t.sistemaPension.nombre, t.tipoComision, t.vigenteDesde DESC",
                            TasaPension.class)
                    .getResultList();
        }

        @Override
        public List<SistemaPension> listarSistemasPension() {
            return unidad.entityManager()
                    .createQuery("SELECT s FROM SistemaPension s ORDER BY s.idSistemaPension",
                            SistemaPension.class)
                    .getResultList();
        }
    }

    public static class Parametros implements ParametroRepositorio {
        private final JpaUnidadDeTrabajo unidad;

        public Parametros(JpaUnidadDeTrabajo unidad) {
            this.unidad = unidad;
        }

        @Override
        public ParametroGeneral guardar(ParametroGeneral parametro) {
            EntityManager em = unidad.entityManager();
            if (parametro.idParametro() == null) {
                em.persist(parametro);
                return parametro;
            }
            return em.merge(parametro);
        }

        @Override
        public Optional<ParametroGeneral> buscarVigente(String codigo, LocalDate fecha) {
            return unidad.entityManager()
                    .createQuery("""
                            SELECT p FROM ParametroGeneral p
                            WHERE p.codigo = :codigo
                              AND p.vigenteDesde <= :fecha
                            ORDER BY p.vigenteDesde DESC
                            """, ParametroGeneral.class)
                    .setParameter("codigo", codigo)
                    .setParameter("fecha", fecha)
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst();
        }
        @Override
        public List<ParametroGeneral> listarTodos() {
            return unidad.entityManager()
                    .createQuery("SELECT p FROM ParametroGeneral p ORDER BY p.codigo, p.vigenteDesde DESC",
                            ParametroGeneral.class)
                    .getResultList();
        }
    }

    public static class Boletas implements BoletaRepositorio {
        private final JpaUnidadDeTrabajo unidad;

        public Boletas(JpaUnidadDeTrabajo unidad) {
            this.unidad = unidad;
        }

        @Override
        public BoletaPago guardar(BoletaPago boleta) {
            EntityManager em = unidad.entityManager();
            if (boleta.idBoleta() == null) {
                em.persist(boleta);
                return boleta;
            }
            return em.merge(boleta);
        }

        @Override
        public List<BoletaPago> listarPorPeriodo(Long idPeriodo) {
            return unidad.entityManager()
                    .createQuery("SELECT b FROM BoletaPago b WHERE b.periodo.idPeriodo = :idPeriodo ORDER BY b.trabajador.apellidos, b.trabajador.nombres",
                            BoletaPago.class)
                    .setParameter("idPeriodo", idPeriodo)
                    .getResultList();
        }

        @Override
        public void eliminarPorPeriodo(Long idPeriodo) {
            EntityManager em = unidad.entityManager();
            for (BoletaPago boleta : listarPorPeriodo(idPeriodo)) {
                em.remove(boleta);
            }
            // Fuerza el DELETE antes de insertar nuevas boletas en un recálculo
            em.flush();
        }

        @Override
        public Optional<BoletaPago> buscarPorId(Long idBoleta) {
            return Optional.ofNullable(unidad.entityManager().find(BoletaPago.class, idBoleta));
        }

        @Override
        public Optional<Empresa> obtenerEmpresa() {
            return unidad.entityManager()
                    .createQuery("SELECT e FROM Empresa e ORDER BY e.idEmpresa", Empresa.class)
                    .setMaxResults(1)
                    .getResultStream()
                    .findFirst();
        }

    }
}
