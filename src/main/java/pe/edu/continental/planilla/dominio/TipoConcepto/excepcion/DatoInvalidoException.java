package pe.edu.continental.planilla.dominio.TipoConcepto.excepcion;

public class DatoInvalidoException extends RuntimeException {
    public DatoInvalidoException(String message) {
        super(message);
    }
}
