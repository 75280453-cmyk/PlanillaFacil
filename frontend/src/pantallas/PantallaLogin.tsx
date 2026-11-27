import { useState, type FormEvent } from "react";
import { api, ErrorApi, guardarSesion, type SesionUsuario } from "../servicios/api";
import { SelectorEscenario } from "../componentes/SelectorEscenario";

interface Props {
    alIniciarSesion: (sesion: SesionUsuario) => void;
}

const BOMBILLAS = Array.from({ length: 14 }, (_, i) => i);

export function PantallaLogin({ alIniciarSesion }: Props) {
    const [correo, setCorreo] = useState("admin@planillafacil.pe");
    const [clave, setClave] = useState("vip2026");
    const [cargando, setCargando] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function manejarEnvio(e: FormEvent) {
        e.preventDefault();
        setError(null);
        setCargando(true);
        try {
            const sesion = await api.login(correo, clave);
            guardarSesion(sesion);
            alIniciarSesion(sesion);
        } catch (err) {
            if (err instanceof ErrorApi) {
                setError(err.message);
            } else {
                setError("Error inesperado al iniciar sesión");
            }
        } finally {
            setCargando(false);
        }
    }

    return (
        <main
            className="pagina"
            style={{
                minHeight: "100vh",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                padding: "1.5rem",
            }}
        >
            <section
                className="cabecera-teatro marquesina"
                style={{
                    width: "100%",
                    maxWidth: "440px",
                    borderRadius: "16px",
                    overflow: "hidden",
                }}
            >
                <div className="fila-bombillas" aria-hidden="true">
                    {BOMBILLAS.map((i) => (
                        <span key={i} className={`bombilla ${i % 2 === 0 ? "par" : "impar"}`} />
                    ))}
                </div>

                <div className="marquesina-interior" style={{ padding: "1.75rem 1.5rem" }}>
                    <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: "0.75rem" }}>
                        <SelectorEscenario />
                    </div>

                    <div style={{ textAlign: "center", marginBottom: "1.25rem" }}>
                        <span className="subtitulo-teatro">♠   Acceso Exclusivo de Empleador   ♦</span>
                        <h1 style={{ margin: "0.35rem 0 0" }}>PlanillaFácil</h1>
                    </div>

                    <div className="ornamento-casino" aria-hidden="true" style={{ marginBottom: "1.25rem" }}>
                        <span className="linea-dorada" />
                        <span className="palos-poker">♠   ♥   ♦   ♣</span>
                        <span className="linea-dorada" />
                    </div>

                    <form onSubmit={manejarEnvio} style={{ display: "grid", gap: "1rem" }}>
                        <label style={{ display: "grid", gap: "0.35rem" }}>
                            <span>Correo corporativo</span>
                            <input
                                type="email"
                                value={correo}
                                onChange={(e) => setCorreo(e.target.value)}
                                placeholder="admin@planillafacil.pe"
                                required
                                autoFocus
                            />
                        </label>

                        <label style={{ display: "grid", gap: "0.35rem" }}>
                            <span>Contraseña</span>
                            <input
                                type="password"
                                value={clave}
                                onChange={(e) => setClave(e.target.value)}
                                placeholder="••••••••"
                                required
                            />
                        </label>

                        {error && (
                            <p
                                role="alert"
                                style={{
                                    margin: 0,
                                    padding: "0.65rem 0.85rem",
                                    borderRadius: "8px",
                                    background: "rgba(220, 38, 38, 0.18)",
                                    border: "1px solid rgba(248, 113, 113, 0.5)",
                                    color: "#fecaca",
                                    fontSize: "0.9rem",
                                }}
                            >
                                {error}
                            </p>
                        )}

                        <button
                            type="submit"
                            disabled={cargando}
                            style={{ marginTop: "0.25rem", width: "100%", padding: "0.75rem" }}
                        >
                            {cargando ? "Verificando credenciales..." : "♦ Ingresar al Salón"}
                        </button>
                    </form>
                </div>

                <div className="fila-bombillas" aria-hidden="true">
                    {BOMBILLAS.map((i) => (
                        <span key={i} className={`bombilla ${i % 2 === 0 ? "impar" : "par"}`} />
                    ))}
                </div>
            </section>
        </main>
    );
}