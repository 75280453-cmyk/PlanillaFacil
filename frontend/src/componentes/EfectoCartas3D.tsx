import { useEffect } from "react";

export function EfectoCartas3D() {
    useEffect(() => {
        const alMover = (e: MouseEvent) => {
            const objetivo = e.target as HTMLElement | null;
            const tarjeta = objetivo?.closest(".tarjeta") as HTMLElement | null;

            // Restablece tarjetas anteriores si salimos de ellas
            document.querySelectorAll<HTMLElement>(".tarjeta.activa-3d").forEach((el) => {
                if (el !== tarjeta) {
                    el.classList.remove("activa-3d");
                    el.style.transform = "";
                }
            });

            if (!tarjeta) return;

            tarjeta.classList.add("activa-3d");
            const rect = tarjeta.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;

            const porcentajeX = x / rect.width;
            const porcentajeY = y / rect.height;

            // Inclinación máxima de 11 grados
            const rotarY = (porcentajeX - 0.5) * 22;
            const rotarX = (0.5 - porcentajeY) * 22;

            tarjeta.style.setProperty("--brillo-x", `${porcentajeX * 100}%`);
            tarjeta.style.setProperty("--brillo-y", `${porcentajeY * 100}%`);
            tarjeta.style.transform = `perspective(700px) rotateX(${rotarX.toFixed(2)}deg) rotateY(${rotarY.toFixed(2)}deg) translateY(-5px) scale(1.02)`;
        };

        const alSalirVentana = () => {
            document.querySelectorAll<HTMLElement>(".tarjeta.activa-3d").forEach((el) => {
                el.classList.remove("activa-3d");
                el.style.transform = "";
            });
        };

        window.addEventListener("mousemove", alMover);
        document.addEventListener("mouseleave", alSalirVentana);

        return () => {
            window.removeEventListener("mousemove", alMover);
            document.removeEventListener("mouseleave", alSalirVentana);
        };
    }, []);

    return null;
}