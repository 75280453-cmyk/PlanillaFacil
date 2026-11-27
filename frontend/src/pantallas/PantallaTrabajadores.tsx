import { useCallback, useEffect, useState } from "react";
import { CredencialElencoVIP } from "../componentes/CredencialElencoVIP";
import { FormularioTrabajador } from "../componentes/FormularioTrabajador";
import { TablaTrabajadores } from "../componentes/TablaTrabajadores";
import { api } from "../servicios/api";
import type { SistemaPension, Trabajador } from "../servicios/tipos";

export function PantallaTrabajadores() {
  const [trabajadores, setTrabajadores] = useState<Trabajador[]>([]);
  const [sistemas, setSistemas] = useState<SistemaPension[]>([]);
  const [editando, setEditando] = useState<Trabajador | null>(null);
  const [idSeleccionado, setIdSeleccionado] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  const cargar = useCallback(async () => {
    try {
      const [lista, tiposPension] = await Promise.all([api.trabajadores(), api.sistemasPension()]);
      setTrabajadores(lista);
      setSistemas(tiposPension);
      setIdSeleccionado((actual) =>
          lista.some((t) => t.id === actual) ? actual : (lista[0]?.id ?? null)
      );
      setError(null);
    } catch (e) {
      setError((e as Error).message);
    }
  }, []);

  useEffect(() => {
    void cargar();
  }, [cargar]);

  function guardado(t: Trabajador, eraNuevo: boolean) {
    setEditando(null);
    setIdSeleccionado(t.id);
    setAviso(eraNuevo ? `Se registró a ${t.nombres} ${t.apellidos}` : `Se actualizó a ${t.nombres} ${t.apellidos}`);
    void cargar();
  }

  const trabajadorEnCartelera =
      editando ?? trabajadores.find((t) => t.id === idSeleccionado) ?? trabajadores[0] ?? null;

  return (
      <>
        {error && <p className="error">{error}</p>}
        {aviso && <p className="aviso">{aviso}</p>}

        <div className="dos-columnas">
          <section>
            <h2>Trabajadores activos ({trabajadores.length})</h2>
            <TablaTrabajadores
                trabajadores={trabajadores}
                idEditando={editando?.id ?? null}
                idSeleccionado={trabajadorEnCartelera?.id ?? null}
                alSeleccionar={(t) => setIdSeleccionado(t.id)}
                alEditar={(t) => {
                  setAviso(null);
                  setEditando(t);
                  setIdSeleccionado(t.id);
                }}
            />

            <CredencialElencoVIP
                trabajador={trabajadorEnCartelera}
                alEditar={(t) => {
                  setAviso(null);
                  setEditando(t);
                }}
            />
          </section>

          <section>
            <FormularioTrabajador
                key={editando?.id ?? "nuevo"}
                sistemas={sistemas}
                trabajador={editando}
                alGuardar={guardado}
                alCancelar={() => setEditando(null)}
            />
          </section>
        </div>
      </>
  );
}