import { useCallback, useEffect, useState } from "react";
import { AbrirPeriodo } from "../componentes/AbrirPeriodo";
import { SelloCierrePeriodo } from "../componentes/SelloCierrePeriodo";
import { TablaAsistencias } from "../componentes/TablaAsistencias";
import { TablaBoletas } from "../componentes/TablaBoletas";
import { TarjetasResumen } from "../componentes/TarjetasResumen";
import { api } from "../servicios/api";
import { nombrePeriodo } from "../servicios/formato";
import type { Asistencia, Boleta, Periodo, ResumenMensual, Trabajador } from "../servicios/tipos";

/** Más reciente primero. */
function ordenar(periodos: Periodo[]): Periodo[] {
  return [...periodos].sort((a, b) => b.anio * 12 + b.mes - (a.anio * 12 + a.mes));
}

export function PantallaPlanilla() {
  const [periodos, setPeriodos] = useState<Periodo[]>([]);
  const [trabajadores, setTrabajadores] = useState<Trabajador[]>([]);
  const [idPeriodo, setIdPeriodo] = useState<number | null>(null);
  const [asistencias, setAsistencias] = useState<Asistencia[]>([]);
  const [boletas, setBoletas] = useState<Boleta[]>([]);
  const [resumen, setResumen] = useState<ResumenMensual | null>(null);
  const [detalleListo, setDetalleListo] = useState(false);
  const [ocupado, setOcupado] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [aviso, setAviso] = useState<string | null>(null);
  const [mostrarNuevoPeriodo, setMostrarNuevoPeriodo] = useState(false);
  const [periodoRecienSellado, setPeriodoRecienSellado] = useState<string | null>(null);

  const periodo = periodos.find((p) => p.idPeriodo === idPeriodo) ?? null;
  const cerrado = periodo?.estado === "CERRADO";
  const hayAbierto = periodos.some((p) => p.estado === "ABIERTO");

  // 1) Al entrar: cargar períodos y trabajadores, y elegir el período más reciente.
  useEffect(() => {
    async function inicio() {
      try {
        const [listaPeriodos, listaTrabajadores] = await Promise.all([api.periodos(), api.trabajadores()]);
        const ordenados = ordenar(listaPeriodos);
        setPeriodos(ordenados);
        setTrabajadores(listaTrabajadores);
        setIdPeriodo(ordenados[0]?.idPeriodo ?? null);
      } catch (e) {
        setError((e as Error).message);
      }
    }
    void inicio();
  }, []);

  // 2) Cada vez que cambia el período elegido: cargar sus asistencias, boletas y resumen.
  const cargarPeriodo = useCallback(async (id: number) => {
    setDetalleListo(false);
    try {
      const [listaAsistencias, listaBoletas] = await Promise.all([api.asistencias(id), api.boletas(id)]);
      setAsistencias(listaAsistencias);
      setBoletas(listaBoletas);
      setResumen(listaBoletas.length > 0 ? await api.resumen(id) : null);
      setError(null);
      setDetalleListo(true);
    } catch (e) {
      setError((e as Error).message);
    }
  }, []);

  useEffect(() => {
    if (idPeriodo !== null) void cargarPeriodo(idPeriodo);
  }, [idPeriodo, cargarPeriodo]);

  function periodoAbierto(nuevo: Periodo) {
    setPeriodos((actuales) => ordenar([...actuales, nuevo]));
    setIdPeriodo(nuevo.idPeriodo);
    setMostrarNuevoPeriodo(false);
    setAviso(`Se abrió ${nombrePeriodo(nuevo.anio, nuevo.mes)}`);
  }

  async function calcular() {
    if (idPeriodo === null) return;
    setOcupado(true);
    setAviso(null);
    try {
      const generadas = await api.calcularPlanilla(idPeriodo);
      setBoletas(generadas);
      setResumen(await api.resumen(idPeriodo));
      setError(null);
      setAviso(`Planilla calculada: ${generadas.length} boleta(s)`);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setOcupado(false);
    }
  }

  async function cerrar() {
    if (periodo === null) return;
    if (!window.confirm(`¿Cerrar ${nombrePeriodo(periodo.anio, periodo.mes)}? Ya no se podrá modificar.`)) return;
    setOcupado(true);
    try {
      const actualizado = await api.cerrarPeriodo(periodo.idPeriodo);
      setPeriodos((actuales) => actuales.map((p) => (p.idPeriodo === actualizado.idPeriodo ? actualizado : p)));
      setError(null);
      const etiquetaMes = nombrePeriodo(actualizado.anio, actualizado.mes);
      setAviso(`Se cerró ${etiquetaMes}`);
      setPeriodoRecienSellado(etiquetaMes);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setOcupado(false);
    }
  }

  return (
      <>
        {error && <p className="error">{error}</p>}
        {aviso && <p className="aviso">{aviso}</p>}

        <section className="bloque">
          <h2>♦ Período Contable</h2>
          <div className="en-linea">
            {periodos.length > 0 && (
                <select
                    aria-label="Seleccionar período"
                    value={idPeriodo ?? ""}
                    onChange={(e) => {
                      setAviso(null);
                      setIdPeriodo(Number(e.target.value));
                    }}
                >
                  {periodos.map((p) => (
                      <option key={p.idPeriodo} value={p.idPeriodo}>
                        {nombrePeriodo(p.anio, p.mes)} ({p.estado === "ABIERTO" ? "abierto" : "cerrado"})
                      </option>
                  ))}
                </select>
            )}
            {periodo && !cerrado && (
                <button type="button" className="secundario" disabled={ocupado} onClick={cerrar}>
                  Cerrar período
                </button>
            )}
            {periodo && <span className={`estado estado-${periodo.estado}`}>{periodo.estado}</span>}

            {hayAbierto && (
                <button
                    type="button"
                    className="secundario"
                    onClick={() => setMostrarNuevoPeriodo((prev) => !prev)}
                >
                  {mostrarNuevoPeriodo ? "Ocultar formulario" : "+ Abrir otro mes"}
                </button>
            )}
          </div>

          {(!hayAbierto || mostrarNuevoPeriodo) && <AbrirPeriodo alAbrir={periodoAbierto} />}
        </section>

        {periodo && detalleListo && (
            <>
              <section className="bloque">
                <h2>♠ Asistencias de {nombrePeriodo(periodo.anio, periodo.mes)}</h2>
                <TablaAsistencias
                    idPeriodo={periodo.idPeriodo}
                    cerrado={cerrado}
                    trabajadores={trabajadores}
                    asistencias={asistencias}
                />
              </section>

              <section className="bloque">
                <h2>♥ Planilla y Boletas</h2>
                <div className="en-linea">
                  <button type="button" disabled={cerrado || ocupado || trabajadores.length === 0} onClick={calcular}>
                    {ocupado ? "Calculando…" : boletas.length > 0 ? "Recalcular planilla" : "Calcular planilla"}
                  </button>
                  {!cerrado && <span className="nota">Si cambia las asistencias, vuelva a calcular.</span>}
                </div>
                {resumen && <TarjetasResumen resumen={resumen} />}
                <TablaBoletas boletas={boletas} />
              </section>
            </>
        )}

        {periodos.length === 0 && !error && (
            <p className="vacio">Todavía no hay períodos registrados. Abra el mes actual para comenzar.</p>
        )}

        <SelloCierrePeriodo
            nombreMes={periodoRecienSellado}
            alCerrar={() => setPeriodoRecienSellado(null)}
        />
      </>
  );
}