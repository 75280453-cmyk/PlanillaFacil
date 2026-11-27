package pe.edu.continental.planilla.infraestructura.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pe.edu.continental.planilla.aplicacion.PeriodoNoEncontradoException;
import pe.edu.continental.planilla.aplicacion.ResumenPlanilla;
import pe.edu.continental.planilla.aplicacion.ServicioPlanilla;
import pe.edu.continental.planilla.aplicacion.TrabajadorNoEncontradoException;
import pe.edu.continental.planilla.dominio.excepcion.DatoInvalidoException;
import pe.edu.continental.planilla.dominio.excepcion.ReglaPlanillaException;
import pe.edu.continental.planilla.dominio.modelo.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adaptador de entrada HTTP (el "recepcionista"): recibe pedidos del navegador,
 * valida la sesión mediante token HMAC-SHA256 y delega al ServicioPlanilla.
 */
public final class ApiPlanilla {

 private static final Logger REGISTRO = Logger.getLogger(ApiPlanilla.class.getName());
 private static final int OK = 200;
 private static final int CREADO = 201;
 private static final int SOLICITUD_INVALIDA = 400;
 private static final int NO_AUTORIZADO = 401;
 private static final int NO_ENCONTRADO = 404;
 private static final int METODO_NO_PERMITIDO = 405;
 private static final int REGLA_INCUMPLIDA = 422;
 private static final int ERROR_INTERNO = 500;

 // Clave secreta para firmar tokens de sesión (HMAC-SHA256)
 private static final String CLAVE_SECRETA_TOKEN = System.getenv()
         .getOrDefault("JWT_SECRET", "PlanillaFacil_VIP_SecretKey_2026_Continental");
 private static final long DURACION_TOKEN_SEGUNDOS = 8 * 3600L; // 8 horas

 // Credenciales autorizadas de empleadores/administradores
 private static final Map<String, CredencialUsuario> USUARIOS = Map.of(
         "admin@planillafacil.pe", new CredencialUsuario("vip2026", "Administrador General", "ADMIN"),
         "gabriel@planillafacil.pe", new CredencialUsuario("123456", "Gabriel Ramos", "ADMIN")
 );

 private record CredencialUsuario(String clave, String nombre, String rol) {}

 private final ServicioPlanilla servicio;

 public ApiPlanilla(ServicioPlanilla servicio) {
  this.servicio = servicio;
 }

 public void registrarEn(HttpServer servidor) {
  servidor.createContext("/api/salud", rutaExacta("/api/salud", "GET",
          ex -> new Respuesta(OK, Json.mensaje("estado", "ok"))));

  // Ruta pública de autenticación
  servidor.createContext("/api/login", rutaExacta("/api/login", "POST",
          this::iniciarSesion));

  servidor.createContext("/api/trabajadores", conAuth(this::manejarTrabajadores));

  servidor.createContext("/api/periodos", conAuth(this::manejarPeriodos));

  servidor.createContext("/api/sistemas-pension", conAuth(rutaExacta("/api/sistemas-pension", "GET",
          ex -> listarSistemasPension())));

  servidor.createContext("/api/asistencias", conAuth(this::manejarAsistencias));

  servidor.createContext("/api/parametros", conAuth(this::manejarParametros));

  servidor.createContext("/api/tasas-pension", conAuth(this::manejarTasasPension));

  servidor.createContext("/api/boletas", conAuth(this::manejarBoletas));

  servidor.createContext("/api/planilla/calcular", conAuth(rutaExacta("/api/planilla/calcular", "POST",
          this::calcularPlanilla)));

  servidor.createContext("/api/planilla/resumen", conAuth(rutaExacta("/api/planilla/resumen", "GET",
          this::resumenMensual)));

  servidor.createContext("/", intercambio -> enviar(intercambio, error(NO_ENCONTRADO, "No existe la ruta")));
 }

 // ---------- Autenticación y seguridad con Token HMAC ----------

