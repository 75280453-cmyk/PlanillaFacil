package pe.edu.continental.planilla.infraestructura.web;

import pe.edu.continental.planilla.aplicacion.ResumenPlanilla;
import pe.edu.continental.planilla.dominio.excepcion.DatoInvalidoException;
import pe.edu.continental.planilla.dominio.modelo.AsistenciaMensual;
import pe.edu.continental.planilla.dominio.modelo.BoletaPago;
import pe.edu.continental.planilla.dominio.modelo.DetalleBoleta;
import pe.edu.continental.planilla.dominio.modelo.PeriodoPlanilla;
import pe.edu.continental.planilla.dominio.modelo.ParametroGeneral;
import pe.edu.continental.planilla.dominio.modelo.SistemaPension;
import pe.edu.continental.planilla.dominio.modelo.TasaPension;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    static String de(AsistenciaMensual a) {
        String nombreCompleto = a.trabajador().apellidos() + ", " + a.trabajador().nombres();
        return "{\"idAsistencia\":%s,\"idTrabajador\":%s,\"trabajador\":%s,\"idPeriodo\":%s,\"diasFalta\":%d,\"minutosTardanza\":%d}"
                .formatted(numero(a.idAsistencia()), numero(a.trabajador().idTrabajador()),
                        texto(nombreCompleto), numero(a.periodo().idPeriodo()),
                        a.diasFalta(), a.minutosTardanza());
    }

    static String de(BoletaPago b) {
        String nombreCompleto = b.trabajador().apellidos() + ", " + b.trabajador().nombres();
        return "{\"idBoleta\":%s,\"idTrabajador\":%s,\"dni\":%s,\"trabajador\":%s,\"idPeriodo\":%s,\"remuneracionBruta\":%s,\"totalDescuentos\":%s,\"netoPagar\":%s,\"aporteEssalud\":%s,\"fechaEmision\":%s,\"detalles\":%s}"
                .formatted(numero(b.idBoleta()), numero(b.trabajador().idTrabajador()),
                        texto(b.trabajador().dni()), texto(nombreCompleto),
                        numero(b.periodo().idPeriodo()), numero(b.remuneracionBruta()),
                        numero(b.totalDescuentos()), numero(b.netoPagar()),
                        numero(b.aporteEssalud()), texto(b.fechaEmision().toString()),
                        lista(b.detalles(), Json::de));
    }

    static String de(DetalleBoleta d) {
        return "{\"concepto\":%s,\"tipo\":%s,\"porcentaje\":%s,\"monto\":%s}"
                .formatted(texto(d.concepto()), texto(d.tipo().name()),
                        numero(d.porcentaje()), numero(d.monto()));
    }

    static String de(ResumenPlanilla r) {
        return "{\"anio\":%d,\"mes\":%d,\"cantidadBoletas\":%d,\"totalRemuneraciones\":%s,\"totalDescuentos\":%s,\"totalNeto\":%s,\"totalEssalud\":%s}"
                .formatted(r.anio(), r.mes(), r.cantidadBoletas(),
                        numero(r.totalRemuneraciones()), numero(r.totalDescuentos()),
                        numero(r.totalNeto()), numero(r.totalEssalud()));
    }

    static String de(PeriodoPlanilla p) {
        return "{\"idPeriodo\":%s,\"anio\":%d,\"mes\":%d,\"estado\":%s}"
                .formatted(numero(p.idPeriodo()), p.anio(), p.mes(), texto(p.estado().name()));
    }

    static String de(SistemaPension s) {
        return "{\"idSistemaPension\":%s,\"nombre\":%s,\"tipo\":%s}"
                .formatted(numero(s.idSistemaPension()), texto(s.nombre()), texto(s.tipo().name()));
    }

    static String de(ParametroGeneral p) {
        return "{\"idParametro\":%s,\"codigo\":%s,\"valor\":%s,\"descripcion\":%s,\"vigenteDesde\":%s}"
                .formatted(numero(p.idParametro()), texto(p.codigo()), numero(p.valor()),
                        texto(p.descripcion()), texto(p.vigenteDesde().toString()));
    }

    static String de(TasaPension t) {
        Long idSistema = t.sistemaPension() != null ? t.sistemaPension().idSistemaPension() : null;
        String nombreSistema = t.sistemaPension() != null ? t.sistemaPension().nombre() : null;
        String hasta = t.vigenteHasta() != null ? t.vigenteHasta().toString() : null;
        return "{\"idTasa\":%s,\"idSistemaPension\":%s,\"sistemaPension\":%s,\"tipoComision\":%s,\"pctAporte\":%s,\"pctPrima\":%s,\"pctComision\":%s,\"pctTotal\":%s,\"vigenteDesde\":%s,\"vigenteHasta\":%s}"
                .formatted(numero(t.idTasa()), numero(idSistema), texto(nombreSistema),
                        texto(t.tipoComision().name()), numero(t.pctAporte()), numero(t.pctPrima()),
                        numero(t.pctComision()), numero(t.pctTotal()),
                        texto(t.vigenteDesde().toString()), texto(hasta));
    }

    static <T> String lista(List<T> elementos, Function<T, String> convertir) {
        return elementos.stream().map(convertir).collect(Collectors.joining(",", "[", "]"));
    }

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

    // ---------- Lector de objetos JSON planos ----------

    static ObjetoJson leerObjeto(String cuerpo) {
        if (cuerpo == null || cuerpo.isBlank()) {
            throw new DatoInvalidoException("El cuerpo JSON es obligatorio");
        }
        return new Lector(cuerpo).leerObjeto();
    }

    static final class ObjetoJson {
        private final Map<String, String> campos;

        ObjetoJson(Map<String, String> campos) {
            this.campos = campos;
        }

        String texto(String clave) {
            return campos.get(clave);
        }

        String textoObligatorio(String clave) {
            String valor = campos.get(clave);
            if (valor == null || valor.isBlank()) {
                throw new DatoInvalidoException("Falta el campo obligatorio: " + clave);
            }
            return valor;
        }

        Long enteroLargoObligatorio(String clave) {
            try {
                return Long.parseLong(textoObligatorio(clave));
            } catch (NumberFormatException e) {
                throw new DatoInvalidoException("El campo '" + clave + "' debe ser un número entero");
            }
        }

        int enteroObligatorio(String clave) {
            try {
                return Integer.parseInt(textoObligatorio(clave));
            } catch (NumberFormatException e) {
                throw new DatoInvalidoException("El campo '" + clave + "' debe ser un número entero");
            }
        }

        BigDecimal decimalObligatorio(String clave) {
            try {
                return new BigDecimal(textoObligatorio(clave));
            } catch (NumberFormatException e) {
                throw new DatoInvalidoException("El campo '" + clave + "' debe ser un número decimal");
            }
        }
    }

    private static final class Lector {
        private final String src;
        private int pos;

        Lector(String src) {
            this.src = src;
        }

        ObjetoJson leerObjeto() {
            saltarEspacios();
            consumir('{');
            saltarEspacios();
            Map<String, String> mapa = new LinkedHashMap<>();
            if (mirar() == '}') {
                pos++;
                return new ObjetoJson(mapa);
            }
            while (true) {
                saltarEspacios();
                String clave = leerCadena();
                saltarEspacios();
                consumir(':');
                saltarEspacios();
                String valor = leerValor();
                mapa.put(clave, valor);
                saltarEspacios();
                char siguiente = mirar();
                if (siguiente == ',') {
                    pos++;
                } else if (siguiente == '}') {
                    pos++;
                    break;
                } else {
                    throw new DatoInvalidoException("JSON mal formado cerca de la posición " + pos);
                }
            }
            return new ObjetoJson(mapa);
        }

        private String leerValor() {
            char c = mirar();
            if (c == '"') {
                return leerCadena();
            }
            if (src.startsWith("null", pos)) {
                pos += 4;
                return null;
            }
            if (src.startsWith("true", pos)) {
                pos += 4;
                return "true";
            }
            if (src.startsWith("false", pos)) {
                pos += 5;
                return "false";
            }
            int inicio = pos;
            while (pos < src.length() && "0123456789.-+eE".indexOf(src.charAt(pos)) >= 0) {
                pos++;
            }
            if (inicio == pos) {
                throw new DatoInvalidoException("Valor JSON inválido en posición " + pos);
            }
            return src.substring(inicio, pos);
        }

        private String leerCadena() {
            consumir('"');
            StringBuilder sb = new StringBuilder();
            while (pos < src.length()) {
                char c = src.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    if (pos >= src.length()) {
                        throw new DatoInvalidoException("Escape incompleto en JSON");
                    }
                    char esc = src.charAt(pos++);
                    switch (esc) {
                        case '"', '\\', '/' -> sb.append(esc);
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            String hex = src.substring(pos, Math.min(pos + 4, src.length()));
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 4;
                        }
                        default -> throw new DatoInvalidoException("Carácter escapado inválido: \\" + esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw new DatoInvalidoException("Cadena JSON sin cierre");
        }

        private void saltarEspacios() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
                pos++;
            }
        }

        private void consumir(char esperado) {
            if (mirar() != esperado) {
                throw new DatoInvalidoException("Se esperaba '" + esperado + "' en posición " + pos);
            }
            pos++;
        }

        private char mirar() {
            if (pos >= src.length()) {
                throw new DatoInvalidoException("Fin inesperado del JSON");
            }
            return src.charAt(pos);
        }
    }
}
