package pe.edu.continental.planilla.dominio.modelo;

import jakarta.persistence.*;
import pe.edu.continental.planilla.dominio.excepcion.DatoInvalidoException;

@Entity
@Table(name = "periodo_planilla")
public class PeriodoPlanilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_periodo")
    private Long idPeriodo;

    @Column(name = "anio", nullable = false)
    private int anio;

    @Column(name = "mes", nullable = false)
    private int mes;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPeriodo estado;

    protected PeriodoPlanilla() {
        // exigido por JPA
    }

    public PeriodoPlanilla(Long idPeriodo, int anio, int mes, EstadoPeriodo estado) {
        if (anio < 2000 || anio > 2100) {
            throw new DatoInvalidoException("El año del período no es válido");
        }
        if (mes < 1 || mes > 12) {
            throw new DatoInvalidoException("El mes debe estar entre 1 y 12");
        }
        this.idPeriodo = idPeriodo;
        this.anio = anio;
        this.mes = mes;
        this.estado = (estado == null) ? EstadoPeriodo.ABIERTO : estado;
    }

    public Long idPeriodo() { return idPeriodo; }
    public int anio() { return anio; }
    public int mes() { return mes; }
    public EstadoPeriodo estado() { return estado; }

    public boolean estaAbierto() {
        return estado == EstadoPeriodo.ABIERTO;
    }
}