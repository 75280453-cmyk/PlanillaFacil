package pe.edu.continental.planilla.dominio.modelo;

import jakarta.persistence.*;
import pe.edu.continental.planilla.dominio.excepcion.ReglaPlanillaException;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "parametro_general")
public class ParametroGeneral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametro")
    private Long idParametro;

    @Column(name = "codigo", nullable = false)
    private String codigo;

    @Column(name = "valor", nullable = false)
    private BigDecimal valor;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;

    protected ParametroGeneral() {
        // exigido por JPA
    }

    public ParametroGeneral(Long idParametro, String codigo, BigDecimal valor,
                            String descripcion, LocalDate vigenteDesde) {
        if (codigo == null || codigo.isBlank()) {
            throw new ReglaPlanillaException("El código del parámetro es obligatorio");
        }
        if (valor == null || valor.signum() < 0) {
            throw new ReglaPlanillaException("El valor del parámetro no puede ser negativo");
        }
        this.idParametro = idParametro;
        this.codigo = codigo;
        this.valor = valor;
        this.descripcion = descripcion;
        this.vigenteDesde = vigenteDesde;
    }

    public Long idParametro() { return idParametro; }
    public String codigo() { return codigo; }
    public BigDecimal valor() { return valor; }
    public String descripcion() { return descripcion; }
    public LocalDate vigenteDesde() { return vigenteDesde; }
}