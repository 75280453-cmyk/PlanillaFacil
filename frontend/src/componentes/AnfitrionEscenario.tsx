import { useEffect, useState } from "react";

const FRASES_CABARET = [
    "¡Bienvenido al Gran Cabaret! El elenco está listo para pasar lista.",
    "Haz clic en cualquier trabajador para revisar su Pase de Camerino VIP.",
    "Recuerda recalcular la planilla si ajustaste faltas o tardanzas.",
    "¿Todo cuadrado? Cierra el período para estampar el sello oficial.",
];

const FRASES_POKER = [
    "Mesa abierta en High Stakes. Suerte con los números de hoy.",
    "Cada boleta cuenta: revisa los tickets VIP antes de cerrar la ronda.",
    "Las cifras están sobre el tapete. ¿Listo para liquidar el mes?",
    "Un buen crupier siempre verifica los aportes antes de repartir.",
];

export function AnfitrionEscenario() {
    const [esPoker, setEsPoker] = useState(
        () => document.body.dataset.escenario === "poker"
    );
    const [saludando, setSaludando] = useState(true);
    const [minimizado, setMinimizado] = useState(false);
    const [indiceFrase, setIndiceFrase] = useState(0);
    const [mostrarGlobo, setMostrarGlobo] = useState(true);

    // Escucha cambios de tema (Gran Cabaret <-> High Stakes) en tiempo real
    useEffect(() => {
        const observador = new MutationObserver(() => {
            const nuevoEsPoker = document.body.dataset.escenario === "poker";
            setEsPoker((anterior) => {
                if (anterior !== nuevoEsPoker) {
                    setSaludando(true);
                    setMostrarGlobo(true);
                    setIndiceFrase(0);
                }
                return nuevoEsPoker;
            });
        });

        observador.observe(document.body, {
            attributes: true,
            attributeFilter: ["data-escenario"],
        });

        return () => observador.disconnect();
    }, []);

    // Pasa de la pose "saludando" a la pose "acompañando" después de 3.5s
    useEffect(() => {
        if (!saludando) return;
        const timer = window.setTimeout(() => setSaludando(false), 3500);
        return () => window.clearTimeout(timer);
    }, [saludando, esPoker]);

    const frases = esPoker ? FRASES_POKER : FRASES_CABARET;
    const imgSaludo = esPoker
        ? "/anfitriones/chico-saludando.png"
        : "/anfitriones/chica-saludando.png";
    const imgReposo = esPoker
        ? "/anfitriones/chico.png"
        : "/anfitriones/chica.png";
    const nombreAnfitrion = esPoker ? "Crupier High Stakes" : "Anfitriona Cabaret";

    function interactuar() {
        setSaludando(true);
        setMostrarGlobo(true);
        setIndiceFrase((prev) => (prev + 1) % frases.length);
    }

    return (
        <>
            {/* Marcos laterales Art Déco que visten los bordes vacíos de la pantalla */}
            <div className="marco-lateral marco-izquierdo" aria-hidden="true" />
            <div className="marco-lateral marco-derecho" aria-hidden="true" />

            {minimizado ? (
                <button
                    type="button"
                    className="anfitrion-minimizado"
                    onClick={() => {
                        setMinimizado(false);
                        setSaludando(true);
                        setMostrarGlobo(true);
                    }}
                    title={`Llamar a ${nombreAnfitrion}`}
                >
                    {esPoker ? "♠" : "♥"} Llamar anfitrión
                </button>
            ) : (
                <aside className="anfitrion-escenario" aria-label={nombreAnfitrion}>
                    {mostrarGlobo && (
                        <div className="anfitrion-globo" onClick={interactuar}>
                            <div className="anfitrion-globo-cabecera">
                                <span>✦ {nombreAnfitrion}</span>
                                <button
                                    type="button"
                                    className="anfitrion-btn-cerrar"
                                    aria-label="Ocultar mensaje"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        setMostrarGlobo(false);
                                    }}
                                >
                                    ×
                                </button>
                            </div>
                            <p>{frases[indiceFrase]}</p>
                        </div>
                    )}

                    <div
                        className={`anfitrion-figura ${saludando ? "esta-saludando" : "esta-reposo"}`}
                        onClick={interactuar}
                        title="Haz clic para saludar o ver un consejo"
                    >
                        <img
                            src={imgSaludo}
                            alt={`${nombreAnfitrion} saludando`}
                            className={`anfitrion-img ${saludando ? "visible" : "oculta"}`}
                        />
                        <img
                            src={imgReposo}
                            alt={nombreAnfitrion}
                            className={`anfitrion-img ${!saludando ? "visible" : "oculta"}`}
                        />
                    </div>

                    <button
                        type="button"
                        className="anfitrion-ocultar"
                        onClick={() => setMinimizado(true)}
                        title="Retirar anfitrión a camerinos"
                    >
                        Retirar
                    </button>
                </aside>
            )}
        </>
    );
}