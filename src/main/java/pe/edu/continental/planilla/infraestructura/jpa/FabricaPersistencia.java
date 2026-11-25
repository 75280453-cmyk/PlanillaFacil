package pe.edu.continental.planilla.infraestructura.jpa;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceConfiguration;
import pe.edu.continental.planilla.dominio.modelo.*;

public final class FabricaPersistencia {
    private static final String UNIDAD = "planillafacil";
    private static final String PROVEEDOR = "org.hibernate.jpa.HibernatePersistenceProvider";

    private FabricaPersistencia() {
    }

    public static EntityManagerFactory crear(ConfiguracionBd bd, boolean mostrarSql) {
        return new PersistenceConfiguration(UNIDAD)
                .provider(PROVEEDOR)
                .managedClass(Empresa.class)
                .managedClass(SistemaPension.class)
                .managedClass(TasaPension.class)
                .managedClass(ParametroGeneral.class)
                .managedClass(Trabajador.class)
                .managedClass(PeriodoPlanilla.class)
                .managedClass(AsistenciaMensual.class)
                .managedClass(BoletaPago.class)
                .managedClass(DetalleBoleta.class)
                .property(PersistenceConfiguration.JDBC_URL, bd.url())
                .property(PersistenceConfiguration.JDBC_USER, bd.usuario())
                .property(PersistenceConfiguration.JDBC_PASSWORD, bd.clave())
                // Hibernate no crea ni modifica tablas: comprueba que el mapeo coincide con esquema.sql.
                .property("hibernate.hbm2ddl.auto", "validate")
                .property("hibernate.show_sql", mostrarSql)
                .property("hibernate.format_sql", mostrarSql)
                .createEntityManagerFactory();
    }
}
