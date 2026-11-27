import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// En desarrollo, Vite reenvía todo lo que empiece con /api al servidor Java (puerto 8080).
// Así el navegador siempre habla con un solo origen y no hace falta configurar CORS en la API.
// OJO: a diferencia del ejemplo de semana 13, aquí NO se quita el prefijo /api,
// porque ApiPlanilla registra sus rutas completas (/api/trabajadores, /api/periodos...).
const API_LOCAL = process.env.API_URL ?? "http://localhost:8080";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": { target: API_LOCAL },
    },
  },
});
