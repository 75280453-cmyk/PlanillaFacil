package pe.edu.continental.planilla.dominio.modelo;

import jakarta.persistence.*;
import pe.edu.continental.planilla.dominio.excepcion.ReglaPlanillaException;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Porcentajes expresados como 10 = 10 %, 1.37 = 1.37 %. */
@Entity
@Table(name = "tasa_pension")
public class TasaPension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tasa")
    private Long idTasa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_sistema_pension")
    private SistemaPension sistemaPension;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comision", nullable = false)
    private TipoComision tipoComision;

    @Column(name = "pct_aporte", nullable = false)
    private BigDecimal pctAporte;

    @Column(name = "pct_prima", nullable = false)
    private BigDecimal pctPrima;

    @Column(name = "pct_comision", nullable = false)
    private BigDecimal pctComision;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;

    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;

    protected TasaPension() {
        // exigido por JPA
    }

    /** Constructor sin sistema (lo usan los tests de la calculadora). */
    public TasaPension(Long idTasa, TipoComision tipoComision, BigDecimal pctAporte,
                       BigDecimal pctPrima, BigDecimal pctComision,
                       LocalDate vigenteDesde, LocalDate vigenteHasta) {
        this(idTasa, null, tipoComision, pctAporte, pctPrima, pctComision, vigenteDesde, vigenteHasta);
    }

    public TasaPension(Long idTasa, SistemaPension sistemaPension, TipoComision tipoComision,
                       BigDecimal pctAporte, BigDecimal pctPrima, BigDecimal pctComision,
                       LocalDate vigenteDesde, LocalDate vigenteHasta) {
        validarPorcentaje(pctAporte, "aporte");
        validarPorcentaje(pctPrima, "prima");
        validarPorcentaje(pctComision, "comisión");
        if (vigenteDesde == null) {
            throw new ReglaPlanillaException("La fecha de inicio de vigencia es obligatoria");
        }
        if (vigenteHasta != null && vigenteHasta.isBefore(vigenteDesde)) {
            throw new ReglaPlanillaException("La vigencia final no puede ser anterior a la inicial");
        }
        this.idTasa = idTasa;
        this.sistemaPension = sistemaPension;
        this.tipoComision = tipoComision;
        this.pctAporte = pctAporte;
        this.pctPrima = pctPrima;
        this.pctComision = pctComision;
        this.vigenteDesde = vigenteDesde;
        this.vigenteHasta = vigenteHasta;
    }

    private static void validarPorcentaje(BigDecimal valor, String nombre) {
        if (valor == null || valor.signum() < 0 || valor.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ReglaPlanillaException("El porcentaje de " + nombre + " debe estar entre 0 y 100");
        }
    }

    public Long idTasa() { return idTasa; }
    public SistemaPension sistemaPension() { return sistemaPension; }
    public TipoComision tipoComision() { return tipoComision; }
    public BigDecimal pctAporte() { return pctAporte; }
    public BigDecimal pctPrima() { return pctPrima; }
    public BigDecimal pctComision() { return pctComision; }
    public LocalDate vigenteDesde() { return vigenteDesde; }
    public LocalDate vigenteHasta() { return vigenteHasta; }

    public BigDecimal pctTotal() {
        return pctAporte.add(pctPrima).add(pctComision);
    }

    public boolean vigenteEn(LocalDate fecha) {
        return !fecha.isBefore(vigenteDesde) && (vigenteHasta == null || !fecha.isAfter(vigenteHasta));
    }
}