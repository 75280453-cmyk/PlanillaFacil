package pe.edu.continental.planilla.aplicacion.puerto;

import pe.edu.continental.planilla.dominio.modelo.BoletaPago;

import java.util.List;

public interface BoletaRepositorio {
    BoletaPago guardar(BoletaPago boleta);
    List<BoletaPago> listarPorPeriodo(Long idPeriodo);
    void eliminarPorPeriodo(Long idPeriodo);
}
