import { Fragment, useState } from "react";
import { urlPdfBoleta } from "../servicios/api";
import { soles } from "../servicios/formato";
import type { Boleta } from "../servicios/tipos";
import { BoletaTicketVIP } from "./BoletaTicketVIP";

interface Props {
  boletas: Boleta[];
}

export function TablaBoletas({ boletas }: Props) {
  const [abierta, setAbierta] = useState<number | null>(null);
  const [ticketSeleccionado, setTicketSeleccionado] = useState<Boleta | null>(null);

  if (boletas.length === 0) {
    return <p className="vacio">Todavía no se calculó la planilla de este período.</p>;
  }
  return (
      <>
        <div className="tabla-contenedor">
          <table>
            <thead>
            <tr>
              <th>Trabajador</th>
              <th className="numero">Bruto</th>
              <th className="numero">Descuentos</th>
              <th className="numero">Neto a pagar</th>
              <th className="numero">EsSalud</th>
              <th></th>
            </tr>
            </thead>
            <tbody>
            {boletas.map((b) => (
                <Fragment key={b.idBoleta}>
                  <tr>
                    <td>{b.trabajador}</td>
                    <td className="numero">{soles(b.remuneracionBruta)}</td>
                    <td className="numero">{soles(b.totalDescuentos)}</td>
                    <td className="numero"><strong>{soles(b.netoPagar)}</strong></td>
                    <td className="numero">{soles(b.aporteEssalud)}</td>
                    <td className="acciones-fila">
                      <button
                          type="button"
                          onClick={() => setTicketSeleccionado(b)}
                      >
                        ✦ Ticket VIP
                      </button>
                      <button
                          type="button"
                          className="secundario"
                          onClick={() => setAbierta(abierta === b.idBoleta ? null : b.idBoleta)}
                      >
                        {abierta === b.idBoleta ? "Ocultar" : "Detalle"}
                      </button>
                      <a className="enlace-boton" href={urlPdfBoleta(b.idBoleta)} target="_blank" rel="noreferrer">
                        PDF
                      </a>
                    </td>
                  </tr>
                  {abierta === b.idBoleta && (
                      <tr className="fila-detalle">
                        <td colSpan={6}>
                          <ul className="detalles">
                            {b.detalles.map((d, i) => (
                                <li key={i}>
                                  <span>{d.concepto}{d.porcentaje !== null && ` (${d.porcentaje}%)`}</span>
                                  <span className={`tipo tipo-${d.tipo}`}>{etiquetaTipo(d.tipo)}</span>
                                  <span className="numero">{soles(d.monto)}</span>
                                </li>
                            ))}
                          </ul>
                        </td>
                      </tr>
                  )}
                </Fragment>
            ))}
            </tbody>
          </table>
        </div>

        <BoletaTicketVIP
            boleta={ticketSeleccionado}
            alCerrar={() => setTicketSeleccionado(null)}
        />
      </>
  );
}

function etiquetaTipo(tipo: string): string {
  if (tipo === "INGRESO") return "Ingreso";
  if (tipo === "DESCUENTO") return "Descuento";
  return "Aporte del empleador";
}