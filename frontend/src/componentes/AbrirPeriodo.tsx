import { useState, type FormEvent } from "react";
import { api } from "../servicios/api";
import { NOMBRES_MESES } from "../servicios/formato";
import type { Periodo } from "../servicios/tipos";

interface Props {
  alAbrir: (periodo: Periodo) => void;
}

export function AbrirPeriodo({ alAbrir }: Props) {
  const hoy = new Date();
  const [anio, setAnio] = useState(String(hoy.getFullYear()));
  const [mes, setMes] = useState(String(hoy.getMonth() + 1));
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function enviar(evento: FormEvent) {
    evento.preventDefault();
    setError(null);
    setEnviando(true);
    try {
      alAbrir(await api.abrirPeriodo(Number(anio), Number(mes)));
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form className="en-linea" onSubmit={enviar}>
      <label>
        Mes
        <select value={mes} onChange={(e) => setMes(e.target.value)}>
          {NOMBRES_MESES.map((nombre, i) => (
            <option key={nombre} value={i + 1}>{nombre}</option>
          ))}
        </select>
      </label>
      <label>
        Año
        <input className="corto" type="number" min="2000" max="2100" value={anio}
               onChange={(e) => setAnio(e.target.value)} required />
      </label>
      <button type="submit" disabled={enviando}>Abrir período</button>
      {error && <span className="error-fila">{error}</span>}
    </form>
  );
}
