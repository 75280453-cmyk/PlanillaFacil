import { fecha, soles } from "../servicios/formato";
import type { Trabajador } from "../servicios/tipos";

interface Props {
  trabajadores: Trabajador[];
  idEditando: number | null;
  idSeleccionado: number | null;
  alSeleccionar: (trabajador: Trabajador) => void;
  alEditar: (trabajador: Trabajador) => void;
}

export function TablaTrabajadores({
                                    trabajadores,
                                    idEditando,
                                    idSeleccionado,
                                    alSeleccionar,
                                    alEditar,
                                  }: Props) {
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
          {trabajadores.map((t) => {
            const activa = t.id === idEditando || t.id === idSeleccionado;
            return (
                <tr
                    key={t.id}
                    className={`fila-seleccionable ${activa ? "fila-activa" : ""}`}
                    onClick={() => alSeleccionar(t)}
                >
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
                    <button
                        type="button"
                        className="secundario"
                        onClick={(e) => {
                          e.stopPropagation();
                          alEditar(t);
                        }}
                    >
                      Editar
                    </button>
                  </td>
                </tr>
            );
          })}
          </tbody>
        </table>
      </div>
  );
}