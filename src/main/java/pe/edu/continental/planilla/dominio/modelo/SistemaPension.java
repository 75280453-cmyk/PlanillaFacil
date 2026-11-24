package pe.edu.continental.planilla.dominio.modelo;

import pe.edu.continental.planilla.dominio.excepcion.DatoInvalidoException;

public record SistemaPension(Long idSistemaPension, String nombre, TipoSistemaPension tipo) {
    public SistemaPension {
        if (nombre == null || nombre.isBlank()) {
            throw new DatoInvalidoException("El nombre del sistema de pensión es obligatorio");
        }
        if (tipo == null) {
            throw new DatoInvalidoException("El tipo de sistema de pensión es obligatorio");
        }
    }

    public boolean esAfp() {
        return tipo == TipoSistemaPension.AFP;
    }
}
