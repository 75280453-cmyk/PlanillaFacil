package pe.edu.continental.planilla.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculadoraPlanilla {
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    public BigDecimal descuentoAfp(BigDecimal base, BigDecimal pctAporte,
                                   BigDecimal pctPrima, BigDecimal pctComision) {
        return porcentaje(base, pctAporte)
                .add(porcentaje(base, pctPrima))
                .add(porcentaje(base, pctComision));
    }

    public BigDecimal descuentoOnp(BigDecimal base, BigDecimal pctOnp) {
        return porcentaje(base, pctOnp);
    }

    public BigDecimal aporteEssalud(BigDecimal base, BigDecimal pctEssalud) {
        return porcentaje(base, pctEssalud);
    }

    private BigDecimal porcentaje(BigDecimal base, BigDecimal pct) {
        return base.multiply(pct).divide(CIEN, 2, RoundingMode.HALF_UP);
    }
}
