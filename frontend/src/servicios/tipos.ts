// Formas de los datos que entrega la API. Deben coincidir con docs/api.md.

export type TipoSistemaPension = "ONP" | "AFP";
export type TipoComision = "FLUJO" | "MIXTA";
export type EstadoTrabajador = "ACTIVO" | "CESADO";

export interface SistemaPension {
  idSistemaPension: number;
  nombre: string;
  tipo: TipoSistemaPension;
}

/** Trabajador tal como lo devuelve la API. */
export interface Trabajador {
  id: number;
  dni: string;
  nombres: string;
  apellidos: string;
  cargo: string;
  fechaIngreso: string; // "2024-03-01"
  sueldoBasico: number;
  sistemaPension: TipoSistemaPension;
  entidadPension: string; // "ONP" o "AFP Integra"
  tipoComision: TipoComision | null; // null si es ONP
  estado: EstadoTrabajador;
}

/** Datos que se envían para registrar (POST) o actualizar (PUT) un trabajador. */
export interface DatosTrabajador {
  dni: string;
  nombres: string;
  apellidos: string;
  cargo: string;
  fechaIngreso: string;
  sueldoBasico: number;
  idSistemaPension: number;
  sistemaPension: TipoSistemaPension;
  entidadPension: string;
  tipoComision: TipoComision | null;
  estado?: EstadoTrabajador;
}

// ---------- Planilla ----------

export type EstadoPeriodo = "ABIERTO" | "CERRADO";

export interface Periodo {
  idPeriodo: number;
  anio: number;
  mes: number;
  estado: EstadoPeriodo;
}

export interface Asistencia {
  idAsistencia: number;
  idTrabajador: number;
  trabajador: string; // "Rojas, Luis"
  idPeriodo: number;
  diasFalta: number;
  minutosTardanza: number;
}

export interface DatosAsistencia {
  idTrabajador: number;
  idPeriodo: number;
  diasFalta: number;
  minutosTardanza: number;
}

export type TipoConcepto = "INGRESO" | "DESCUENTO" | "APORTE_EMPLEADOR";

export interface DetalleBoleta {
  concepto: string;
  tipo: TipoConcepto;
  porcentaje: number | null;
  monto: number;
}

export interface Boleta {
  idBoleta: number;
  idTrabajador: number;
  dni: string;
  trabajador: string;
  idPeriodo: number;
  remuneracionBruta: number;
  totalDescuentos: number;
  netoPagar: number;
  aporteEssalud: number;
  fechaEmision: string;
  detalles: DetalleBoleta[];
}

export interface ResumenMensual {
  anio: number;
  mes: number;
  cantidadBoletas: number;
  totalRemuneraciones: number;
  totalDescuentos: number;
  totalNeto: number;
  totalEssalud: number;
}
