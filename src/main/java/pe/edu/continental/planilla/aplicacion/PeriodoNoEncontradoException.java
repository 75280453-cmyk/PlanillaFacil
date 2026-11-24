package pe.edu.continental.planilla.aplicacion;

import javax.management.RuntimeErrorException;

public class PeriodoNoEncontradoException extends RuntimeException {
    public PeriodoNoEncontradoException(Long id) {
        super("No existe el período con id " + id);
    }
}
