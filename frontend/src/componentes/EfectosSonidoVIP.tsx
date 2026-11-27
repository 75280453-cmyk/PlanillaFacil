import { useEffect, useState } from "react";
import { sonidosCabaret } from "../utilidades/sonidosCabaret";

export function EfectosSonidoVIP() {
    const [silenciado, setSilenciado] = useState(false);

    // 1. Escuchar clics en botones y pestañas para sonar como ficha o carta
    useEffect(() => {
        const manejarClickGlobal = (e: MouseEvent) => {
            const objetivo = e.target as HTMLElement | null;
            if (!objetivo) return;

            const boton = objetivo.closest("button, [role='tab']");
            if (!boton) return;

            // Si es pestaña, suena a carta repartida; si es botón normal, a ficha de póker
            if (
                boton.getAttribute("role") === "tab" ||
                boton.classList.contains("pestana")
            ) {
                sonidosCabaret.repartirCarta();
            } else {
                sonidosCabaret.tocarFicha();
            }
        };

        document.addEventListener("click", manejarClickGlobal);
        return () => document.removeEventListener("click", manejarClickGlobal);
    }, []);

    // 2. Escuchar cambio de tema en body[data-escenario] para el acorde teatral
    useEffect(() => {
        let escenarioAnterior = document.body.dataset.escenario || "cabaret";

        const observador = new MutationObserver(() => {
            const actual = document.body.dataset.escenario || "cabaret";
            if (actual !== escenarioAnterior) {
                escenarioAnterior = actual;
                sonidosCabaret.cambiarEscenario(actual === "poker");
            }
        });

        observador.observe(document.body, {
            attributes: true,
            attributeFilter: ["data-escenario"],
        });

        return () => observador.disconnect();
    }, []);

    const toggleMute = () => {
        const nuevoEstado = sonidosCabaret.alternarSilencio();
        setSilenciado(nuevoEstado);
        if (!nuevoEstado) {
            sonidosCabaret.tocarFicha();
        }
    };

    return (
        <button
            type="button"
            className="boton-sfx-vip"
            onClick={toggleMute}
            title={silenciado ? "Activar efectos de fichas y cartas" : "Silenciar efectos"}
            aria-label="Efectos de sonido"
        >
            {silenciado ? "🔇 SFX Off" : "🪙 SFX On"}
        </button>
    );
}