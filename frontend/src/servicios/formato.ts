/** 1500 -> "S/ 1,500.00" */
export function soles(monto: number): string {
  return new Intl.NumberFormat("es-PE", { style: "currency", currency: "PEN" }).format(monto);
}

/** "2024-03-01" -> "01/03/2024" (sin pasar por Date, para evitar desfases de zona horaria). */
export function fecha(iso: string): string {
  const [anio, mes, dia] = iso.split("-");
  return `${dia}/${mes}/${anio}`;
}

/** Fecha de hoy como "YYYY-MM-DD", hora local. */
export function hoyIso(): string {
  const d = new Date();
  const mes = String(d.getMonth() + 1).padStart(2, "0");
  const dia = String(d.getDate()).padStart(2, "0");
  return `${d.getFullYear()}-${mes}-${dia}`;
}

const MESES = ["enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
  "agosto", "septiembre", "octubre", "noviembre", "diciembre"];

/** (2026, 11) -> "Noviembre 2026" */
export function nombrePeriodo(anio: number, mes: number): string {
  const nombre = MESES[mes - 1] ?? String(mes);
  return `${nombre.charAt(0).toUpperCase()}${nombre.slice(1)} ${anio}`;
}

export const NOMBRES_MESES = MESES.map((m) => m.charAt(0).toUpperCase() + m.slice(1));
