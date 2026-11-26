package pe.edu.continental.planilla.dominio.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.edu.continental.planilla.dominio.excepcion.TrabajadorInvalidoException;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "trabajador")
public class Trabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_trabajador")
    private Long idTrabajador;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "dni", nullable = false, unique = true)
    private String dni;

    @Column(name = "nombres", nullable = false)
    private String nombres;

    @Column(name = "apellidos", nullable = false)
    private String apellidos;

    @Column(name = "cargo")
    private String cargo;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "sueldo_basico", nullable = false)
    private BigDecimal sueldoBasico;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_sistema_pension")
    private SistemaPension sistemaPension;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comision")
    private TipoComision tipoComision;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoTrabajador estado;

    protected Trabajador() {
        // exigido por JPA
    }

    public Trabajador(Long idTrabajador, String dni, String nombres, String apellidos, String cargo,
                      LocalDate fechaIngreso, BigDecimal sueldoBasico, SistemaPension sistemaPension,
                      TipoComision tipoComision, EstadoTrabajador estado) {
        if (dni == null || !dni.matches("\\d{8}")) {
            throw new TrabajadorInvalidoException("El DNI debe tener 8 dígitos");
        }
        if (nombres == null || nombres.isBlank() || apellidos == null || apellidos.isBlank()) {
            throw new TrabajadorInvalidoException("Nombres y apellidos son obligatorios");
        }
        if (fechaIngreso == null || fechaIngreso.isAfter(LocalDate.now())) {
            throw new TrabajadorInvalidoException("La fecha de ingreso no es válida");
        }
        if (sueldoBasico == null || sueldoBasico.signum() <= 0) {
            throw new TrabajadorInvalidoException("El sueldo básico debe ser mayor que cero");
        }
        if (sistemaPension == null) {
            throw new TrabajadorInvalidoException("El sistema de pensiones es obligatorio");
        }
        if (sistemaPension.esAfp() && tipoComision == null) {
            throw new TrabajadorInvalidoException("Un trabajador en AFP debe tener tipo de comisión");
        }
        if (!sistemaPension.esAfp() && tipoComision != null) {
            throw new TrabajadorInvalidoException("Un trabajador en ONP no lleva tipo de comisión");
        }
        this.idTrabajador = idTrabajador;
        this.dni = dni;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.cargo = cargo;
        this.fechaIngreso = fechaIngreso;
        this.sueldoBasico = sueldoBasico;
        this.sistemaPension = sistemaPension;
        this.tipoComision = tipoComision;
        this.estado = (estado == null) ? EstadoTrabajador.ACTIVO : estado;
    }

    public Long idTrabajador() { return idTrabajador; }
    public String dni() { return dni; }
    public String nombres() { return nombres; }
    public String apellidos() { return apellidos; }
    public String cargo() { return cargo; }
    public LocalDate fechaIngreso() { return fechaIngreso; }
    public BigDecimal sueldoBasico() { return sueldoBasico; }
    public SistemaPension sistemaPension() { return sistemaPension; }
    public TipoComision tipoComision() { return tipoComision; }
    public EstadoTrabajador estado() { return estado; }

    public String nombreCompleto() {
        return nombres + " " + apellidos;
    }

    public boolean estaActivo() {
        return estado == EstadoTrabajador.ACTIVO;
    }

    @Override
    public String toString() {
        return "Trabajador[" + idTrabajador + ", " + dni + ", " + nombreCompleto() + "]";
    }
}