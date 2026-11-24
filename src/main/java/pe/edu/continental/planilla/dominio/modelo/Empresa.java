package pe.edu.continental.planilla.dominio.modelo;

import pe.edu.continental.planilla.dominio.excepcion.DatoInvalidoException;

public record Empresa(Long idEmpresa, String ruc, String razonSocial, String direccion) {
    public Empresa {
        if (ruc == null || !ruc.matches("\\d{11}")) {
            throw new DatoInvalidoException("El RUC debe tener 11 dígitos");
        }
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new DatoInvalidoException("La razón social es obligatoria");
        }
    }
}
