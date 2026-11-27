import { useEffect } from "react";
import { urlPdfBoleta } from "../servicios/api";
import { soles } from "../servicios/formato";
import type { Boleta } from "../servicios/tipos";

interface Props {
    boleta: Boleta | null;
    alCerrar: () => void;
}

export function BoletaTicketVIP({ boleta, alCerrar }: Props) {
    // Cerrar con tecla Escape
    useEffect(() => {
        if (!boleta) return;
        const manejarTecla = (e: KeyboardEvent) => {
            if (e.key === "Escape") alCerrar();
        };
        window.addEventListener("keydown", manejarTecla);
        return () => window.removeEventListener("keydown", manejarTecla);
    }, [boleta, alCerrar]);

    if (!boleta) return null;

    const ingresos = boleta.detalles.filter((d) => d.tipo === "INGRESO");
    const descuentos = boleta.detalles.filter((d) => d.tipo === "DESCUENTO");
    const aportes = boleta.detalles.filter((d) => d.tipo !== "INGRESO" && d.tipo !== "DESCUENTO");

    return (
        <div
            className="ticket-vip-overlay"
            role="dialog"
            aria-modal="true"
            aria-labelledby="titulo-ticket-vip"
            onClick={alCerrar}
        >
            <article className="ticket-vip" onClick={(e) => e.stopPropagation()}>
                {/* Muescas laterales tipo boleto troquelado */}
                <span className="ticket-muesca izquierda" aria-hidden="true" />
                <span className="ticket-muesca derecha" aria-hidden="true" />

                {/* Cabecera del Pase de Función */}
                <header className="ticket-cabecera">
                    <div className="ticket-etiqueta-superior">
                        <span>✦ PLANILLAFÁCIL · SALÓN VIP ✦</span>
                        <span className="ticket-folio">N.º {String(boleta.idBoleta).padStart(5, "0")}</span>
                    </div>
                    <h3 id="titulo-ticket-vip" className="ticket-titular">
                        {boleta.trabajador}
                    </h3>
                    <p className="ticket-subtitulo">Boleta Oficial de Remuneraciones · Pase Liquidado</p>
                </header>

                <div className="ticket-linea-troquelada" aria-hidden="true" />

                {/* Cuerpo de Conceptos */}
                <div className="ticket-cuerpo">
                    {ingresos.length > 0 && (
                        <div className="ticket-seccion">
                            <h4>♠ Ingresos</h4>
                            <ul>
                                {ingresos.map((d, i) => (
                                    <li key={i}>
                    <span>
                      {d.concepto}
                        {d.porcentaje !== null && ` (${d.porcentaje}%)`}
                    </span>
                                        <span className="numero">{soles(d.monto)}</span>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}

                    {descuentos.length > 0 && (
                        <div className="ticket-seccion">
                            <h4>♥ Descuentos de Ley</h4>
                            <ul>
                                {descuentos.map((d, i) => (
                                    <li key={i}>
                    <span>
                      {d.concepto}
                        {d.porcentaje !== null && ` (${d.porcentaje}%)`}
                    </span>
                                        <span className="numero negativo">- {soles(d.monto)}</span>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}

                    {aportes.length > 0 && (
                        <div className="ticket-seccion">
                            <h4>♦ Aportes del Empleador</h4>
                            <ul>
                                {aportes.map((d, i) => (
                                    <li key={i}>
                    <span>
                      {d.concepto}
                        {d.porcentaje !== null && ` (${d.porcentaje}%)`}
                    </span>
                                        <span className="numero">{soles(d.monto)}</span>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}

                    {/* Resumen de Totales y Sello Estampado */}
                    <div className="ticket-totales-contenedor">
                        <div className="ticket-totales">
                            <div>
                                <span>Remuneración Bruta</span>
                                <strong>{soles(boleta.remuneracionBruta)}</strong>
                            </div>
                            <div>
                                <span>Total Descuentos</span>
                                <strong>- {soles(boleta.totalDescuentos)}</strong>
                            </div>
                            <div>
                                <span>Aporte EsSalud</span>
                                <strong>{soles(boleta.aporteEssalud)}</strong>
                            </div>
                            <div className="ticket-neto">
                                <span>NETO A PAGAR</span>
                                <strong>{soles(boleta.netoPagar)}</strong>
                            </div>
                        </div>

                        {/* Sello dorado animado */}
                        <div className="ticket-sello-vip" aria-hidden="true">
                            <span>LIQUIDADO</span>
                            <small>PASE AUTORIZADO</small>
                        </div>
                    </div>
                </div>

                <div className="ticket-linea-troquelada" aria-hidden="true" />

                {/* Pie con acciones */}
                <footer className="ticket-pie">
                    <div className="ticket-codigo-barras" aria-hidden="true">
                        ║▌│█║▌│ █║▌│█│║▌║
                    </div>
                    <div className="ticket-acciones">
                        <a
                            className="enlace-boton"
                            href={urlPdfBoleta(boleta.idBoleta)}
                            target="_blank"
                            rel="noreferrer"
                        >
                            Descargar PDF
                        </a>
                        <button type="button" className="secundario" onClick={() => window.print()}>
                            Imprimir Ticket
                        </button>
                        <button type="button" onClick={alCerrar}>
                            Cerrar
                        </button>
                    </div>
                </footer>
            </article>
        </div>
    );
}