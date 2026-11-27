import { useEffect, useState } from "react";

type Escenario = "cabaret" | "poker";

export function SelectorEscenario() {
    const [escenario, setEscenario] = useState<Escenario>("cabaret");

    useEffect(() => {
        document.body.dataset.escenario = escenario;
    }, [escenario]);

    const alternar = () => {
        setEscenario((actual) => (actual === "cabaret" ? "poker" : "cabaret"));
    };

    return (
        <button
            type="button"
            className={`boton-escenario ${escenario === "poker" ? "modo-poker" : ""}`}
            onClick={alternar}
            title="Cambiar ambiente del salón"
        >
      <span className="icono-escenario">
        {escenario === "cabaret" ? "♠" : "♦"}
      </span>
            <span>
        {escenario === "cabaret" ? "Modo High Stakes" : "Modo Gran Cabaret"}
      </span>
        </button>
    );
}