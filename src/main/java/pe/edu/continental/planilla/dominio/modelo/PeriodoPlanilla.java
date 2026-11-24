package pe.edu.continental.planilla.dominio.modelo;

import pe.edu.continental.planilla.dominio.excepcion.DatoInvalidoException;

public record PeriodoPlanilla(Long idPeriodo, int anio, int mes, EstadoPeriodo estado) {
    public PeriodoPlanilla {
        if (anio < 2000 || anio > 2100) {
            throw new DatoInvalidoException("El año del período no es válido");
        }
        if (mes < 1 || mes > 12) {
            throw new DatoInvalidoException("El mes debe estar entre 1 y 12");
        }
        if (estado == null) {
            estado = EstadoPeriodo.ABIERTO;
        }
    }

    public boolean estaAbierto() {
        return estado == EstadoPeriodo.ABIERTO;
    }
}
