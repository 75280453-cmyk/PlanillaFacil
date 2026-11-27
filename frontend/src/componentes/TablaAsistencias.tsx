import { useState } from "react";
import { api } from "../servicios/api";
import type { Asistencia, Trabajador } from "../servicios/tipos";

interface Props {
  idPeriodo: number;
  cerrado: boolean;
  trabajadores: Trabajador[];
  asistencias: Asistencia[];
}

export function TablaAsistencias({ idPeriodo, cerrado, trabajadores, asistencias }: Props) {
  if (trabajadores.length === 0) {
    return <p className="vacio">Primero registre trabajadores en la pestaña "Trabajadores".</p>;
  }
  return (
    <div className="tabla-contenedor">
      <table>
        <thead>
          <tr>
            <th>Trabajador</th>
            <th>Días de falta</th>
            <th>Minutos de tardanza</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {trabajadores.map((t) => (
            <FilaAsistencia
              key={`${t.id}-${idPeriodo}`}
              idPeriodo={idPeriodo}
              cerrado={cerrado}
              trabajador={t}
              existente={asistencias.find((a) => a.idTrabajador === t.id)}
            />
          ))}
        </tbody>
      </table>
    </div>
  );
}

interface PropsFila {
  idPeriodo: number;
  cerrado: boolean;
  trabajador: Trabajador;
  existente: Asistencia | undefined;
}

function FilaAsistencia({ idPeriodo, cerrado, trabajador, existente }: PropsFila) {
  const [dias, setDias] = useState(String(existente?.diasFalta ?? 0));
  const [minutos, setMinutos] = useState(String(existente?.minutosTardanza ?? 0));
  const [mensaje, setMensaje] = useState<{ ok: boolean; texto: string } | null>(null);
  const [guardando, setGuardando] = useState(false);

  async function guardar() {
    setGuardando(true);
    setMensaje(null);
    try {
      await api.registrarAsistencia({
        idTrabajador: trabajador.id,
        idPeriodo,
        diasFalta: Number(dias),
        minutosTardanza: Number(minutos),
      });
      setMensaje({ ok: true, texto: "Guardado" });
    } catch (e) {
      setMensaje({ ok: false, texto: (e as Error).message });
    } finally {
      setGuardando(false);
    }
  }

  const nombre = `${trabajador.apellidos}, ${trabajador.nombres}`;
  return (
    <tr>
      <td>{nombre}</td>
      <td>
        <input className="corto" type="number" min="0" max="31" step="1" value={dias}
               aria-label={`Días de falta de ${nombre}`} disabled={cerrado}
               onChange={(e) => setDias(e.target.value)} />
      </td>
      <td>
        <input className="corto" type="number" min="0" step="1" value={minutos}
               aria-label={`Minutos de tardanza de ${nombre}`} disabled={cerrado}
               onChange={(e) => setMinutos(e.target.value)} />
      </td>
      <td>
        <button type="button" className="secundario" disabled={cerrado || guardando}
                aria-label={`Guardar asistencia de ${nombre}`} onClick={guardar}>
          Guardar
        </button>
        {mensaje && <span className={mensaje.ok ? "ok-fila" : "error-fila"}>{mensaje.texto}</span>}
      </td>
    </tr>
  );
}
