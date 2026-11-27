import type {
  Asistencia,
  Boleta,
  DatosAsistencia,
  DatosTrabajador,
  Periodo,
  ResumenMensual,
  SistemaPension,
  Trabajador,
} from "./tipos";

const BASE = "/api";
const CLAVE_SESION = "planillafacil_sesion";

export interface SesionUsuario {
  token: string;
  usuario: {
    correo: string;
    nombre: string;
    rol: string;
  };
}

export function guardarSesion(sesion: SesionUsuario): void {
  sessionStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
}

export function obtenerSesion(): SesionUsuario | null {
  const crudo = sessionStorage.getItem(CLAVE_SESION);
  if (!crudo) return null;
  try {
    return JSON.parse(crudo) as SesionUsuario;
  } catch {
    return null;
  }
}

export function cerrarSesion(): void {
  sessionStorage.removeItem(CLAVE_SESION);
}

/** Error que devuelve la API: trae el código HTTP y el mensaje de la regla incumplida. */
export class ErrorApi extends Error {
  readonly estado: number;

  constructor(estado: number, mensaje: string) {
    super(mensaje);
    this.name = "ErrorApi";
    this.estado = estado;
  }
}

type Metodo = "GET" | "POST" | "PUT";

/**
 * Hace un pedido a la API y devuelve el JSON ya convertido.
 * Adjunta el token Bearer si existe sesión activa.
 */
async function pedir<T>(metodo: Metodo, ruta: string, cuerpo?: unknown): Promise<T> {
  const headers: Record<string, string> = {};
  if (cuerpo !== undefined) {
    headers["Content-Type"] = "application/json";
  }
  const sesion = obtenerSesion();
  if (sesion?.token) {
    headers["Authorization"] = `Bearer ${sesion.token}`;
  }

  let respuesta: Response;
  try {
    respuesta = await fetch(`${BASE}${ruta}`, {
      method: metodo,
      headers,
      body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
    });
  } catch {
    throw new ErrorApi(0, "No hay conexión con el servidor de PlanillaFácil");
  }

  const datos = await respuesta.json().catch(() => ({}));
  if (!respuesta.ok) {
    if (respuesta.status === 401 && ruta !== "/login") {
      cerrarSesion();
      window.dispatchEvent(new Event("planillafacil:expirada"));
    }
    throw new ErrorApi(respuesta.status, datos.error ?? `Error ${respuesta.status}`);
  }
  return datos as T;
}

export const api = {
  salud: () => pedir<{ estado: string }>("GET", "/salud"),
  login: (correo: string, clave: string) =>
      pedir<SesionUsuario>("POST", "/login", { correo, clave }),

  sistemasPension: () => pedir<SistemaPension[]>("GET", "/sistemas-pension"),
  trabajadores: () => pedir<Trabajador[]>("GET", "/trabajadores"),
  registrarTrabajador: (datos: DatosTrabajador) => pedir<Trabajador>("POST", "/trabajadores", datos),
  actualizarTrabajador: (id: number, datos: DatosTrabajador) =>
      pedir<Trabajador>("PUT", `/trabajadores/${id}`, datos),

  periodos: () => pedir<Periodo[]>("GET", "/periodos"),
  abrirPeriodo: (anio: number, mes: number) => pedir<Periodo>("POST", "/periodos", { anio, mes }),
  cerrarPeriodo: (idPeriodo: number) => pedir<Periodo>("POST", `/periodos/${idPeriodo}/cerrar`),

  asistencias: (idPeriodo: number) => pedir<Asistencia[]>("GET", `/asistencias?idPeriodo=${idPeriodo}`),
  registrarAsistencia: (datos: DatosAsistencia) => pedir<Asistencia>("POST", "/asistencias", datos),

  calcularPlanilla: (idPeriodo: number) => pedir<Boleta[]>("POST", "/planilla/calcular", { idPeriodo }),
  resumen: (idPeriodo: number) => pedir<ResumenMensual>("GET", `/planilla/resumen?idPeriodo=${idPeriodo}`),
  boletas: (idPeriodo: number) => pedir<Boleta[]>("GET", `/boletas?idPeriodo=${idPeriodo}`),
};

/** Dirección del PDF de una boleta (se abre en otra pestaña con el token de sesión). */
export function urlPdfBoleta(idBoleta: number): string {
  const sesion = obtenerSesion();
  const query = sesion?.token ? `?token=${encodeURIComponent(sesion.token)}` : "";
  return `${BASE}/boletas/${idBoleta}/pdf${query}`;
}