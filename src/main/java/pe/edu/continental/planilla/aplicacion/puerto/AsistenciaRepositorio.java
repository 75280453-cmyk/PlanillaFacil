package pe.edu.continental.planilla.aplicacion.puerto;

import pe.edu.continental.planilla.dominio.modelo.AsistenciaMensual;

import java.util.Optional;

public interface AsistenciaRepositorio {
    AsistenciaMensual guardar(AsistenciaMensual asistencia);
    Optional<AsistenciaMensual> buscar(Long idTrabajador, Long idPeriodo);
}
