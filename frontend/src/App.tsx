import { useState } from "react";
import { PantallaPlanilla } from "./pantallas/PantallaPlanilla";
import { PantallaTrabajadores } from "./pantallas/PantallaTrabajadores";

type Pestana = "trabajadores" | "planilla";

export function App() {
  const [pestana, setPestana] = useState<Pestana>("trabajadores");

  return (
    <main className="pagina">
      <header>
        <h1>PlanillaFácil</h1>
        <nav className="pestanas" aria-label="Secciones">
          <button type="button" className={pestana === "trabajadores" ? "activa" : undefined}
                  onClick={() => setPestana("trabajadores")}>
            Trabajadores
          </button>
          <button type="button" className={pestana === "planilla" ? "activa" : undefined}
                  onClick={() => setPestana("planilla")}>
            Planilla mensual
          </button>
        </nav>
      </header>

      {/* Se vuelve a montar al cambiar de pestaña, así siempre muestra datos frescos. */}
      {pestana === "trabajadores" ? <PantallaTrabajadores /> : <PantallaPlanilla />}
    </main>
  );
}
