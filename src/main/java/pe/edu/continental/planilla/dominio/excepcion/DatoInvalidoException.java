package pe.edu.continental.planilla.dominio.excepcion;

public class DatoInvalidoException extends RuntimeException {
    public DatoInvalidoException(String message) {
        super(message);
    }
}
