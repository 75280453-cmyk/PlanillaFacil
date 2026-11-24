package pe.edu.continental.planilla.dominio.modelo;

import pe.edu.continental.planilla.dominio.excepcion.AsistenciaInvalidaException;

public record AsistenciaMensual(
        Long idAsistencia,
        Trabajador trabajador,
        PeriodoPlanilla periodo,
        int diasFalta,
        int minutosTardanza) {
    public AsistenciaMensual {
        if (trabajador == null || periodo == null) {
            throw new AsistenciaInvalidaException("Trabajador y período son obligatorios");
        }
        if (diasFalta < 0 || diasFalta > 31) {
            throw new AsistenciaInvalidaException("Los días de falta deben estar entre 0 y 31");
        }
        if (minutosTardanza < 0) {
            throw new AsistenciaInvalidaException("Los minutos de tardanza no pueden ser negativos");
        }
    }
}