 private Respuesta iniciarSesion(HttpExchange intercambio) {
  Json.ObjetoJson json = Json.leerObjeto(leerCuerpo(intercambio));
  String correo = json.textoObligatorio("correo").trim().toLowerCase();
  String clave = json.textoObligatorio("clave");

  CredencialUsuario usuario = USUARIOS.get(correo);
  if (usuario == null || !usuario.clave().equals(clave)) {
   return error(NO_AUTORIZADO, "Correo o contraseña incorrectos");
  }

  String token = generarToken(correo);
  String cuerpo = "{"
          + "\"token\":" + escaparJson(token) + ","
          + "\"usuario\":{"
          + "\"correo\":" + escaparJson(correo) + ","
          + "\"nombre\":" + escaparJson(usuario.nombre()) + ","
          + "\"rol\":" + escaparJson(usuario.rol())
          + "}}";
  return new Respuesta(OK, cuerpo);
 }

 private HttpHandler conAuth(HttpHandler siguiente) {
  return intercambio -> {
   if (!estaAutorizado(intercambio)) {
    enviar(intercambio, error(NO_AUTORIZADO, "Sesión no iniciada o token expirado"));
    return;
   }
   siguiente.handle(intercambio);
  };
 }

 private static boolean estaAutorizado(HttpExchange intercambio) {
  String authHeader = intercambio.getRequestHeaders().getFirst("Authorization");
  String token = null;

  if (authHeader != null && authHeader.startsWith("Bearer ")) {
   token = authHeader.substring("Bearer ".length()).trim();
  } else {
   // Permite ?token=... para abrir PDFs en pestaña nueva
   String query = intercambio.getRequestURI().getQuery();
   if (query != null) {
    for (String par : query.split("&")) {
     String[] kv = par.split("=", 2);
     if (kv.length == 2 && "token".equals(kv[0]) && !kv[1].isBlank()) {
      token = kv[1].trim();
      break;
     }
    }
   }
  }
  return validarToken(token);
 }

 private static String generarToken(String correo) {
  long expira = Instant.now().getEpochSecond() + DURACION_TOKEN_SEGUNDOS;
  String payload = correo + "|" + expira;
  String payloadB64 = Base64.getUrlEncoder().withoutPadding()
          .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
  String firma = firmarHmac(payloadB64);
  return payloadB64 + "." + firma;
 }

 private static boolean validarToken(String token) {
  if (token == null || token.isBlank()) return false;
  String[] partes = token.split("\\.");
  if (partes.length != 2) return false;

  String firmaEsperada = firmarHmac(partes[0]);
  if (!MessageDigest.isEqual(
          firmaEsperada.getBytes(StandardCharsets.UTF_8),
          partes[1].getBytes(StandardCharsets.UTF_8))) {
   return false;
  }

  try {
   String payload = new String(Base64.getUrlDecoder().decode(partes[0]), StandardCharsets.UTF_8);
   String[] datos = payload.split("\\|");
   if (datos.length != 2) return false;
   long expira = Long.parseLong(datos[1]);
   return Instant.now().getEpochSecond() < expira;
  } catch (Exception e) {
   return false;
  }
 }

