package pe.edu.continental.planilla.infraestructura.web;

import pe.edu.continental.planilla.dominio.modelo.Trabajador;

import java.math.BigDecimal;

/**
 * Ficha resumida de un trabajador, pensada para viajar como JSON.
 * Solo tiene datos simples (texto y números): nada de objetos internos del dominio.
 */
public record TrabajadorDto(
        Long id,
        String dni,
        String nombres,
        String apellidos,
        String cargo,
        String fechaIngreso,      // "2024-03-01"
        BigDecimal sueldoBasico,
        String sistemaPension,    // "ONP" o "AFP"
        String entidadPension,    // "ONP" o "AFP Integra"
        String tipoComision,      // "FLUJO", "MIXTA" o null si es ONP
        String estado) {          // "ACTIVO" o "CESADO"

    public static TrabajadorDto de(Trabajador t) {
        return new TrabajadorDto(
                t.idTrabajador(),
                t.dni(),
                t.nombres(),
                t.apellidos(),
                t.cargo(),
                t.fechaIngreso().toString(),
                t.sueldoBasico(),
                t.sistemaPension().tipo().name(),
                t.sistemaPension().nombre(),
                t.tipoComision() == null ? null : t.tipoComision().name(),
                t.estado().name());
    }
}
