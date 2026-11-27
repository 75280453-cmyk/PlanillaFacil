import { fecha, soles } from "../servicios/formato";
import type { Trabajador } from "../servicios/tipos";

interface Props {
  trabajadores: Trabajador[];
  idEditando: number | null;
  alEditar: (trabajador: Trabajador) => void;
}

export function TablaTrabajadores({ trabajadores, idEditando, alEditar }: Props) {
  if (trabajadores.length === 0) {
    return <p className="vacio">Todavía no hay trabajadores registrados.</p>;
  }
  return (
    <div className="tabla-contenedor">
      <table>
        <thead>
          <tr>
            <th>DNI</th>
            <th>Trabajador</th>
            <th>Cargo</th>
            <th>Ingreso</th>
            <th className="numero">Sueldo</th>
            <th>Pensión</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {trabajadores.map((t) => (
            <tr key={t.id} className={t.id === idEditando ? "fila-activa" : undefined}>
              <td>{t.dni}</td>
              <td>
                {t.apellidos}, {t.nombres}
              </td>
              <td>{t.cargo}</td>
              <td>{fecha(t.fechaIngreso)}</td>
              <td className="numero">{soles(t.sueldoBasico)}</td>
              <td>
                {t.entidadPension}
                {t.tipoComision && <span className="etiqueta">{t.tipoComision}</span>}
              </td>
              <td>
                <button type="button" className="secundario" onClick={() => alEditar(t)}>
                  Editar
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
