package pe.edu.continental.planilla.infraestructura.memoria;

import pe.edu.continental.planilla.aplicacion.puerto.UnidadDeTrabajo;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * "Transacción" de práctica: no hay base de datos, así que solo se asegura de que
 * un pedido a la vez toque los datos. El servidor atiende varios pedidos en paralelo
 * y los repositorios en memoria no soportan eso por sí solos.
 */
public class UnidadDeTrabajoEnMemoria implements UnidadDeTrabajo {

    private final ReentrantLock cerrojo = new ReentrantLock();

    @Override
    public <T> T ejecutar(Supplier<T> trabajo) {
        cerrojo.lock();
        try {
            return trabajo.get();
        } finally {
            cerrojo.unlock();
        }
    }
}
