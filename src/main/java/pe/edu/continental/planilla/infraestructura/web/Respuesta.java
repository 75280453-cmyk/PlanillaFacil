package pe.edu.continental.planilla.infraestructura.web;

/** Código de estado HTTP (200, 404...) y cuerpo JSON de una respuesta. */
record Respuesta(int estado, String cuerpo) {
}
