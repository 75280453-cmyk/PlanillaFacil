import { soles } from "../servicios/formato";
import type { ResumenMensual } from "../servicios/tipos";

export function TarjetasResumen({ resumen }: { resumen: ResumenMensual }) {
  const datos = [
    ["Boletas", String(resumen.cantidadBoletas)],
    ["Remuneraciones", soles(resumen.totalRemuneraciones)],
    ["Descuentos", soles(resumen.totalDescuentos)],
    ["Neto a pagar", soles(resumen.totalNeto)],
    ["EsSalud (empleador)", soles(resumen.totalEssalud)],
  ];
  return (
    <div className="tarjetas">
      {datos.map(([titulo, valor]) => (
        <div key={titulo} className="tarjeta">
          <span className="tarjeta-titulo">{titulo}</span>
          <span className="tarjeta-valor">{valor}</span>
        </div>
      ))}
    </div>
  );
}
