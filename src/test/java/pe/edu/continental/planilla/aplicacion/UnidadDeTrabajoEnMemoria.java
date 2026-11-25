package pe.edu.continental.planilla.aplicacion;
import pe.edu.continental.planilla.aplicacion.puerto.UnidadDeTrabajo;

import java.util.function.Supplier;

class UnidadDeTrabajoEnMemoria implements UnidadDeTrabajo {

    private int ejecuciones = 0;

    @Override
    public <T> T ejecutar(Supplier<T> trabajo) {
        ejecuciones++;
        return trabajo.get();
    }

    int ejecuciones() {
        return ejecuciones;
    }
}
