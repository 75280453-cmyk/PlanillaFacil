import { useEffect, useState } from "react";
import { soles } from "../servicios/formato";
import type { ResumenMensual } from "../servicios/tipos";

interface DatoTarjeta {
  titulo: string;
  objetivo: number;
  esMoneda: boolean;
  destacada?: boolean;
}

function ContadorCasino({
                          objetivo,
                          esMoneda,
                        }: {
  objetivo: number;
  esMoneda: boolean;
}) {
  const [actual, setActual] = useState(0);
  const [finalizado, setFinalizado] = useState(false);

  useEffect(() => {
    setFinalizado(false);
    const duracion = 850; // ms que tarda en rodar el marcador
    const inicio = performance.now();
    let frameId: number;

    const animar = (ahora: number) => {
      const progreso = Math.min((ahora - inicio) / duracion, 1);
      // Desaceleración suave tipo ruleta / marcador mecánico
      const curva = 1 - Math.pow(1 - progreso, 3);
      setActual(objetivo * curva);

      if (progreso < 1) {
        frameId = requestAnimationFrame(animar);
      } else {
        setActual(objetivo);
        setFinalizado(true);
      }
    };

    frameId = requestAnimationFrame(animar);
    return () => cancelAnimationFrame(frameId);
  }, [objetivo]);

  const texto = esMoneda
      ? soles(actual)
      : String(Math.round(actual));

  return (
      <span className={`tarjeta-valor contador-casino ${finalizado ? "listo" : ""}`}>
      {texto}
    </span>
  );
}

export function TarjetasResumen({ resumen }: { resumen: ResumenMensual }) {
  const datos: DatoTarjeta[] = [
    { titulo: "Boletas", objetivo: resumen.cantidadBoletas, esMoneda: false },
    { titulo: "Remuneraciones", objetivo: resumen.totalRemuneraciones, esMoneda: true },
    { titulo: "Descuentos", objetivo: resumen.totalDescuentos, esMoneda: true },
    { titulo: "Neto a pagar", objetivo: resumen.totalNeto, esMoneda: true, destacada: true },
    { titulo: "EsSalud (empleador)", objetivo: resumen.totalEssalud, esMoneda: true },
  ];

  return (
      <div className="tarjetas">
        {datos.map((d) => (
            <div
                key={d.titulo}
                className={`tarjeta ${d.destacada ? "tarjeta-neto-vip" : ""}`}
            >
              <span className="tarjeta-titulo">{d.titulo}</span>
              <ContadorCasino objetivo={d.objetivo} esMoneda={d.esMoneda} />
            </div>
        ))}
      </div>
  );
}