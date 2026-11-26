package pe.edu.continental.planilla.aplicacion.puerto;

import pe.edu.continental.planilla.dominio.modelo.BoletaPago;
import pe.edu.continental.planilla.dominio.modelo.Empresa;

import java.util.List;
import java.util.Optional;

public interface BoletaRepositorio {
    BoletaPago guardar(BoletaPago boleta);
    List<BoletaPago> listarPorPeriodo(Long idPeriodo);
    void eliminarPorPeriodo(Long idPeriodo);
    Optional<BoletaPago> buscarPorId(Long idBoleta);
    Optional<Empresa> obtenerEmpresa();
}
