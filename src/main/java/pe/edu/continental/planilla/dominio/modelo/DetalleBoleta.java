package pe.edu.continental.planilla.dominio.modelo;

import jakarta.persistence.*;
import pe.edu.continental.planilla.dominio.excepcion.ReglaPlanillaException;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_boleta")
public class DetalleBoleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boleta", nullable = false)
    private BoletaPago boleta;

    @Column(name = "concepto", nullable = false)
    private String concepto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoConcepto tipo;

    @Column(name = "porcentaje")
    private BigDecimal porcentaje;   // puede ser null (ej. descuento por faltas)

    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

    protected DetalleBoleta() {
        // exigido por JPA
    }

    public DetalleBoleta(Long idDetalle, String concepto, TipoConcepto tipo,
                         BigDecimal porcentaje, BigDecimal monto) {
        if (concepto == null || concepto.isBlank()) {
            throw new ReglaPlanillaException("El concepto es obligatorio");
        }
        if (tipo == null) {
            throw new ReglaPlanillaException("El tipo de concepto es obligatorio");
        }
        if (monto == null || monto.signum() < 0) {
            throw new ReglaPlanillaException("El monto no puede ser negativo");
        }
        this.idDetalle = idDetalle;
        this.concepto = concepto;
        this.tipo = tipo;
        this.porcentaje = porcentaje;
        this.monto = monto;
    }

    /** Lo llama BoletaPago al armarse; deja enlazado el detalle con su boleta. */
    void asignarBoleta(BoletaPago boleta) {
        this.boleta = boleta;
    }

    public Long idDetalle() { return idDetalle; }
    public String concepto() { return concepto; }
    public TipoConcepto tipo() { return tipo; }
    public BigDecimal porcentaje() { return porcentaje; }
    public BigDecimal monto() { return monto; }
}