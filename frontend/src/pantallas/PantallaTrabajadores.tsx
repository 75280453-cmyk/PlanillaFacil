import { useCallback, useEffect, useState } from "react";
import { FormularioTrabajador } from "../componentes/FormularioTrabajador";
import { TablaTrabajadores } from "../componentes/TablaTrabajadores";
import { api } from "../servicios/api";
import type { SistemaPension, Trabajador } from "../servicios/tipos";

export function PantallaTrabajadores() {
  const [trabajadores, setTrabajadores] = useState<Trabajador[]>([]);
  const [sistemas, setSistemas] = useState<SistemaPension[]>([]);
  const [editando, setEditando] = useState<Trabajador | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);

  const cargar = useCallback(async () => {
    try {
      const [lista, tiposPension] = await Promise.all([api.trabajadores(), api.sistemasPension()]);
      setTrabajadores(lista);
      setSistemas(tiposPension);
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
    setAviso(eraNuevo ? `Se registró a ${t.nombres} ${t.apellidos}` : `Se actualizó a ${t.nombres} ${t.apellidos}`);
    void cargar();
  }

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
            alEditar={(t) => {
              setAviso(null);
              setEditando(t);
            }}
          />
        </section>

        <section>
          {/* La "key" reinicia el formulario cada vez que cambia el trabajador a editar. */}
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
