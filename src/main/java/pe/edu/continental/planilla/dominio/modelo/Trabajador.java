package pe.edu.continental.planilla.dominio.modelo;

import pe.edu.continental.planilla.dominio.TipoConcepto.excepcion.TrabajadorInvalidoException;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Trabajador(Long idTrabajador,
                         String dni,
                         String nombres,
                         String apellidos,
                         String cargo,
                         LocalDate fechaIngreso,
                         BigDecimal sueldoBasico,
                         SistemaPension sistemaPension,
                         TipoComision tipoComision,
                         EstadoTrabajador estado) {
    public Trabajador {
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
        if (estado == null) {
            estado = EstadoTrabajador.ACTIVO;
        }
    }

    public String nombreCompleto() {
        return nombres + " " + apellidos;
    }

    public boolean estaActivo() {
        return estado == EstadoTrabajador.ACTIVO;
    }
}
