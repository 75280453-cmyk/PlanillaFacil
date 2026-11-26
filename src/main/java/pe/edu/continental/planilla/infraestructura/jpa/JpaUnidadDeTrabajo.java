package pe.edu.continental.planilla.infraestructura.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import pe.edu.continental.planilla.aplicacion.puerto.UnidadDeTrabajo;

import java.util.function.Supplier;

public class JpaUnidadDeTrabajo implements UnidadDeTrabajo {
    private final EntityManagerFactory fabrica;
    private final ThreadLocal<EntityManager> actual = new ThreadLocal<>();

    public JpaUnidadDeTrabajo(EntityManagerFactory fabrica) {
        this.fabrica = fabrica;
    }

    @Override
    public <T> T ejecutar(Supplier<T> trabajo) {
        EntityManager em = fabrica.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        actual.set(em);
        try {
            tx.begin();
            T resultado = trabajo.get();
            tx.commit();
            return resultado;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            actual.remove();
            em.close();
        }
    }

    EntityManager entityManager() {
        EntityManager em = actual.get();
        if (em == null) {
            throw new IllegalStateException("No hay una transacción JPA activa en este hilo");
        }
        return em;
    }
}
