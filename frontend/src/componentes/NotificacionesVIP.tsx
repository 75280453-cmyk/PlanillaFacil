import { useEffect, useState } from "react";

interface AlertaVIP {
    id: number;
    tipo: "exito" | "error";
    mensaje: string;
}

export function NotificacionesVIP() {
    const [alertas, setAlertas] = useState<AlertaVIP[]>([]);

    useEffect(() => {
        let ultimoTexto = "";
        let ultimoTiempo = 0;

        const lanzarAlerta = (tipo: "exito" | "error", texto: string) => {
            const limpio = texto.trim();
            if (!limpio) return;

            const ahora = Date.now();
            if (limpio === ultimoTexto && ahora - ultimoTiempo < 600) return;
            ultimoTexto = limpio;
            ultimoTiempo = ahora;

            const id = ahora + Math.random();
            setAlertas((prev) => [...prev.slice(-2), { id, tipo, mensaje: limpio }]);

            window.setTimeout(() => {
                setAlertas((prev) => prev.filter((a) => a.id !== id));
            }, 4200);
        };

        const revisarNodos = () => {
            document.querySelectorAll<HTMLElement>(".aviso").forEach((el) => {
                if (el.dataset.vipMostrado !== el.textContent) {
                    el.dataset.vipMostrado = el.textContent ?? "";
                    lanzarAlerta("exito", el.textContent ?? "");
                }
            });

            document.querySelectorAll<HTMLElement>(".error").forEach((el) => {
                if (el.dataset.vipMostrado !== el.textContent) {
                    el.dataset.vipMostrado = el.textContent ?? "";
                    lanzarAlerta("error", el.textContent ?? "");
                }
            });
        };

        const observador = new MutationObserver(revisarNodos);
        observador.observe(document.body, {
            childList: true,
            subtree: true,
            characterData: true,
        });

        revisarNodos();

        return () => observador.disconnect();
    }, []);

    if (alertas.length === 0) return null;

    return (
        <div className="contenedor-tickets-vip" aria-live="polite">
            {alertas.map((alerta) => (
                <div
                    key={alerta.id}
                    className={`ticket-vip ${alerta.tipo}`}
                    onClick={() =>
                        setAlertas((prev) => prev.filter((a) => a.id !== alerta.id))
                    }
                >
                    <div className="ticket-talon">
                        <span>{alerta.tipo === "exito" ? "♦" : "♠"}</span>
                        <small>VIP</small>
                    </div>
                    <div className="ticket-cuerpo">
                        <strong>
                            {alerta.tipo === "exito" ? "Aviso de Salón" : "Atención en Escena"}
                        </strong>
                        <p>{alerta.mensaje}</p>
                    </div>
                    <div className="ticket-progreso" />
                </div>
            ))}
        </div>
    );
}