package pe.edu.continental.planilla.dominio.modelo;

import pe.edu.continental.planilla.dominio.excepcion.ReglaPlanillaException;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TasaPension(
        Long idTasa,
        TipoComision tipoComision,
        BigDecimal pctAporte,
        BigDecimal pctPrima,
        BigDecimal pctComision,
        LocalDate vigenteDesde,
        LocalDate vigenteHasta) {
    public TasaPension {
        validarPorcentaje(pctAporte, "aporte");
        validarPorcentaje(pctPrima, "prima");
        validarPorcentaje(pctComision, "comisión");
        if (vigenteDesde == null) {
            throw new ReglaPlanillaException("La fecha de inicio de vigencia es obligatoria");
        }
        if (vigenteHasta != null && vigenteHasta.isBefore(vigenteDesde)) {
            throw new ReglaPlanillaException("La vigencia final no puede ser anterior a la inicial");
        }
    }

    private static void validarPorcentaje(BigDecimal valor, String nombre) {
        if (valor == null || valor.signum() < 0 || valor.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ReglaPlanillaException("El porcentaje de " + nombre + " debe estar entre 0 y 100");
        }
    }

    public BigDecimal pctTotal() {
        return pctAporte.add(pctPrima).add(pctComision);
    }

    public boolean vigenteEn(LocalDate fecha) {
        return !fecha.isBefore(vigenteDesde) && (vigenteHasta == null || !fecha.isAfter(vigenteHasta));
    }
}
