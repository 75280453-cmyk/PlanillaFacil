package pe.edu.continental.planilla.aplicacion;

public class TrabajadorNoEncontradoException extends RuntimeException {
    public TrabajadorNoEncontradoException(Long id) {
        super("No existe el trabajador con id " + id);
    }
}
