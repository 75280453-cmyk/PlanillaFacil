import { AnfitrionEscenario } from "./componentes/AnfitrionEscenario";
import { useState } from "react";
import { FocoEscenario } from "./componentes/FocoEscenario";
import { CursorCabaret } from "./componentes/CursorCabaret";
import { EfectoCartas3D } from "./componentes/EfectoCartas3D";
import { MusicaCabaret } from "./componentes/MusicaCabaret";
import { NotificacionesVIP } from "./componentes/NotificacionesVIP";
import { SelectorEscenario } from "./componentes/SelectorEscenario";
import { TelonCabaret } from "./componentes/TelonCabaret";
import { PantallaPlanilla } from "./pantallas/PantallaPlanilla";
import { PantallaTrabajadores } from "./pantallas/PantallaTrabajadores";

type Pestana = "trabajadores" | "planilla";

const BOMBILLAS = Array.from({ length: 18 }, (_, i) => i);

export function App() {
  const [pestana, setPestana] = useState<Pestana>("trabajadores");

  return (
      <>
        <AnfitrionEscenario />
        <FocoEscenario />
        <TelonCabaret />
        <CursorCabaret />
        <EfectoCartas3D />
        <NotificacionesVIP />
        <main className="pagina">
          <header className="cabecera-teatro marquesina">
            <div className="fila-bombillas" aria-hidden="true">
              {BOMBILLAS.map((i) => (
                  <span key={i} className={`bombilla ${i % 2 === 0 ? "par" : "impar"}`} />
              ))}
            </div>

            <div className="marquesina-interior">
              <div className="marca-titulo">
                <div>
                <span className="subtitulo-teatro">
                  ♠   Gran Cabaret & Salón High Stakes   ♦
                </span>
                  <h1>PlanillaFácil</h1>
                </div>
                <div className="controles-salon">
                  <SelectorEscenario />
                  <MusicaCabaret />
                </div>
              </div>

              <div className="ornamento-casino" aria-hidden="true">
                <span className="linea-dorada" />
                <span className="palos-poker">♠   ♥   ♦   ♣</span>
                <span className="linea-dorada" />
              </div>
            </div>

            <div className="fila-bombillas" aria-hidden="true">
              {BOMBILLAS.map((i) => (
                  <span key={i} className={`bombilla ${i % 2 === 0 ? "impar" : "par"}`} />
              ))}
            </div>

            <nav className="pestanas" aria-label="Secciones">
              <button
                  type="button"
                  className={pestana === "trabajadores" ? "activa" : undefined}
                  onClick={() => setPestana("trabajadores")}
              >
                ♦ Elenco y Personal
              </button>
              <button
                  type="button"
                  className={pestana === "planilla" ? "activa" : undefined}
                  onClick={() => setPestana("planilla")}
              >
                ♠ Planilla Mensual
              </button>
            </nav>
          </header>

          {/* Se vuelve a montar al cambiar de pestaña, así siempre muestra datos frescos. */}
          {pestana === "trabajadores" ? <PantallaTrabajadores /> : <PantallaPlanilla />}
        </main>
      </>
  );
}