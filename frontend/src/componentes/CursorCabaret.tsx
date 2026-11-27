import { useEffect, useRef } from "react";

interface Particula {
    x: number;
    y: number;
    vx: number;
    vy: number;
    vida: number;
    tamano: number;
    rotacion: number;
    vRotacion: number;
    tipo: "rombo" | "estrella";
    color: string;
}

export function CursorCabaret() {
    const aroRef = useRef<HTMLDivElement>(null);
    const punteroRef = useRef<HTMLDivElement>(null);
    const focoRef = useRef<HTMLDivElement>(null);
    const canvasRef = useRef<HTMLCanvasElement>(null);

    useEffect(() => {
        const esTactil = window.matchMedia("(pointer: coarse)").matches;
        if (esTactil) return;

        const canvas = canvasRef.current;
        const ctx = canvas?.getContext("2d");
        let ancho = (canvas!.width = window.innerWidth);
        let alto = (canvas!.height = window.innerHeight);

        const ajustarTamano = () => {
            if (!canvas) return;
            ancho = canvas.width = window.innerWidth;
            alto = canvas.height = window.innerHeight;
        };
        window.addEventListener("resize", ajustarTamano);

        let mouseX = ancho / 2;
        let mouseY = alto / 2;
        let aroX = mouseX;
        let aroY = mouseY;
        let sobreInteractivo = false;
        const particulas: Particula[] = [];

        const colores = ["#f3d37a", "#d4af37", "#ff4d6d", "#fff1c1"];

        const crearChispas = (x: number, y: number, cantidad: number) => {
            for (let i = 0; i < cantidad; i++) {
                const angulo = Math.random() * Math.PI * 2;
                const velocidad = 0.7 + Math.random() * 2.6;
                particulas.push({
                    x,
                    y,
                    vx: Math.cos(angulo) * velocidad,
                    vy: Math.sin(angulo) * velocidad - 0.6,
                    vida: 1,
                    tamano: 3.5 + Math.random() * 3.5,
                    rotacion: Math.random() * Math.PI,
                    vRotacion: (Math.random() - 0.5) * 0.14,
                    tipo: Math.random() > 0.45 ? "rombo" : "estrella",
                    color: colores[Math.floor(Math.random() * colores.length)],
                });
            }
        };

        const alMover = (e: MouseEvent) => {
            mouseX = e.clientX;
            mouseY = e.clientY;
            if (punteroRef.current) {
                punteroRef.current.style.transform = `translate3d(${mouseX}px, ${mouseY}px, 0)`;
            }
            if (focoRef.current) {
                focoRef.current.style.transform = `translate3d(${mouseX}px, ${mouseY}px, 0)`;
            }
            if (Math.random() < 0.2) {
                crearChispas(mouseX, mouseY, 1);
            }
        };

        const alHacerClic = (e: MouseEvent) => {
            crearChispas(e.clientX, e.clientY, 16);
        };

        const alPasarSobreElemento = (e: MouseEvent) => {
            const objetivo = e.target as HTMLElement | null;
            sobreInteractivo = Boolean(
                objetivo?.closest("button, a, input, select, .tarjeta")
            );
            if (punteroRef.current) {
                punteroRef.current.classList.toggle("interactivo", sobreInteractivo);
            }
        };

        window.addEventListener("mousemove", alMover);
        window.addEventListener("mousedown", alHacerClic);
        window.addEventListener("mouseover", alPasarSobreElemento);

        const dibujarRombo = (ctx: CanvasRenderingContext2D, r: number) => {
            ctx.beginPath();
            ctx.moveTo(0, -r * 1.3);
            ctx.lineTo(r * 0.85, 0);
            ctx.lineTo(0, r * 1.3);
            ctx.lineTo(-r * 0.85, 0);
            ctx.closePath();
            ctx.fill();
        };

        const dibujarEstrella = (ctx: CanvasRenderingContext2D, r: number) => {
            ctx.beginPath();
            for (let i = 0; i < 8; i++) {
                const radio = i % 2 === 0 ? r * 1.4 : r * 0.35;
                const ang = (i * Math.PI) / 4;
                if (i === 0) ctx.moveTo(Math.cos(ang) * radio, Math.sin(ang) * radio);
                else ctx.lineTo(Math.cos(ang) * radio, Math.sin(ang) * radio);
            }
            ctx.closePath();
            ctx.fill();
        };

        let animacionId = 0;
        const animar = () => {
            aroX += (mouseX - aroX) * 0.16;
            aroY += (mouseY - aroY) * 0.16;

            if (aroRef.current) {
                const escala = sobreInteractivo ? 1.45 : 1;
                aroRef.current.style.transform = `translate3d(${aroX}px, ${aroY}px, 0) scale(${escala})`;
                aroRef.current.classList.toggle("activo", sobreInteractivo);
            }

            if (ctx) {
                ctx.clearRect(0, 0, ancho, alto);
                for (let i = particulas.length - 1; i >= 0; i--) {
                    const p = particulas[i];
                    p.x += p.vx;
                    p.y += p.vy;
                    p.vy += 0.035;
                    p.rotacion += p.vRotacion;
                    p.vida -= 0.024;

                    if (p.vida <= 0) {
                        particulas.splice(i, 1);
                        continue;
                    }

                    ctx.save();
                    ctx.translate(p.x, p.y);
                    ctx.rotate(p.rotacion);
                    ctx.globalAlpha = Math.max(0, p.vida);
                    ctx.fillStyle = p.color;
                    ctx.shadowBlur = 10;
                    ctx.shadowColor = p.color;

                    const radioActual = p.tamano * (0.4 + p.vida * 0.6);
                    if (p.tipo === "rombo") {
                        dibujarRombo(ctx, radioActual);
                    } else {
                        dibujarEstrella(ctx, radioActual);
                    }
                    ctx.restore();
                }
            }

            animacionId = requestAnimationFrame(animar);
        };

        animacionId = requestAnimationFrame(animar);

        return () => {
            cancelAnimationFrame(animacionId);
            window.removeEventListener("resize", ajustarTamano);
            window.removeEventListener("mousemove", alMover);
            window.removeEventListener("mousedown", alHacerClic);
            window.removeEventListener("mouseover", alPasarSobreElemento);
        };
    }, []);

    return (
        <>
            <div ref={focoRef} className="cursor-foco" aria-hidden="true" />
            <canvas ref={canvasRef} className="cursor-canvas" aria-hidden="true" />
            <div ref={aroRef} className="cursor-aro" aria-hidden="true" />

            {/* Guante blanco de mayordomo de gala */}
            <div ref={punteroRef} className="cursor-puntero" aria-hidden="true">
                {/* Estado normal: Mano enguantada elegante */}
                <svg
                    className="guante-normal"
                    width="34"
                    height="34"
                    viewBox="0 0 36 36"
                    fill="none"
                >
                    {/* Guante blanco con dedos relajados */}
                    <path
                        d="M11 6.5C11 5.1 12.1 4 13.5 4C14.9 4 16 5.1 16 6.5V14H16.5V5.5C16.5 4.1 17.6 3 19 3C20.4 3 21.5 4.1 21.5 5.5V14H22V7C22 5.7 23 4.7 24.3 4.7C25.6 4.7 26.6 5.7 26.6 7V15.5H27V10.5C27 9.3 27.9 8.4 29.1 8.4C30.3 8.4 31.2 9.3 31.2 10.5V20.5C31.2 25.2 27.5 28.5 22.5 28.5H15.5C12.2 28.5 9.8 26.4 8.2 23.4L5.6 18.5C4.9 17.2 5.4 15.6 6.7 14.9C7.9 14.3 9.4 14.7 10.1 15.9L11 17.5V6.5Z"
                        fill="url(#gradGuante)"
                        stroke="#2a0811"
                        strokeWidth="1.5"
                        strokeLinejoin="round"
                    />
                    {/* Tres costuras doradas clásicas en el dorso del guante */}
                    <line x1="15.5" y1="18" x2="15.5" y2="23" stroke="#bfa054" strokeWidth="1.2" strokeLinecap="round" />
                    <line x1="19.5" y1="17.5" x2="19.5" y2="23" stroke="#bfa054" strokeWidth="1.2" strokeLinecap="round" />
                    <line x1="23.5" y1="18" x2="23.5" y2="23" stroke="#bfa054" strokeWidth="1.2" strokeLinecap="round" />
                    {/* Puño de esmoquin borgoña y gemelo dorado */}
                    <rect x="11.5" y="27.5" width="15" height="5.5" rx="2" fill="#6e1429" stroke="#d4af37" strokeWidth="1.3" />
                    <circle cx="23.5" cy="30.2" r="1.4" fill="#f3d37a" />
                    <defs>
                        <linearGradient id="gradGuante" x1="6" y1="4" x2="30" y2="28" gradientUnits="userSpaceOnUse">
                            <stop offset="0%" stopColor="#ffffff" />
                            <stop offset="75%" stopColor="#f5efe6" />
                            <stop offset="100%" stopColor="#dfd3c3" />
                        </linearGradient>
                    </defs>
                </svg>

                {/* Estado interactivo: Mano de mayordomo señalando con el índice */}
                <svg
                    className="guante-senalar"
                    width="34"
                    height="34"
                    viewBox="0 0 36 36"
                    fill="none"
                >
                    <path
                        d="M12.5 3.5C12.5 2.1 13.6 1 15 1C16.4 1 17.5 2.1 17.5 3.5V13.5H18.2C19.5 13.5 20.5 14.5 20.5 15.8V16.5H21.2C22.5 16.5 23.5 17.5 23.5 18.8V19.5H24.2C25.5 19.5 26.5 20.5 26.5 21.8V23.5C26.5 27.3 23.4 29.5 19.5 29.5H14.5C11.4 29.5 9.2 27.6 7.8 24.8L5.4 20.2C4.7 18.9 5.2 17.3 6.5 16.6C7.7 16 9.2 16.4 9.9 17.6L12.5 21.5V3.5Z"
                        fill="url(#gradGuante2)"
                        stroke="#2a0811"
                        strokeWidth="1.5"
                        strokeLinejoin="round"
                    />
                    {/* Costuras del dorso */}
                    <line x1="15" y1="18.5" x2="15" y2="23.5" stroke="#bfa054" strokeWidth="1.2" strokeLinecap="round" />
                    <line x1="18.5" y1="19" x2="18.5" y2="23.5" stroke="#bfa054" strokeWidth="1.2" strokeLinecap="round" />
                    {/* Puño de gala */}
                    <rect x="10.5" y="28.5" width="14" height="5.5" rx="2" fill="#941b37" stroke="#f3d37a" strokeWidth="1.4" />
                    <circle cx="21.5" cy="31.2" r="1.5" fill="#f3d37a" />
                    <defs>
                        <linearGradient id="gradGuante2" x1="6" y1="2" x2="26" y2="29" gradientUnits="userSpaceOnUse">
                            <stop offset="0%" stopColor="#ffffff" />
                            <stop offset="80%" stopColor="#f7f1e8" />
                            <stop offset="100%" stopColor="#e2d5c3" />
                        </linearGradient>
                    </defs>
                </svg>
            </div>
        </>
    );
}