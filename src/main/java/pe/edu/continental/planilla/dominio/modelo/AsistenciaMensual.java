package pe.edu.continental.planilla.dominio.modelo;

import jakarta.persistence.*;
import pe.edu.continental.planilla.dominio.excepcion.AsistenciaInvalidaException;

@Entity
@Table(name = "asistencia_mensual")
public class AsistenciaMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asistencia")
    private Long idAsistencia;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_trabajador")
    private pe.edu.continental.planilla.dominio.modelo.Trabajador trabajador;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_periodo")
    private PeriodoPlanilla periodo;

    @Column(name = "dias_falta", nullable = false)
    private int diasFalta;

    @Column(name = "minutos_tardanza", nullable = false)
    private int minutosTardanza;

    protected AsistenciaMensual() {
        // exigido por JPA
    }

    public AsistenciaMensual(Long idAsistencia, pe.edu.continental.planilla.dominio.modelo.Trabajador trabajador, PeriodoPlanilla periodo,
                             int diasFalta, int minutosTardanza) {
        if (trabajador == null || periodo == null) {
            throw new AsistenciaInvalidaException("Trabajador y período son obligatorios");
        }
        if (diasFalta < 0 || diasFalta > 31) {
            throw new AsistenciaInvalidaException("Los días de falta deben estar entre 0 y 31");
        }
        if (minutosTardanza < 0) {
            throw new AsistenciaInvalidaException("Los minutos de tardanza no pueden ser negativos");
        }
        this.idAsistencia = idAsistencia;
        this.trabajador = trabajador;
        this.periodo = periodo;
        this.diasFalta = diasFalta;
        this.minutosTardanza = minutosTardanza;
    }

    public Long idAsistencia() { return idAsistencia; }
    public Trabajador trabajador() { return trabajador; }
    public PeriodoPlanilla periodo() { return periodo; }
    public int diasFalta() { return diasFalta; }
    public int minutosTardanza() { return minutosTardanza; }
}