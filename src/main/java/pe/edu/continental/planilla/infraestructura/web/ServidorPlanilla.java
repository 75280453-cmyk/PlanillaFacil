package pe.edu.continental.planilla.infraestructura.web;

import com.sun.net.httpserver.HttpServer;
import pe.edu.continental.planilla.infraestructura.memoria.EntornoDemo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Punto de entrada de la API web: enciende el servidor y lo deja atendiendo pedidos
 * hasta que se detiene el programa. Por ahora usa EntornoDemo (datos en memoria);
 * cuando llegue la base de datos, solo cambia con qué repositorios se arma el servicio.
 *
 * Para probar:  ejecutar main y abrir http://localhost:8080/api/trabajadores
 */
public final class ServidorPlanilla {

    private static final Logger REGISTRO = Logger.getLogger(ServidorPlanilla.class.getName());
    private static final int PUERTO_POR_DEFECTO = 8080;
    private static final int SEGUNDOS_DE_GRACIA = 2;

    private ServidorPlanilla() {
    }

    public static void main(String[] args) throws IOException {
        EntornoDemo entorno = EntornoDemo.crear();

        HttpServer servidor = HttpServer.create(new InetSocketAddress(puerto()), 0);
        servidor.setExecutor(Executors.newVirtualThreadPerTaskExecutor());   // un hilo liviano por pedido
        new ApiPlanilla(entorno.servicio()).registrarEn(servidor);

        // Al detener el programa, se deja terminar los pedidos en curso antes de cerrar.
        Runtime.getRuntime().addShutdownHook(Thread.ofPlatform().unstarted(() -> {
            servidor.stop(SEGUNDOS_DE_GRACIA);
            REGISTRO.info("Servidor detenido");
        }));

        servidor.start();
        REGISTRO.info("PlanillaFácil atendiendo en http://localhost:" + servidor.getAddress().getPort());
    }

    /** Lee el puerto de la variable de entorno PUERTO; si no existe, usa 8080. */
    private static int puerto() {
        String valor = System.getenv("PUERTO");
        return valor == null || valor.isBlank() ? PUERTO_POR_DEFECTO : Integer.parseInt(valor);
    }
}
