package pe.edu.continental.planilla.infraestructura.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pe.edu.continental.planilla.aplicacion.PeriodoNoEncontradoException;
import pe.edu.continental.planilla.aplicacion.ServicioPlanilla;
import pe.edu.continental.planilla.aplicacion.TrabajadorNoEncontradoException;
import pe.edu.continental.planilla.dominio.excepcion.DatoInvalidoException;
import pe.edu.continental.planilla.dominio.excepcion.ReglaPlanillaException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adaptador de entrada HTTP (el "recepcionista"): recibe pedidos del navegador,
 * se los pasa al ServicioPlanilla y devuelve la respuesta en JSON.
 * Traduce las excepciones del sistema a códigos de estado HTTP.
 *
 *   GET /api/salud
 *   GET /api/trabajadores
 */
public final class ApiPlanilla {

    private static final Logger REGISTRO = Logger.getLogger(ApiPlanilla.class.getName());
    private static final int OK = 200;
    private static final int SOLICITUD_INVALIDA = 400;
    private static final int NO_ENCONTRADO = 404;
    private static final int METODO_NO_PERMITIDO = 405;
    private static final int REGLA_INCUMPLIDA = 422;
    private static final int ERROR_INTERNO = 500;

    private final ServicioPlanilla servicio;

    public ApiPlanilla(ServicioPlanilla servicio) {
        this.servicio = servicio;
    }

    /** Anota en el servidor qué hacer con cada dirección. */
    public void registrarEn(HttpServer servidor) {
        servidor.createContext("/api/salud", ruta("/api/salud", "GET",
                () -> new Respuesta(OK, Json.mensaje("estado", "ok"))));
        servidor.createContext("/api/trabajadores", ruta("/api/trabajadores", "GET",
                this::listarTrabajadores));
        // Cualquier otra dirección: respuesta JSON en vez de la página HTML por defecto del servidor.
        servidor.createContext("/", intercambio -> enviar(intercambio, error(NO_ENCONTRADO, "No existe la ruta")));
    }

    private Respuesta listarTrabajadores() {
        var fichas = servicio.listarTrabajadoresActivos().stream().map(TrabajadorDto::de).toList();
        return new Respuesta(OK, Json.lista(fichas, Json::de));
    }

    // ---------- Mecánica común a todas las rutas ----------

    /**
     * Crea el manejador de una dirección. Revisa que la dirección sea exactamente la esperada
     * y que el método (GET, POST...) sea el correcto; luego ejecuta la acción.
     */
    private static HttpHandler ruta(String direccion, String metodo, Supplier<Respuesta> accion) {
        return intercambio -> {
            Respuesta respuesta;
            if (!direccion.equals(intercambio.getRequestURI().getPath())) {
                respuesta = error(NO_ENCONTRADO, "No existe la ruta");
            } else if (!metodo.equals(intercambio.getRequestMethod())) {
                respuesta = error(METODO_NO_PERMITIDO, "Use " + metodo);
            } else {
                respuesta = atender(accion);
            }
            enviar(intercambio, respuesta);
        };
    }

    /** Ejecuta la acción y convierte cualquier excepción en un código de error con mensaje. */
    private static Respuesta atender(Supplier<Respuesta> accion) {
        try {
            return accion.get();
        } catch (TrabajadorNoEncontradoException | PeriodoNoEncontradoException e) {
            return error(NO_ENCONTRADO, e.getMessage());
        } catch (ReglaPlanillaException e) {          // va antes que DatoInvalido: es su hija
            return error(REGLA_INCUMPLIDA, e.getMessage());
        } catch (DatoInvalidoException | IllegalArgumentException e) {
            return error(SOLICITUD_INVALIDA, e.getMessage());
        } catch (RuntimeException e) {
            REGISTRO.log(Level.SEVERE, "Error no previsto", e);
            return error(ERROR_INTERNO, "Error interno del servidor");
        }
    }

    private static Respuesta error(int estado, String mensaje) {
        return new Respuesta(estado, Json.mensaje("error", mensaje));
    }

    private static void enviar(HttpExchange intercambio, Respuesta respuesta) throws IOException {
        byte[] cuerpo = respuesta.cuerpo().getBytes(StandardCharsets.UTF_8);
        intercambio.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        intercambio.sendResponseHeaders(respuesta.estado(), cuerpo.length);
        try (OutputStream salida = intercambio.getResponseBody()) {
            salida.write(cuerpo);
        }
    }
}