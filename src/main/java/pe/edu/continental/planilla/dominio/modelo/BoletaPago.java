package pe.edu.continental.planilla.dominio.modelo;

import jakarta.persistence.*;
import pe.edu.continental.planilla.dominio.excepcion.ReglaPlanillaException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "boleta_pago")
public class BoletaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_boleta")
    private Long idBoleta;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_trabajador")
    private Trabajador trabajador;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_periodo")
    private PeriodoPlanilla periodo;

    @Column(name = "remuneracion_bruta", nullable = false)
    private BigDecimal remuneracionBruta;

    @Column(name = "total_descuentos", nullable = false)
    private BigDecimal totalDescuentos;

    @Column(name = "neto_pagar", nullable = false)
    private BigDecimal netoPagar;

    @Column(name = "aporte_essalud", nullable = false)
    private BigDecimal aporteEssalud;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "ruta_pdf")
    private String rutaPdf;

    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("idDetalle")
    private List<DetalleBoleta> detalles = new ArrayList<>();

    protected BoletaPago() {
        // exigido por JPA
    }

    public BoletaPago(Long idBoleta, Trabajador trabajador, PeriodoPlanilla periodo,
                      BigDecimal remuneracionBruta, BigDecimal totalDescuentos, BigDecimal netoPagar,
                      BigDecimal aporteEssalud, LocalDate fechaEmision, String rutaPdf,
                      List<DetalleBoleta> detalles) {
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
        this.idBoleta = idBoleta;
        this.trabajador = trabajador;
        this.periodo = periodo;
        this.remuneracionBruta = remuneracionBruta;
        this.totalDescuentos = totalDescuentos;
        this.netoPagar = netoPagar;
        this.aporteEssalud = aporteEssalud;
        this.fechaEmision = fechaEmision;
        this.rutaPdf = rutaPdf;
        this.detalles = new ArrayList<>(detalles);
        this.detalles.forEach(d -> d.asignarBoleta(this));
    }

    public Long idBoleta() { return idBoleta; }
    public Trabajador trabajador() { return trabajador; }
    public PeriodoPlanilla periodo() { return periodo; }
    public BigDecimal remuneracionBruta() { return remuneracionBruta; }
    public BigDecimal totalDescuentos() { return totalDescuentos; }
    public BigDecimal netoPagar() { return netoPagar; }
    public BigDecimal aporteEssalud() { return aporteEssalud; }
    public LocalDate fechaEmision() { return fechaEmision; }
    public String rutaPdf() { return rutaPdf; }

    public List<DetalleBoleta> detalles() {
        return Collections.unmodifiableList(detalles);
    }

    /** Para cuando se genera el PDF y se guarda su ruta. */
    public void asignarRutaPdf(String ruta) {
        this.rutaPdf = ruta;
    }
}