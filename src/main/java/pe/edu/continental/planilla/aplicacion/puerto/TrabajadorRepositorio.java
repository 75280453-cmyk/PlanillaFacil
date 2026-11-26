package pe.edu.continental.planilla.aplicacion.puerto;

import pe.edu.continental.planilla.dominio.modelo.Trabajador;

import java.util.List;
import java.util.Optional;

public interface TrabajadorRepositorio {
    Trabajador guardar(Trabajador trabajador);   // inserta si id == null, actualiza si no
    Optional<Trabajador> buscarPorId(Long id);
    List<Trabajador> listarActivos();
    Optional<Trabajador> buscarPorDni(String dni);
}
