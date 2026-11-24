package pe.edu.continental.planilla.aplicacion;

import java.math.BigDecimal;

public record ResumenPlanilla(
        int anio,
        int mes,
        int cantidadBoletas,
        BigDecimal totalRemuneraciones,
        BigDecimal totalDescuentos,
        BigDecimal totalNeto,
        BigDecimal totalEssalud) {
}
