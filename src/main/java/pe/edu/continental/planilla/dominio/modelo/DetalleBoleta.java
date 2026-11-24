package pe.edu.continental.planilla.dominio.modelo;

import pe.edu.continental.planilla.dominio.excepcion.ReglaPlanillaException;

import java.math.BigDecimal;

public record DetalleBoleta(
        Long idDetalle,
        String concepto,
        TipoConcepto tipo,
        BigDecimal porcentaje,
        BigDecimal monto) {
    public DetalleBoleta {
        if (concepto == null || concepto.isBlank()) {
            throw new ReglaPlanillaException("El concepto es obligatorio");
        }
        if (tipo == null) {
            throw new ReglaPlanillaException("El tipo de concepto es obligatorio");
        }
        if (monto == null || monto.signum() < 0) {
            throw new ReglaPlanillaException("El monto no puede ser negativo");
        }
        // porcentaje puede ser null (ej. descuento por faltas, que no es un %)
    }
}
