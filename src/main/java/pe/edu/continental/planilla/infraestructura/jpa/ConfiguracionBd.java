package pe.edu.continental.planilla.infraestructura.jpa;

public record ConfiguracionBd(String url, String usuario, String clave) {
    public static ConfiguracionBd desdeEntorno() {
        return new ConfiguracionBd(
                valor("PLANILLA_BD_URL", "jdbc:postgresql://localhost:5432/planillafacil"),
                valor("PLANILLA_BD_USUARIO", "planilla"),
                valor("PLANILLA_BD_CLAVE", "planilla_dev"));
    }

    private static String valor(String nombre, String porDefecto) {
        String v = System.getenv(nombre);
        return (v == null || v.isBlank()) ? porDefecto : v;
    }
}
