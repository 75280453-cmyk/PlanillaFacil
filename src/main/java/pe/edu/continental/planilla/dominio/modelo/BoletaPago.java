package pe.edu.continental.planilla.dominio.modelo;

import pe.edu.continental.planilla.dominio.TipoConcepto.excepcion.ReglaPlanillaException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BoletaPago(
        Long idBoleta,
        Trabajador trabajador,
        PeriodoPlanilla periodo,
        BigDecimal remuneracionBruta,
        BigDecimal totalDescuentos,
        BigDecimal netoPagar,
        BigDecimal aporteEssalud,
        LocalDate fechaEmision,
        String rutaPdf,
        List<DetalleBoleta> detalles) {
    public BoletaPago {
        if (trabajador == null || periodo == null) {
            throw new ReglaPlanillaException("Trabajador y período son obligatorios");
        }
        if (remuneracionBruta == null || totalDescuentos == null
                || netoPagar == null || aporteEssalud == null) {
            throw new ReglaPlanillaException("Los importes de la boleta son obligatorios");
        }
        if (remuneracionBruta.subtract(totalDescuentos).compareTo(netoPagar) != 0) {
            throw new ReglaPlanillaException("El neto no coincide con bruta - descuentos");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new ReglaPlanillaException("La boleta debe tener al menos un detalle");
        }
        detalles = List.copyOf(detalles); // copia defensiva: la lista queda inmutable
        // rutaPdf puede ser null hasta que se genere el PDF
    }
}
