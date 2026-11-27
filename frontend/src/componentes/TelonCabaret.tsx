import { useEffect, useState } from "react";

export function TelonCabaret() {
    const [abriendo, setAbriendo] = useState(false);
    const [oculto, setOculto] = useState(false);

    useEffect(() => {
        // Inicia la apertura apenas monta la escena
        const t1 = window.setTimeout(() => setAbriendo(true), 250);
        // Retira el componente del DOM al terminar para no bloquear clics
        const t2 = window.setTimeout(() => setOculto(true), 1750);

        return () => {
            window.clearTimeout(t1);
            window.clearTimeout(t2);
        };
    }, []);

    if (oculto) return null;

    return (
        <div className={`telon-contenedor ${abriendo ? "abriendo" : ""}`} aria-hidden="true">
            <div className="telon-hoja telon-izquierda">
                <div className="telon-fleco" />
            </div>

            <div className="telon-hoja telon-derecha">
                <div className="telon-fleco" />
            </div>

            <div className="telon-sello">
                <span className="sello-palos">♠   ♥   ♦   ♣</span>
                <strong>PLANILLAFÁCIL</strong>
                <small>Gran Salón & Cabaret</small>
            </div>
        </div>
    );
}