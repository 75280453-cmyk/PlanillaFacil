package pe.edu.continental.planilla.aplicacion.puerto;

import pe.edu.continental.planilla.dominio.modelo.ParametroGeneral;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ParametroRepositorio {
    Optional<ParametroGeneral> buscarVigente(String codigo, LocalDate fecha);
    ParametroGeneral guardar(ParametroGeneral parametro);
    List<ParametroGeneral> listarTodos();
}
