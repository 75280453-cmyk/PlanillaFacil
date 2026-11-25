-- PlanillaFácil: esquema de base de datos (PostgreSQL)
-- Porcentajes en escala "10 = 10 %". Montos en soles con 2 decimales.

CREATE TABLE empresa (
                         id_empresa    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         ruc           CHAR(11)     NOT NULL UNIQUE,
                         razon_social  VARCHAR(150) NOT NULL,
                         direccion     VARCHAR(200)
);

CREATE TABLE sistema_pension (
                                 id_sistema_pension BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 nombre             VARCHAR(60) NOT NULL UNIQUE,
                                 tipo               VARCHAR(3)  NOT NULL CHECK (tipo IN ('ONP', 'AFP'))
);

CREATE TABLE tasa_pension (
                              id_tasa             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              id_sistema_pension  BIGINT       NOT NULL REFERENCES sistema_pension (id_sistema_pension),
                              tipo_comision       VARCHAR(10)  NOT NULL CHECK (tipo_comision IN ('FLUJO', 'MIXTA')),
                              pct_aporte          NUMERIC(5,2) NOT NULL CHECK (pct_aporte   BETWEEN 0 AND 100),
                              pct_prima           NUMERIC(5,2) NOT NULL CHECK (pct_prima    BETWEEN 0 AND 100),
                              pct_comision        NUMERIC(5,2) NOT NULL CHECK (pct_comision BETWEEN 0 AND 100),
                              vigente_desde       DATE         NOT NULL,
                              vigente_hasta       DATE,
                              CHECK (vigente_hasta IS NULL OR vigente_hasta >= vigente_desde)
);

CREATE TABLE parametro_general (
                                   id_parametro   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                   codigo         VARCHAR(30)   NOT NULL,
                                   valor          NUMERIC(8,2)  NOT NULL CHECK (valor >= 0),
                                   descripcion    VARCHAR(150),
                                   vigente_desde  DATE          NOT NULL,
                                   UNIQUE (codigo, vigente_desde)
);

CREATE TABLE trabajador (
                            id_trabajador       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            id_empresa          BIGINT         REFERENCES empresa (id_empresa),
                            dni                 CHAR(8)        NOT NULL UNIQUE,
                            nombres             VARCHAR(80)    NOT NULL,
                            apellidos           VARCHAR(80)    NOT NULL,
                            cargo               VARCHAR(80),
                            fecha_ingreso       DATE           NOT NULL,
                            sueldo_basico       NUMERIC(10,2)  NOT NULL CHECK (sueldo_basico > 0),
                            id_sistema_pension  BIGINT         NOT NULL REFERENCES sistema_pension (id_sistema_pension),
                            tipo_comision       VARCHAR(10)    CHECK (tipo_comision IN ('FLUJO', 'MIXTA')),
                            estado              VARCHAR(10)    NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'CESADO'))
);

CREATE TABLE periodo_planilla (
                                  id_periodo  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                  id_empresa  BIGINT      REFERENCES empresa (id_empresa),
                                  anio        INT         NOT NULL CHECK (anio BETWEEN 2000 AND 2100),
                                  mes         INT         NOT NULL CHECK (mes BETWEEN 1 AND 12),
                                  estado      VARCHAR(10) NOT NULL DEFAULT 'ABIERTO' CHECK (estado IN ('ABIERTO', 'CERRADO')),
                                  UNIQUE (id_empresa, anio, mes)
);

CREATE TABLE asistencia_mensual (
                                    id_asistencia    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                    id_trabajador    BIGINT NOT NULL REFERENCES trabajador (id_trabajador),
                                    id_periodo       BIGINT NOT NULL REFERENCES periodo_planilla (id_periodo),
                                    dias_falta       INT    NOT NULL DEFAULT 0 CHECK (dias_falta BETWEEN 0 AND 31),
                                    minutos_tardanza INT    NOT NULL DEFAULT 0 CHECK (minutos_tardanza >= 0),
                                    UNIQUE (id_trabajador, id_periodo)
);

CREATE TABLE boleta_pago (
                             id_boleta           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             id_trabajador       BIGINT        NOT NULL REFERENCES trabajador (id_trabajador),
                             id_periodo          BIGINT        NOT NULL REFERENCES periodo_planilla (id_periodo),
                             remuneracion_bruta  NUMERIC(10,2) NOT NULL,
                             total_descuentos    NUMERIC(10,2) NOT NULL,
                             neto_pagar          NUMERIC(10,2) NOT NULL,
                             aporte_essalud      NUMERIC(10,2) NOT NULL,
                             fecha_emision       DATE          NOT NULL,
                             ruta_pdf            VARCHAR(300),
                             UNIQUE (id_trabajador, id_periodo),
                             CHECK (remuneracion_bruta - total_descuentos = neto_pagar)
);

CREATE TABLE detalle_boleta (
                                id_detalle  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                id_boleta   BIGINT        NOT NULL REFERENCES boleta_pago (id_boleta) ON DELETE CASCADE,
                                concepto    VARCHAR(80)   NOT NULL,
                                tipo        VARCHAR(20)   NOT NULL CHECK (tipo IN ('INGRESO', 'DESCUENTO', 'APORTE_EMPLEADOR')),
                                porcentaje  NUMERIC(5,2),
                                monto       NUMERIC(10,2) NOT NULL CHECK (monto >= 0)
);

-- ---------- Datos iniciales ----------
-- Porcentajes referenciales de 2026 (los del informe). Verificar y completar con la SBS.

INSERT INTO sistema_pension (nombre, tipo) VALUES
                                               ('ONP', 'ONP'),
                                               ('AFP Integra', 'AFP');

INSERT INTO tasa_pension (id_sistema_pension, tipo_comision, pct_aporte, pct_prima, pct_comision, vigente_desde)
SELECT id_sistema_pension, 'FLUJO', 10.00, 1.37, 1.55, DATE '2026-01-01'
FROM sistema_pension WHERE nombre = 'AFP Integra';

INSERT INTO parametro_general (codigo, valor, descripcion, vigente_desde) VALUES
                                                                              ('ONP',     13.00, 'Aporte ONP del trabajador (%)',        DATE '2026-01-01'),
                                                                              ('ESSALUD',  9.00, 'Aporte EsSalud del empleador (%)',     DATE '2026-01-01');