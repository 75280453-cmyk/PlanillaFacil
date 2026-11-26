package pe.edu.continental.planilla.dominio.modelo;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.edu.continental.planilla.dominio.excepcion.DatoInvalidoException;

@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Long idEmpresa;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "ruc", nullable = false, unique = true)
    private String ruc;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(name = "direccion")
    private String direccion;

    protected Empresa() {
        // exigido por JPA
    }

    public Empresa(Long idEmpresa, String ruc, String razonSocial, String direccion) {
        if (ruc == null || !ruc.matches("\\d{11}")) {
            throw new DatoInvalidoException("El RUC debe tener 11 dígitos");
        }
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new DatoInvalidoException("La razón social es obligatoria");
        }
        this.idEmpresa = idEmpresa;
        this.ruc = ruc;
        this.razonSocial = razonSocial;
        this.direccion = direccion;
    }

    public Long idEmpresa() { return idEmpresa; }
    public String ruc() { return ruc; }
    public String razonSocial() { return razonSocial; }
    public String direccion() { return direccion; }
}