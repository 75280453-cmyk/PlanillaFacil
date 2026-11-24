package pe.edu.continental.planilla.dominio;

import pe.edu.continental.planilla.dominio.excepcion.ReglaPlanillaException;
import pe.edu.continental.planilla.dominio.modelo.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalculadoraPlanilla {
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final BigDecimal DIAS_MES = BigDecimal.valueOf(30);
    private static final BigDecimal MINUTOS_MES = BigDecimal.valueOf(30 * 8 * 60);

    // ---------- Cálculos sueltos ----------

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

    public BigDecimal descuentoPorFaltas(BigDecimal sueldo, int diasFalta) {
        return sueldo.multiply(BigDecimal.valueOf(diasFalta))
                .divide(DIAS_MES, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal descuentoPorTardanza(BigDecimal sueldo, int minutosTardanza) {
        return sueldo.multiply(BigDecimal.valueOf(minutosTardanza))
                .divide(MINUTOS_MES, 2, RoundingMode.HALF_UP);
    }

    // ---------- Boleta completa ----------

    /**
     * @param tasaAfp   tasa vigente del AFP del trabajador (se ignora si está en ONP, puede ser null)
     * @param pctOnp    porcentaje de ONP, por ejemplo 13
     * @param pctEssalud porcentaje de EsSalud, por ejemplo 9
     */
    public BoletaPago calcularBoleta(AsistenciaMensual asistencia,
                                     TasaPension tasaAfp,
                                     BigDecimal pctOnp,
                                     BigDecimal pctEssalud,
                                     LocalDate fechaEmision) {
        Trabajador trabajador = asistencia.trabajador();
        PeriodoPlanilla periodo = asistencia.periodo();

        if (!periodo.estaAbierto()) {
            throw new ReglaPlanillaException("El período está cerrado, no se puede calcular");
        }
        if (!trabajador.estaActivo()) {
            throw new ReglaPlanillaException("El trabajador no está activo");
        }

        BigDecimal sueldo = trabajador.sueldoBasico().setScale(2, RoundingMode.HALF_UP);
        BigDecimal dctoFaltas = descuentoPorFaltas(sueldo, asistencia.diasFalta());
        BigDecimal dctoTardanza = descuentoPorTardanza(sueldo, asistencia.minutosTardanza());
        BigDecimal base = sueldo.subtract(dctoFaltas).subtract(dctoTardanza);

        if (base.signum() < 0) {
            throw new ReglaPlanillaException("Los descuentos por asistencia superan el sueldo");
        }

        List<DetalleBoleta> detalles = new ArrayList<>();
        detalles.add(new DetalleBoleta(null, "Sueldo básico", TipoConcepto.INGRESO, null, sueldo));

        BigDecimal totalDescuentos = BigDecimal.ZERO.setScale(2);

        if (dctoFaltas.signum() > 0) {
            detalles.add(new DetalleBoleta(null, "Descuento por faltas", TipoConcepto.DESCUENTO, null, dctoFaltas));
            totalDescuentos = totalDescuentos.add(dctoFaltas);
        }
        if (dctoTardanza.signum() > 0) {
            detalles.add(new DetalleBoleta(null, "Descuento por tardanzas", TipoConcepto.DESCUENTO, null, dctoTardanza));
            totalDescuentos = totalDescuentos.add(dctoTardanza);
        }

        if (trabajador.sistemaPension().esAfp()) {
            if (tasaAfp == null || tasaAfp.tipoComision() != trabajador.tipoComision()) {
                throw new ReglaPlanillaException("No hay una tasa AFP que coincida con el tipo de comisión del trabajador");
            }
            BigDecimal aporte = porcentaje(base, tasaAfp.pctAporte());
            BigDecimal prima = porcentaje(base, tasaAfp.pctPrima());
            BigDecimal comision = porcentaje(base, tasaAfp.pctComision());

            detalles.add(new DetalleBoleta(null, "AFP aporte obligatorio", TipoConcepto.DESCUENTO, tasaAfp.pctAporte(), aporte));
            detalles.add(new DetalleBoleta(null, "AFP prima de seguro", TipoConcepto.DESCUENTO, tasaAfp.pctPrima(), prima));
            detalles.add(new DetalleBoleta(null, "AFP comisión", TipoConcepto.DESCUENTO, tasaAfp.pctComision(), comision));
            totalDescuentos = totalDescuentos.add(aporte).add(prima).add(comision);
        } else {
            BigDecimal onp = descuentoOnp(base, pctOnp);
            detalles.add(new DetalleBoleta(null, "ONP", TipoConcepto.DESCUENTO, pctOnp, onp));
            totalDescuentos = totalDescuentos.add(onp);
        }

        BigDecimal essalud = aporteEssalud(base, pctEssalud);
        detalles.add(new DetalleBoleta(null, "EsSalud (aporte del empleador)",
                TipoConcepto.APORTE_EMPLEADOR, pctEssalud, essalud));

        BigDecimal neto = sueldo.subtract(totalDescuentos);

        return new BoletaPago(null, trabajador, periodo, sueldo, totalDescuentos,
                neto, essalud, fechaEmision, null, detalles);
    }

    private BigDecimal porcentaje(BigDecimal base, BigDecimal pct) {
        return base.multiply(pct).divide(CIEN, 2, RoundingMode.HALF_UP);
    }
}
