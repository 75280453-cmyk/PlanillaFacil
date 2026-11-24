package pe.edu.continental.planilla.aplicacion.puerto;

import pe.edu.continental.planilla.dominio.modelo.SistemaPension;
import pe.edu.continental.planilla.dominio.modelo.TasaPension;
import pe.edu.continental.planilla.dominio.modelo.TipoComision;

import java.time.LocalDate;
import java.util.Optional;

public interface TasaPensionRepositorio {
    Optional<TasaPension> buscarVigente(SistemaPension sistema, TipoComision tipoComision, LocalDate fecha);
}
