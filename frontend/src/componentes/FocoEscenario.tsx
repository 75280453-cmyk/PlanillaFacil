import { useEffect } from "react";

export function FocoEscenario() {
    useEffect(() => {
        const actualizarPosicion = (e: PointerEvent) => {
            document.documentElement.style.setProperty("--foco-x", `${e.clientX}px`);
            document.documentElement.style.setProperty("--foco-y", `${e.clientY}px`);
        };

        window.addEventListener("pointermove", actualizarPosicion, { passive: true });
        return () => window.removeEventListener("pointermove", actualizarPosicion);
    }, []);

    return <div className="foco-escenario" aria-hidden="true" />;
}