 private static String firmarHmac(String datos) {
  try {
   Mac mac = Mac.getInstance("HmacSHA256");
   mac.init(new SecretKeySpec(CLAVE_SECRETA_TOKEN.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
   byte[] hash = mac.doFinal(datos.getBytes(StandardCharsets.UTF_8));
   return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
  } catch (Exception e) {
   throw new IllegalStateException("No se pudo firmar el token de sesión", e);
  }
 }

 private static String escaparJson(String texto) {
  return "\"" + texto.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
 }

 // ---------- Manejadores por recurso ----------

 private void manejarTrabajadores(HttpExchange intercambio) throws IOException {
  String ruta = intercambio.getRequestURI().getPath();
  String metodo = intercambio.getRequestMethod();
  Respuesta respuesta;

  if ("/api/trabajadores".equals(ruta)) {
   respuesta = switch (metodo) {
    case "GET" -> atender(intercambio, ex -> listarTrabajadores());
    case "POST" -> atender(intercambio, this::registrarTrabajador);
    default -> error(METODO_NO_PERMITIDO, "Use GET o POST");
   };
  } else if (ruta.startsWith("/api/trabajadores/")) {
   if (!"PUT".equals(metodo)) {
    respuesta = error(METODO_NO_PERMITIDO, "Use PUT");
   } else {
    respuesta = atender(intercambio, ex -> actualizarTrabajador(ex, extraerIdFinal(ruta, "/api/trabajadores/")));
   }
  } else {
   respuesta = error(NO_ENCONTRADO, "No existe la ruta");
  }
  enviar(intercambio, respuesta);
 }

 private void manejarPeriodos(HttpExchange intercambio) throws IOException {
  String ruta = intercambio.getRequestURI().getPath();
  String metodo = intercambio.getRequestMethod();
  Respuesta respuesta;

  if ("/api/periodos".equals(ruta)) {
   respuesta = switch (metodo) {
    case "GET" -> atender(intercambio, ex -> listarPeriodos());
    case "POST" -> atender(intercambio, this::abrirPeriodo);
    default -> error(METODO_NO_PERMITIDO, "Use GET o POST");
   };
  } else if (ruta.startsWith("/api/periodos/") && ruta.endsWith("/cerrar")) {
   if (!"POST".equals(metodo)) {
    respuesta = error(METODO_NO_PERMITIDO, "Use POST");
   } else {
    String idTexto = ruta.substring("/api/periodos/".length(), ruta.length() - "/cerrar".length());
    respuesta = atender(intercambio, ex -> cerrarPeriodo(parsearId(idTexto)));
   }
  } else {
   respuesta = error(NO_ENCONTRADO, "No existe la ruta");
  }
  enviar(intercambio, respuesta);
 }

 private Respuesta listarPeriodos() {
  return new Respuesta(OK, Json.lista(servicio.listarPeriodos(), Json::de));
 }

 private Respuesta abrirPeriodo(HttpExchange intercambio) {
  Json.ObjetoJson json = Json.leerObjeto(leerCuerpo(intercambio));
  int anio = json.enteroObligatorio("anio");
  int mes = json.enteroObligatorio("mes");
  PeriodoPlanilla creado = servicio.abrirPeriodo(anio, mes);
  return new Respuesta(CREADO, Json.de(creado));
 }

 private Respuesta cerrarPeriodo(Long idPeriodo) {
  PeriodoPlanilla cerrado = servicio.cerrarPeriodo(idPeriodo);
  return new Respuesta(OK, Json.de(cerrado));
 }

 private static Long parsearId(String segmento) {
  if (segmento.isBlank() || segmento.contains("/")) {
   throw new DatoInvalidoException("Identificador inválido en la ruta");
  }
  try {
   return Long.parseLong(segmento);
  } catch (NumberFormatException e) {
   throw new DatoInvalidoException("El identificador de la ruta debe ser numérico");
  }
 }

 private Respuesta listarTrabajadores() {
  var fichas = servicio.listarTrabajadoresActivos().stream().map(TrabajadorDto::de).toList();
  return new Respuesta(OK, Json.lista(fichas, Json::de));
 }

 private Respuesta registrarTrabajador(HttpExchange intercambio) {
  Json.ObjetoJson json = Json.leerObjeto(leerCuerpo(intercambio));
  Trabajador nuevo = construirTrabajador(null, json);
  Trabajador guardado = servicio.registrarTrabajador(nuevo);
  return new Respuesta(CREADO, Json.de(TrabajadorDto.de(guardado)));
 }

 private Respuesta actualizarTrabajador(HttpExchange intercambio, Long idTrabajador) {
  Json.ObjetoJson json = Json.leerObjeto(leerCuerpo(intercambio));
  Trabajador actualizado = construirTrabajador(idTrabajador, json);
  Trabajador guardado = servicio.actualizarTrabajador(actualizado);
  return new Respuesta(OK, Json.de(TrabajadorDto.de(guardado)));
 }

 private Respuesta registrarAsistencia(HttpExchange intercambio) {
  Json.ObjetoJson json = Json.leerObjeto(leerCuerpo(intercambio));
  Long idTrabajador = json.enteroLargoObligatorio("idTrabajador");
  Long idPeriodo = json.enteroLargoObligatorio("idPeriodo");
  int diasFalta = json.enteroObligatorio("diasFalta");
  int minutosTardanza = json.enteroObligatorio("minutosTardanza");

  AsistenciaMensual guardada = servicio.registrarAsistencia(idTrabajador, idPeriodo, diasFalta, minutosTardanza);
  return new Respuesta(OK, Json.de(guardada));
 }

 private Respuesta calcularPlanilla(HttpExchange intercambio) {
  Json.ObjetoJson json = Json.leerObjeto(leerCuerpo(intercambio));
  Long idPeriodo = json.enteroLargoObligatorio("idPeriodo");
  List<BoletaPago> boletas = servicio.calcularPlanilla(idPeriodo);
  return new Respuesta(OK, Json.lista(boletas, Json::de));
 }

 private Respuesta resumenMensual(HttpExchange intercambio) {
  Long idPeriodo = parametroLong(intercambio, "idPeriodo");
  ResumenPlanilla resumen = servicio.resumenMensual(idPeriodo);
  return new Respuesta(OK, Json.de(resumen));
 }

 private Respuesta listarSistemasPension() {
  return new Respuesta(OK, Json.lista(servicio.listarSistemasPension(), Json::de));
 }

 private void manejarAsistencias(HttpExchange intercambio) throws IOException {
  if (!"/api/asistencias".equals(intercambio.getRequestURI().getPath())) {
   enviar(intercambio, error(NO_ENCONTRADO, "No existe la ruta"));
   return;
  }
  Respuesta respuesta = switch (intercambio.getRequestMethod()) {
   case "GET" -> atender(intercambio, this::listarAsistencias);
   case "POST" -> atender(intercambio, this::registrarAsistencia);
   default -> error(METODO_NO_PERMITIDO, "Use GET o POST");
  };
  enviar(intercambio, respuesta);
 }

 private Respuesta listarAsistencias(HttpExchange intercambio) {
  Long idPeriodo = parametroLong(intercambio, "idPeriodo");
  return new Respuesta(OK, Json.lista(servicio.listarAsistencias(idPeriodo), Json::de));
 }

 private Respuesta listarBoletas(HttpExchange intercambio) {
  Long idPeriodo = parametroLong(intercambio, "idPeriodo");
  return new Respuesta(OK, Json.lista(servicio.listarBoletas(idPeriodo), Json::de));
 }

 private void manejarBoletas(HttpExchange intercambio) throws IOException {
  String ruta = intercambio.getRequestURI().getPath();
  String metodo = intercambio.getRequestMethod();

  if ("/api/boletas".equals(ruta)) {
   Respuesta respuesta = "GET".equals(metodo)
           ? atender(intercambio, this::listarBoletas)
           : error(METODO_NO_PERMITIDO, "Use GET");
   enviar(intercambio, respuesta);
   return;
  }

  if (ruta.startsWith("/api/boletas/") && ruta.endsWith("/pdf")) {
   if (!"GET".equals(metodo)) {
    enviar(intercambio, error(METODO_NO_PERMITIDO, "Use GET"));
    return;
   }
   try {
    String idTexto = ruta.substring("/api/boletas/".length(), ruta.length() - "/pdf".length());
    Long idBoleta = parsearId(idTexto);
    var datos = servicio.obtenerDatosBoleta(idBoleta);
    byte[] pdf = GeneradorBoletaPdf.generar(datos.boleta(), datos.empresa());

    intercambio.getResponseHeaders().set("Content-Type", "application/pdf");
    intercambio.getResponseHeaders().set("Content-Disposition",
            "inline; filename=\"boleta-" + idBoleta + ".pdf\"");
    intercambio.sendResponseHeaders(OK, pdf.length);
    try (OutputStream salida = intercambio.getResponseBody()) {
     salida.write(pdf);
    }
   } catch (ReglaPlanillaException e) {
    enviar(intercambio, error(NO_ENCONTRADO, e.getMessage()));
   } catch (DatoInvalidoException e) {
    enviar(intercambio, error(SOLICITUD_INVALIDA, e.getMessage()));
   } catch (RuntimeException e) {
    REGISTRO.log(Level.SEVERE, "Error generando PDF", e);
    enviar(intercambio, error(ERROR_INTERNO, "Error interno generando PDF"));
   }
   return;
  }

  enviar(intercambio, error(NO_ENCONTRADO, "No existe la ruta"));
 }

 private void manejarParametros(HttpExchange intercambio) throws IOException {
  String ruta = intercambio.getRequestURI().getPath();
  String metodo = intercambio.getRequestMethod();
  Respuesta respuesta;

  if ("/api/parametros".equals(ruta)) {
   respuesta = "GET".equals(metodo)
           ? atender(intercambio, ex -> new Respuesta(OK, Json.lista(servicio.listarParametros(), Json::de)))
           : error(METODO_NO_PERMITIDO, "Use GET");
  } else if (ruta.startsWith("/api/parametros/")) {
   if (!"PUT".equals(metodo)) {
    respuesta = error(METODO_NO_PERMITIDO, "Use PUT");
   } else {
    String codigo = ruta.substring("/api/parametros/".length());
    if (codigo.isBlank() || codigo.contains("/")) {
     respuesta = error(SOLICITUD_INVALIDA, "Código de parámetro inválido en la ruta");
    } else {
     respuesta = atender(intercambio, ex -> actualizarParametro(ex, codigo));
    }
   }
  } else {
   respuesta = error(NO_ENCONTRADO, "No existe la ruta");
  }
  enviar(intercambio, respuesta);
 }

 private Respuesta actualizarParametro(HttpExchange intercambio, String codigo) {
  Json.ObjetoJson json = Json.leerObjeto(leerCuerpo(intercambio));
  BigDecimal valor = json.decimalObligatorio("valor");
  String descripcion = json.texto("descripcion");
  ParametroGeneral actualizado = servicio.actualizarParametro(codigo, valor, descripcion);
  return new Respuesta(OK, Json.de(actualizado));
 }

 private void manejarTasasPension(HttpExchange intercambio) throws IOException {
  String ruta = intercambio.getRequestURI().getPath();
  String metodo = intercambio.getRequestMethod();
  Respuesta respuesta;

  if ("/api/tasas-pension".equals(ruta)) {
   respuesta = "GET".equals(metodo)
           ? atender(intercambio, ex -> new Respuesta(OK, Json.lista(servicio.listarTasasPension(), Json::de)))
           : error(METODO_NO_PERMITIDO, "Use GET");
  } else if (ruta.startsWith("/api/tasas-pension/")) {
   if (!"PUT".equals(metodo)) {
    respuesta = error(METODO_NO_PERMITIDO, "Use PUT");
   } else {
    respuesta = atender(intercambio, ex -> actualizarTasaPension(ex, extraerIdFinal(ruta, "/api/tasas-pension/")));
   }
  } else {
   respuesta = error(NO_ENCONTRADO, "No existe la ruta");
  }
  enviar(intercambio, respuesta);
 }

 private Respuesta actualizarTasaPension(HttpExchange intercambio, Long idTasa) {
  Json.ObjetoJson json = Json.leerObjeto(leerCuerpo(intercambio));
  BigDecimal pctAporte = json.decimalObligatorio("pctAporte");
  BigDecimal pctPrima = json.decimalObligatorio("pctPrima");
  BigDecimal pctComision = json.decimalObligatorio("pctComision");
  TasaPension actualizada = servicio.actualizarTasaPension(idTasa, pctAporte, pctPrima, pctComision);
  return new Respuesta(OK, Json.de(actualizada));
 }

 // ---------- Conversión de entrada y mecánica HTTP ----------

 private static Trabajador construirTrabajador(Long idTrabajador, Json.ObjetoJson json) {
  String dni = json.textoObligatorio("dni");
  String nombres = json.textoObligatorio("nombres");
  String apellidos = json.textoObligatorio("apellidos");
  String cargo = json.textoObligatorio("cargo");
  LocalDate fechaIngreso = parsearFecha(json.textoObligatorio("fechaIngreso"));
  BigDecimal sueldoBasico = json.decimalObligatorio("sueldoBasico");

  Long idSistema = json.enteroLargoObligatorio("idSistemaPension");
  TipoSistemaPension tipoPension = parsearEnum(
          TipoSistemaPension.class, json.textoObligatorio("sistemaPension"), "sistemaPension");
  String entidad = json.texto("entidadPension");
  if (entidad == null || entidad.isBlank()) {
   entidad = tipoPension.name();
  }
  SistemaPension sistema = new SistemaPension(idSistema, entidad, tipoPension);

  String comisionTexto = json.texto("tipoComision");
  TipoComision tipoComision = (comisionTexto == null || comisionTexto.isBlank())
          ? null
          : parsearEnum(TipoComision.class, comisionTexto, "tipoComision");

  String estadoTexto = json.texto("estado");
  EstadoTrabajador estado = (estadoTexto == null || estadoTexto.isBlank())
          ? EstadoTrabajador.ACTIVO
          : parsearEnum(EstadoTrabajador.class, estadoTexto, "estado");

  return new Trabajador(idTrabajador, dni, nombres, apellidos, cargo,
          fechaIngreso, sueldoBasico, sistema, tipoComision, estado);
 }

 private static HttpHandler rutaExacta(String direccion, String metodo, Function<HttpExchange, Respuesta> accion) {
  return intercambio -> {
   Respuesta respuesta;
   if (!direccion.equals(intercambio.getRequestURI().getPath())) {
    respuesta = error(NO_ENCONTRADO, "No existe la ruta");
   } else if (!metodo.equals(intercambio.getRequestMethod())) {
    respuesta = error(METODO_NO_PERMITIDO, "Use " + metodo);
   } else {
    respuesta = atender(intercambio, accion);
   }
   enviar(intercambio, respuesta);
  };
 }

 private static Respuesta atender(HttpExchange intercambio, Function<HttpExchange, Respuesta> accion) {
  try {
   return accion.apply(intercambio);
  } catch (TrabajadorNoEncontradoException | PeriodoNoEncontradoException e) {
   return error(NO_ENCONTRADO, e.getMessage());
  } catch (ReglaPlanillaException e) {
   return error(REGLA_INCUMPLIDA, e.getMessage());
  } catch (DatoInvalidoException | IllegalArgumentException e) {
   return error(SOLICITUD_INVALIDA, e.getMessage());
  } catch (RuntimeException e) {
   REGISTRO.log(Level.SEVERE, "Error no previsto", e);
   return error(ERROR_INTERNO, "Error interno del servidor");
  }
 }

 private static String leerCuerpo(HttpExchange intercambio) {
  try {
   return new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
  } catch (IOException e) {
   throw new DatoInvalidoException("No se pudo leer el cuerpo de la petición");
  }
 }

 private static Long extraerIdFinal(String ruta, String prefijo) {
  String segmento = ruta.substring(prefijo.length());
  if (segmento.isBlank() || segmento.contains("/")) {
   throw new DatoInvalidoException("Identificador inválido en la ruta");
  }
  try {
   return Long.parseLong(segmento);
  } catch (NumberFormatException e) {
   throw new DatoInvalidoException("El identificador de la ruta debe ser numérico");
  }
 }

 private static Long parametroLong(HttpExchange intercambio, String nombre) {
  String query = intercambio.getRequestURI().getQuery();
  if (query != null) {
   for (String par : query.split("&")) {
    String[] kv = par.split("=", 2);
    if (kv.length == 2 && nombre.equals(kv[0]) && !kv[1].isBlank()) {
     try {
      return Long.parseLong(kv[1]);
     } catch (NumberFormatException e) {
      throw new DatoInvalidoException("El parámetro '" + nombre + "' debe ser numérico");
     }
    }
   }
  }
  throw new DatoInvalidoException("Falta el parámetro obligatorio: " + nombre);
 }

 private static LocalDate parsearFecha(String texto) {
  try {
   return LocalDate.parse(texto);
  } catch (DateTimeParseException e) {
   throw new DatoInvalidoException("Formato de fecha inválido, use YYYY-MM-DD");
  }
 }

 private static <E extends Enum<E>> E parsearEnum(Class<E> tipo, String valor, String campo) {
  try {
   return Enum.valueOf(tipo, valor.trim().toUpperCase());
  } catch (IllegalArgumentException e) {
   throw new DatoInvalidoException("Valor inválido para '" + campo + "': " + valor);
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