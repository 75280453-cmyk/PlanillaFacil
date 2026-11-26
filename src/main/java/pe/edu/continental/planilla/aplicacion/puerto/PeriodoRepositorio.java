package pe.edu.continental.planilla.aplicacion.puerto;

import pe.edu.continental.planilla.dominio.modelo.PeriodoPlanilla;

import java.util.List;
import java.util.Optional;

public interface PeriodoRepositorio {
    PeriodoPlanilla guardar(PeriodoPlanilla periodo);
    Optional<PeriodoPlanilla> buscarPorId(Long id);
    List<PeriodoPlanilla> listarTodos();
}
