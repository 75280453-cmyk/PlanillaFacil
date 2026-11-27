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
 * Si la API responde con error (400, 404, 422...), lanza un ErrorApi con el mensaje.
 */
async function pedir<T>(metodo: Metodo, ruta: string, cuerpo?: unknown): Promise<T> {
  let respuesta: Response;
  try {
    respuesta = await fetch(`${BASE}${ruta}`, {
      method: metodo,
      headers: cuerpo === undefined ? undefined : { "Content-Type": "application/json" },
      body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
    });
  } catch {
    throw new ErrorApi(0, "No hay conexión con el servidor de PlanillaFácil");
  }
  const datos = await respuesta.json().catch(() => ({}));
  if (!respuesta.ok) {
    throw new ErrorApi(respuesta.status, datos.error ?? `Error ${respuesta.status}`);
  }
  return datos as T;
}

export const api = {
  salud: () => pedir<{ estado: string }>("GET", "/salud"),
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

/** Dirección del PDF de una boleta (se abre en otra pestaña, no es JSON). */
export function urlPdfBoleta(idBoleta: number): string {
  return `${BASE}/boletas/${idBoleta}/pdf`;
}
