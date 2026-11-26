package pe.edu.continental.planilla.infraestructura.web;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Convierte fichas (DTO) a texto JSON, escrito a mano para no agregar librerías.
 * Misma idea que el Json.java del ejemplo de semana 13.
 */
final class Json {

    private Json() {
    }

    static String de(TrabajadorDto t) {
        return "{\"id\":%s,\"dni\":%s,\"nombres\":%s,\"apellidos\":%s,\"cargo\":%s,\"fechaIngreso\":%s,\"sueldoBasico\":%s,\"sistemaPension\":%s,\"entidadPension\":%s,\"tipoComision\":%s,\"estado\":%s}"
                .formatted(numero(t.id()), texto(t.dni()), texto(t.nombres()), texto(t.apellidos()),
                        texto(t.cargo()), texto(t.fechaIngreso()), numero(t.sueldoBasico()),
                        texto(t.sistemaPension()), texto(t.entidadPension()),
                        texto(t.tipoComision()), texto(t.estado()));
    }

    /** Convierte una lista en un arreglo JSON: [ficha, ficha, ...]. */
    static <T> String lista(List<T> elementos, Function<T, String> convertir) {
        return elementos.stream().map(convertir).collect(Collectors.joining(",", "[", "]"));
    }

    /** Un objeto de un solo dato, por ejemplo {"error":"No existe"}. */
    static String mensaje(String clave, String valor) {
        return "{%s:%s}".formatted(texto(clave), texto(valor));
    }

    static String texto(String valor) {
        if (valor == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        valor.chars().forEach(c -> sb.append(escapar((char) c)));
        return sb.append('"').toString();
    }

    private static String escapar(char c) {
        return switch (c) {
            case '"' -> "\\\"";
            case '\\' -> "\\\\";
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            case '\t' -> "\\t";
            default -> c < ' ' ? "\\u%04x".formatted((int) c) : String.valueOf(c);
        };
    }

    private static String numero(Number valor) {
        if (valor == null) {
            return "null";
        }
        return valor instanceof BigDecimal d ? d.toPlainString() : valor.toString();
    }
}
