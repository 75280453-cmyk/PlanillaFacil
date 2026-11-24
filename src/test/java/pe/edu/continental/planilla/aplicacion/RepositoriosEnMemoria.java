package pe.edu.continental.planilla.aplicacion;

import pe.edu.continental.planilla.aplicacion.puerto.*;
import pe.edu.continental.planilla.dominio.modelo.*;

import java.time.LocalDate;
import java.util.*;

final class RepositoriosEnMemoria {
    private RepositoriosEnMemoria() {
    }

    static class Trabajadores implements TrabajadorRepositorio {
        private final Map<Long, Trabajador> datos = new LinkedHashMap<>();
        private long secuencia = 0;

        @Override
        public Trabajador guardar(Trabajador t) {
            Long id = t.idTrabajador() != null ? t.idTrabajador() : ++secuencia;
            Trabajador guardado = new Trabajador(id, t.dni(), t.nombres(), t.apellidos(), t.cargo(),
                    t.fechaIngreso(), t.sueldoBasico(), t.sistemaPension(), t.tipoComision(), t.estado());
            datos.put(id, guardado);
            return guardado;
        }

        @Override
        public Optional<Trabajador> buscarPorId(Long id) {
            return Optional.ofNullable(datos.get(id));
        }

        @Override
        public List<Trabajador> listarActivos() {
            return datos.values().stream().filter(Trabajador::estaActivo).toList();
        }
    }

    static class Periodos implements PeriodoRepositorio {
        private final Map<Long, PeriodoPlanilla> datos = new LinkedHashMap<>();
        private long secuencia = 0;

        @Override
        public PeriodoPlanilla guardar(PeriodoPlanilla p) {
            Long id = p.idPeriodo() != null ? p.idPeriodo() : ++secuencia;
            PeriodoPlanilla guardado = new PeriodoPlanilla(id, p.anio(), p.mes(), p.estado());
            datos.put(id, guardado);
            return guardado;
        }

        @Override
        public Optional<PeriodoPlanilla> buscarPorId(Long id) {
            return Optional.ofNullable(datos.get(id));
        }
    }

    static class Asistencias implements AsistenciaRepositorio {
        private final Map<String, AsistenciaMensual> datos = new LinkedHashMap<>();
        private long secuencia = 0;

        private static String clave(Long idTrabajador, Long idPeriodo) {
            return idTrabajador + "-" + idPeriodo;
        }

        @Override
        public AsistenciaMensual guardar(AsistenciaMensual a) {
            Long id = a.idAsistencia() != null ? a.idAsistencia() : ++secuencia;
            AsistenciaMensual guardada = new AsistenciaMensual(id, a.trabajador(), a.periodo(),
                    a.diasFalta(), a.minutosTardanza());
            datos.put(clave(a.trabajador().idTrabajador(), a.periodo().idPeriodo()), guardada);
            return guardada;
        }

        @Override
        public Optional<AsistenciaMensual> buscar(Long idTrabajador, Long idPeriodo) {
            return Optional.ofNullable(datos.get(clave(idTrabajador, idPeriodo)));
        }
    }

    static class Tasas implements TasaPensionRepositorio {
        private record Entrada(SistemaPension sistema, TasaPension tasa) {
        }

        private final List<Entrada> datos = new ArrayList<>();

        /** Método extra de prueba: deja cargada una tasa para un sistema. */
        void agregar(SistemaPension sistema, TasaPension tasa) {
            datos.add(new Entrada(sistema, tasa));
        }

        @Override
        public Optional<TasaPension> buscarVigente(SistemaPension sistema, TipoComision tipoComision,
                                                   LocalDate fecha) {
            return datos.stream()
                    .filter(e -> e.sistema().equals(sistema))
                    .map(Entrada::tasa)
                    .filter(t -> t.tipoComision() == tipoComision && t.vigenteEn(fecha))
                    .findFirst();
        }
    }

    static class Parametros implements ParametroRepositorio {
        private final List<ParametroGeneral> datos = new ArrayList<>();
        private long secuencia = 0;

        @Override
        public ParametroGeneral guardar(ParametroGeneral p) {
            Long id = p.idParametro() != null ? p.idParametro() : ++secuencia;
            ParametroGeneral guardado = new ParametroGeneral(id, p.codigo(), p.valor(),
                    p.descripcion(), p.vigenteDesde());
            datos.add(guardado);
            return guardado;
        }

        @Override
        public Optional<ParametroGeneral> buscarVigente(String codigo, LocalDate fecha) {
            return datos.stream()
                    .filter(p -> p.codigo().equals(codigo) && !p.vigenteDesde().isAfter(fecha))
                    .max(Comparator.comparing(ParametroGeneral::vigenteDesde));
        }
    }

    static class Boletas implements BoletaRepositorio {
        private final List<BoletaPago> datos = new ArrayList<>();
        private long secuencia = 0;

        @Override
        public BoletaPago guardar(BoletaPago b) {
            Long id = b.idBoleta() != null ? b.idBoleta() : ++secuencia;
            BoletaPago guardada = new BoletaPago(id, b.trabajador(), b.periodo(), b.remuneracionBruta(),
                    b.totalDescuentos(), b.netoPagar(), b.aporteEssalud(), b.fechaEmision(),
                    b.rutaPdf(), b.detalles());
            datos.add(guardada);
            return guardada;
        }

        @Override
        public List<BoletaPago> listarPorPeriodo(Long idPeriodo) {
            return datos.stream().filter(b -> b.periodo().idPeriodo().equals(idPeriodo)).toList();
        }

        @Override
        public void eliminarPorPeriodo(Long idPeriodo) {
            datos.removeIf(b -> b.periodo().idPeriodo().equals(idPeriodo));
        }
    }
}
