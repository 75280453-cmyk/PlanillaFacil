class MotorSonidoCabaret {
    private ctx: AudioContext | null = null;
    private silenciado = false;

    private obtenerContexto(): AudioContext | null {
        if (this.silenciado) return null;
        if (!this.ctx && typeof window !== "undefined") {
            const AudioCtx =
                window.AudioContext ||
                (window as unknown as { webkitAudioContext: typeof AudioContext })
                    .webkitAudioContext;
            if (AudioCtx) this.ctx = new AudioCtx();
        }
        if (this.ctx?.state === "suspended") {
            this.ctx.resume().catch(() => {});
        }
        return this.ctx;
    }

    public alternarSilencio(): boolean {
        this.silenciado = !this.silenciado;
        return this.silenciado;
    }

    public estaSilenciado(): boolean {
        return this.silenciado;
    }

    /** Sonido seco y cerámico de ficha de póker al pulsar botones */
    public tocarFicha(): void {
        const ctx = this.obtenerContexto();
        if (!ctx) return;

        const ahora = ctx.currentTime;
        const frecuencias = [2400, 3850, 5200];

        frecuencias.forEach((freq, i) => {
            const osc = ctx.createOscillator();
            const ganancia = ctx.createGain();

            osc.type = "triangle";
            osc.frequency.setValueAtTime(freq, ahora + i * 0.012);
            osc.frequency.exponentialRampToValueAtTime(
                freq * 0.65,
                ahora + 0.06 + i * 0.012
            );

            ganancia.gain.setValueAtTime(0.08 / (i + 1), ahora + i * 0.012);
            ganancia.gain.exponentialRampToValueAtTime(
                0.0001,
                ahora + 0.075 + i * 0.012
            );

            osc.connect(ganancia);
            ganancia.connect(ctx.destination);

            osc.start(ahora + i * 0.012);
            osc.stop(ahora + 0.08 + i * 0.012);
        });
    }

    /** Deslizamiento rápido de naipe al cambiar de pestaña o abrir tarjetas */
    public repartirCarta(): void {
        const ctx = this.obtenerContexto();
        if (!ctx) return;

        const ahora = ctx.currentTime;
        const duracion = 0.09;
        const bufferSize = Math.floor(ctx.sampleRate * duracion);
        const buffer = ctx.createBuffer(1, bufferSize, ctx.sampleRate);
        const datos = buffer.getChannelData(0);

        for (let i = 0; i < bufferSize; i++) {
            datos[i] = (Math.random() * 2 - 1) * Math.sin((i / bufferSize) * Math.PI);
        }

        const ruido = ctx.createBufferSource();
        ruido.buffer = buffer;

        const filtro = ctx.createBiquadFilter();
        filtro.type = "bandpass";
        filtro.frequency.setValueAtTime(1400, ahora);
        filtro.frequency.exponentialRampToValueAtTime(3200, ahora + duracion);
        filtro.Q.value = 1.8;

        const ganancia = ctx.createGain();
        ganancia.gain.setValueAtTime(0.14, ahora);
        ganancia.gain.exponentialRampToValueAtTime(0.001, ahora + duracion);

        ruido.connect(filtro);
        filtro.connect(ganancia);
        ganancia.connect(ctx.destination);

        ruido.start(ahora);
        ruido.stop(ahora + duracion);
    }

    /** Acorde corto al cambiar entre Gran Cabaret y High Stakes */
    public cambiarEscenario(esPoker: boolean): void {
        const ctx = this.obtenerContexto();
        if (!ctx) return;

        const ahora = ctx.currentTime;
        // Cabaret: acorde cálido mayor (Do-Mi-Sol-Si) | High Stakes: acorde tenso menor (La-Do-Mi-Sol#)
        const notas = esPoker ? [220, 261.63, 329.63, 415.3] : [261.63, 329.63, 392.0, 493.88];

        notas.forEach((freq, idx) => {
            const osc = ctx.createOscillator();
            const ganancia = ctx.createGain();

            osc.type = esPoker ? "sawtooth" : "sine";
            const inicio = ahora + idx * 0.045;
            osc.frequency.setValueAtTime(freq, inicio);

            ganancia.gain.setValueAtTime(0.0001, inicio);
            ganancia.gain.linearRampToValueAtTime(0.035, inicio + 0.03);
            ganancia.gain.exponentialRampToValueAtTime(0.0001, inicio + 0.55);

            osc.connect(ganancia);
            ganancia.connect(ctx.destination);

            osc.start(inicio);
            osc.stop(inicio + 0.58);
        });
    }
}

export const sonidosCabaret = new MotorSonidoCabaret();