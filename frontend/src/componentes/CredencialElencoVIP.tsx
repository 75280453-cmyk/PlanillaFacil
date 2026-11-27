import { fecha, soles } from "../servicios/formato";
import type { Trabajador } from "../servicios/tipos";

interface Props {
    trabajador: Trabajador | null;
    alEditar: (trabajador: Trabajador) => void;
}

const PALOS = ["♠", "♥", "♦", "♣"];

export function CredencialElencoVIP({ trabajador, alEditar }: Props) {
    if (!trabajador) return null;

    const palo = PALOS[trabajador.id % PALOS.length];
    const iniciales = `${trabajador.nombres.charAt(0)}${trabajador.apellidos.charAt(0)}`.toUpperCase();

    return (
        <div key={trabajador.id} className="credencial-vip-caja">
            <div className="credencial-vip-cabecera">
                <span className="credencial-vip-cinta">✦ PASE DE CAMERINO · ELENCO VIP ✦</span>
                <span className="credencial-vip-id">ID #{String(trabajador.id).padStart(3, "0")}</span>
            </div>

            <div className="credencial-vip-cuerpo">
                <div className="credencial-vip-avatar" aria-hidden="true">
                    <span className="credencial-vip-palo">{palo}</span>
                    <strong>{iniciales}</strong>
                </div>

                <div className="credencial-vip-info">
                    <span className="credencial-vip-cargo">{trabajador.cargo}</span>
                    <h3>
                        {trabajador.nombres} {trabajador.apellidos}
                    </h3>
                    <p className="credencial-vip-dni">DNI: {trabajador.dni}</p>
                </div>

                <div className="credencial-vip-sueldo">
                    <span>Sueldo Base</span>
                    <strong>{soles(trabajador.sueldoBasico)}</strong>
                </div>
            </div>

            <div className="credencial-vip-pie">
                <div className="credencial-vip-datos">
          <span>
            <strong>Ingreso:</strong> {fecha(trabajador.fechaIngreso)}
          </span>
                    <span>
            <strong>Pensión:</strong> {trabajador.entidadPension}
                        {trabajador.tipoComision ? ` (${trabajador.tipoComision})` : ""}
          </span>
                </div>

                <button type="button" className="secundario" onClick={() => alEditar(trabajador)}>
                    Editar ficha
                </button>
            </div>
        </div>
    );
}