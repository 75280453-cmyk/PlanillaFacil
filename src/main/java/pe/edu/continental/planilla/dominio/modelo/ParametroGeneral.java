package pe.edu.continental.planilla.dominio.modelo;

import pe.edu.continental.planilla.dominio.excepcion.ReglaPlanillaException;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParametroGeneral(
        Long idParametro,
        String codigo,
        BigDecimal valor,
        String descripcion,
        LocalDate vigenteDesde) {
    public ParametroGeneral {
        if (codigo == null || codigo.isBlank()) {
            throw new ReglaPlanillaException("El código del parámetro es obligatorio");
        }
        if (valor == null || valor.signum() < 0) {
            throw new ReglaPlanillaException("El valor del parámetro no puede ser negativo");
        }
    }
}
