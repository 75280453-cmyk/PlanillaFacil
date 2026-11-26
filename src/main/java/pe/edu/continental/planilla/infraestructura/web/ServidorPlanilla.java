package pe.edu.continental.planilla.infraestructura.web;

import com.sun.net.httpserver.HttpServer;
import jakarta.persistence.EntityManagerFactory;
import pe.edu.continental.planilla.aplicacion.ServicioPlanilla;
import pe.edu.continental.planilla.dominio.CalculadoraPlanilla;
import pe.edu.continental.planilla.infraestructura.jpa.ConfiguracionBd;
import pe.edu.continental.planilla.infraestructura.jpa.FabricaPersistencia;
import pe.edu.continental.planilla.infraestructura.jpa.JpaUnidadDeTrabajo;
import pe.edu.continental.planilla.infraestructura.jpa.RepositoriosJpa;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Clock;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Punto de entrada de la API web: enciende el servidor conectado a PostgreSQL
 * y lo deja atendiendo pedidos hasta que se detiene el programa.
 *
 * Para probar: levantar PostgreSQL y abrir http://localhost:8080/api/trabajadores
 */
public final class ServidorPlanilla {

    private static final Logger REGISTRO = Logger.getLogger(ServidorPlanilla.class.getName());
    private static final int PUERTO_POR_DEFECTO = 8080;
    private static final int SEGUNDOS_DE_GRACIA = 2;

    private ServidorPlanilla() {
    }

    public static void main(String[] args) throws IOException {
        ConfiguracionBd config = ConfiguracionBd.desdeEntorno();
        EntityManagerFactory fabrica = FabricaPersistencia.crear(config, false);
        JpaUnidadDeTrabajo unidad = new JpaUnidadDeTrabajo(fabrica);

        ServicioPlanilla servicio = new ServicioPlanilla(
                new RepositoriosJpa.Trabajadores(unidad),
                new RepositoriosJpa.Periodos(unidad),
                new RepositoriosJpa.Asistencias(unidad),
                new RepositoriosJpa.Tasas(unidad),
                new RepositoriosJpa.Parametros(unidad),
                new RepositoriosJpa.Boletas(unidad),
                new CalculadoraPlanilla(),
                unidad,
                Clock.systemDefaultZone()
        );

        HttpServer servidor = HttpServer.create(new InetSocketAddress(puerto()), 0);
        servidor.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        new ApiPlanilla(servicio).registrarEn(servidor);

        Runtime.getRuntime().addShutdownHook(Thread.ofPlatform().unstarted(() -> {
            servidor.stop(SEGUNDOS_DE_GRACIA);
            fabrica.close();
            REGISTRO.info("Servidor y conexión a base de datos detenidos");
        }));

        servidor.start();
        REGISTRO.info("PlanillaFácil atendiendo en http://localhost:" + servidor.getAddress().getPort());
    }

    private static int puerto() {
        String valor = System.getenv("PUERTO");
        return valor == null || valor.isBlank() ? PUERTO_POR_DEFECTO : Integer.parseInt(valor);
    }
}
