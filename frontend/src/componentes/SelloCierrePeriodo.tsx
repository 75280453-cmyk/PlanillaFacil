import { useEffect } from "react";

interface Props {
    nombreMes: string | null;
    alCerrar: () => void;
}

export function SelloCierrePeriodo({ nombreMes, alCerrar }: Props) {
    useEffect(() => {
        if (!nombreMes) return;
        const manejarTecla = (e: KeyboardEvent) => {
            if (e.key === "Escape" || e.key === "Enter") alCerrar();
        };
        window.addEventListener("keydown", manejarTecla);
        return () => window.removeEventListener("keydown", manejarTecla);
    }, [nombreMes, alCerrar]);

    if (!nombreMes) return null;

    return (
        <div
            className="sello-cierre-overlay"
            role="dialog"
            aria-modal="true"
            aria-labelledby="titulo-sello-cierre"
            onClick={alCerrar}
        >
            <div className="sello-cierre-acta" onClick={(e) => e.stopPropagation()}>
                <span className="sello-cierre-ornamento">✦ ACTA DE CIERRE CONTABLE ✦</span>
                <h3 id="titulo-sello-cierre">{nombreMes}</h3>
                <p>
                    La planilla del período ha quedado oficialmente cerrada, auditada y bloqueada
                    contra modificaciones.
                </p>

                {/* Sello de lacre animado */}
                <div className="sello-lacre-medallon" aria-hidden="true">
                    <div className="sello-lacre-interior">
                        <span>SELLADO</span>
                        <small>DIRECCIÓN VIP</small>
                    </div>
                </div>

                <button type="button" onClick={alCerrar}>
                    Continuar
                </button>
            </div>
        </div>
    );
}