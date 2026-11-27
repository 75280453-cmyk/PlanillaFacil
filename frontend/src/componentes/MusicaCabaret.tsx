import { useEffect, useRef, useState } from "react";

export function MusicaCabaret() {
    const [activa, setActiva] = useState(false);
    const [volumen, setVolumen] = useState(0.25);
    const audioRef = useRef<HTMLAudioElement | null>(null);

    useEffect(() => {
        const audio = new Audio("/cabaret.mp3");
        audio.loop = true;
        audio.volume = volumen;
        audioRef.current = audio;

        return () => {
            audio.pause();
            audioRef.current = null;
        };
    }, []);

    useEffect(() => {
        if (audioRef.current) {
            audioRef.current.volume = volumen;
        }
    }, [volumen]);

    const alternarMusica = () => {
        const audio = audioRef.current;
        if (!audio) return;

        if (activa) {
            audio.pause();
            setActiva(false);
        } else {
            audio
                .play()
                .then(() => setActiva(true))
                .catch(() => {
                    alert("No se pudo reproducir. Verifica que exista frontend/public/cabaret.mp3");
                });
        }
    };

    return (
        <div style={{ display: "inline-flex", alignItems: "center", gap: "0.5rem" }}>
            <button
                type="button"
                className={`boton-musica ${activa ? "sonando" : ""}`}
                onClick={alternarMusica}
                title="Activar o silenciar música ambiental"
            >
                <span className="icono-musica">{activa ? "♪" : "♩"}</span>
                {activa ? "Pausar música" : "Música: Off"}
            </button>

            {activa && (
                <input
                    type="range"
                    min="0.05"
                    max="1"
                    step="0.05"
                    value={volumen}
                    onChange={(e) => setVolumen(Number(e.target.value))}
                    title={`Volumen: ${Math.round(volumen * 100)}%`}
                    style={{ width: "70px", accentColor: "#d4af37", cursor: "pointer" }}
                />
            )}
        </div>
    );
}