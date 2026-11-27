import { useState, type FormEvent } from "react";
import { api } from "../servicios/api";
import { hoyIso } from "../servicios/formato";
import type {
  DatosTrabajador,
  EstadoTrabajador,
  SistemaPension,
  TipoComision,
  Trabajador,
} from "../servicios/tipos";

interface Props {
  sistemas: SistemaPension[];
  /** Si viene un trabajador, el formulario edita; si no, registra uno nuevo. */
  trabajador: Trabajador | null;
  alGuardar: (guardado: Trabajador, eraNuevo: boolean) => void;
  alCancelar: () => void;
}

export function FormularioTrabajador({ sistemas, trabajador, alGuardar, alCancelar }: Props) {
  const editando = trabajador !== null;

  // Cada campo del formulario es un texto; se convierte a número recién al enviar.
  const [dni, setDni] = useState(trabajador?.dni ?? "");
  const [nombres, setNombres] = useState(trabajador?.nombres ?? "");
  const [apellidos, setApellidos] = useState(trabajador?.apellidos ?? "");
  const [cargo, setCargo] = useState(trabajador?.cargo ?? "");
  const [fechaIngreso, setFechaIngreso] = useState(trabajador?.fechaIngreso ?? "");
  const [sueldo, setSueldo] = useState(trabajador ? String(trabajador.sueldoBasico) : "");
  const [idSistema, setIdSistema] = useState(() => {
    const actual = sistemas.find((s) => s.nombre === trabajador?.entidadPension);
    return actual ? String(actual.idSistemaPension) : "";
  });
  const [comision, setComision] = useState<TipoComision | "">(trabajador?.tipoComision ?? "");
  const [estado, setEstado] = useState<EstadoTrabajador>(trabajador?.estado ?? "ACTIVO");
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const sistema = sistemas.find((s) => String(s.idSistemaPension) === idSistema);
  const esAfp = sistema?.tipo === "AFP";

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    if (!sistema) {
      setError("Elija un sistema de pensión");
      return;
    }
    const datos: DatosTrabajador = {
      dni: dni.trim(),
      nombres: nombres.trim(),
      apellidos: apellidos.trim(),
      cargo: cargo.trim(),
      fechaIngreso,
      sueldoBasico: Number(sueldo),
      idSistemaPension: sistema.idSistemaPension,
      sistemaPension: sistema.tipo,
      entidadPension: sistema.nombre,
      tipoComision: esAfp && comision !== "" ? comision : null,
      ...(editando ? { estado } : {}),
    };
    setError(null);
    setEnviando(true);
    try {
      const guardado = editando
        ? await api.actualizarTrabajador(trabajador.id, datos)
        : await api.registrarTrabajador(datos);
      alGuardar(guardado, !editando);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form className="formulario" onSubmit={enviar}>
      <h2>{editando ? "Editar trabajador" : "Nuevo trabajador"}</h2>

      <label>
        DNI
        <input value={dni} onChange={(e) => setDni(e.target.value)} inputMode="numeric"
               maxLength={8} pattern="\d{8}" title="8 dígitos" required />
      </label>
      <label>
        Nombres
        <input value={nombres} onChange={(e) => setNombres(e.target.value)} required />
      </label>
      <label>
        Apellidos
        <input value={apellidos} onChange={(e) => setApellidos(e.target.value)} required />
      </label>
      <label>
        Cargo
        <input value={cargo} onChange={(e) => setCargo(e.target.value)} required />
      </label>
      <label>
        Fecha de ingreso
        <input type="date" value={fechaIngreso} max={hoyIso()}
               onChange={(e) => setFechaIngreso(e.target.value)} required />
      </label>
      <label>
        Sueldo básico (S/)
        <input type="number" value={sueldo} min="0.01" step="0.01"
               onChange={(e) => setSueldo(e.target.value)} required />
      </label>
      <label>
        Sistema de pensión
        <select value={idSistema} onChange={(e) => setIdSistema(e.target.value)} required>
          <option value="">Elija…</option>
          {sistemas.map((s) => (
            <option key={s.idSistemaPension} value={s.idSistemaPension}>
              {s.nombre}
            </option>
          ))}
        </select>
      </label>
      {esAfp && (
        <label>
          Tipo de comisión
          <select value={comision} onChange={(e) => setComision(e.target.value as TipoComision | "")} required>
            <option value="">Elija…</option>
            <option value="FLUJO">Flujo</option>
            <option value="MIXTA">Mixta</option>
          </select>
        </label>
      )}
      {editando && (
        <label>
          Estado
          <select value={estado} onChange={(e) => setEstado(e.target.value as EstadoTrabajador)}>
            <option value="ACTIVO">Activo</option>
            <option value="CESADO">Cesado (deja de aparecer en la lista)</option>
          </select>
        </label>
      )}

      {error && <p className="error">{error}</p>}

      <div className="acciones">
        <button type="submit" disabled={enviando}>
          {enviando ? "Guardando…" : editando ? "Guardar cambios" : "Registrar"}
        </button>
        {editando && (
          <button type="button" className="secundario" onClick={alCancelar}>
            Cancelar
          </button>
        )}
      </div>
    </form>
  );
}
