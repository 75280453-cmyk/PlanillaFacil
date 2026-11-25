package pe.edu.continental.planilla.dominio.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pe.edu.continental.planilla.dominio.excepcion.DatoInvalidoException;

@Entity
@Table(name = "sistema_pension")
public class SistemaPension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sistema_pension")
    private Long idSistemaPension;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoSistemaPension tipo;

    protected SistemaPension() {
        // exigido por JPA
    }

    public SistemaPension(Long idSistemaPension, String nombre, TipoSistemaPension tipo) {
        if (nombre == null || nombre.isBlank()) {
            throw new DatoInvalidoException("El nombre del sistema de pensión es obligatorio");
        }
        if (tipo == null) {
            throw new DatoInvalidoException("El tipo de sistema de pensión es obligatorio");
        }
        this.idSistemaPension = idSistemaPension;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public Long idSistemaPension() {
        return idSistemaPension;
    }

    public String nombre() {
        return nombre;
    }

    public TipoSistemaPension tipo() {
        return tipo;
    }

    public boolean esAfp() {
        return tipo == TipoSistemaPension.AFP;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SistemaPension otro)) return false;
        return idSistemaPension != null && idSistemaPension.equals(otro.idSistemaPension);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SistemaPension[" + idSistemaPension + ", " + nombre + ", " + tipo + "]";
    }
}