# API web de PlanillaFácil

Contrato entre el backend (`infraestructura/web`) y el frontend (`frontend/`).
Describe lo que **existe hoy en `ApiPlanilla`**. Si el código cambia, este documento se actualiza.

- **URL base:** `http://localhost:8080` (el frontend la usa a través del proxy de Vite).
- **Formato:** todo viaja en JSON (UTF-8), salvo el PDF de la boleta.
- **Cuerpos de entrada:** objetos JSON **planos** (sin objetos ni listas dentro).
- **Fechas:** `"YYYY-MM-DD"`. **Montos y porcentajes:** números con decimales (`1500`, `2200.50`).

## Errores

Todos los errores tienen la misma forma:

```json
{ "error": "Ya existe un trabajador registrado con el DNI 70112233" }
```

| Código | Cuándo ocurre |
|---|---|
| 200 | Todo bien |
| 201 | Se creó algo (trabajador, período) |
| 400 | Dato inválido o faltante (DNI de 8 dígitos, campo obligatorio, id no numérico, fecha mal escrita) |
| 404 | La ruta no existe, o el trabajador / período no existe |
| 405 | Método incorrecto para esa ruta (por ejemplo `DELETE`) |
| 422 | Se incumple una regla de negocio (DNI repetido, período cerrado, período duplicado) |
| 500 | Error inesperado del servidor |

---

## Resumen de rutas

| Método y ruta | Para qué sirve |
|---|---|
| `GET /api/salud` | Comprobar que el servidor responde |
| `GET /api/sistemas-pension` | Listar ONP y AFP (para el desplegable del formulario) |
| `GET /api/trabajadores` | Listar trabajadores activos |
| `POST /api/trabajadores` | Registrar un trabajador |
| `PUT /api/trabajadores/{id}` | Actualizar un trabajador |
| `GET /api/periodos` | Listar los períodos (meses) |
| `POST /api/periodos` | Abrir un período |
| `POST /api/periodos/{id}/cerrar` | Cerrar un período |
| `GET /api/asistencias?idPeriodo=` | Listar asistencias de un período |
| `POST /api/asistencias` | Registrar faltas y tardanzas |
| `POST /api/planilla/calcular` | Calcular las boletas de un período |
| `GET /api/planilla/resumen?idPeriodo=` | Totales del mes |
| `GET /api/boletas?idPeriodo=` | Listar boletas de un período |
| `GET /api/boletas/{id}/pdf` | Descargar la boleta en PDF |
| `GET /api/parametros` | Listar parámetros (ONP, EsSalud) |
| `PUT /api/parametros/{codigo}` | Cambiar el valor de un parámetro |
| `GET /api/tasas-pension` | Listar tasas de AFP |
| `PUT /api/tasas-pension/{id}` | Cambiar una tasa |

---

## Sistemas de pensión

### `GET /api/sistemas-pension`

```json
[
  { "idSistemaPension": 1, "nombre": "ONP", "tipo": "ONP" },
  { "idSistemaPension": 2, "nombre": "AFP Integra", "tipo": "AFP" }
]
```

## Trabajadores

Objeto trabajador (lo que devuelve la API):

```json
{
  "id": 1,
  "dni": "70112233",
  "nombres": "Ana",
  "apellidos": "Torres",
  "cargo": "Asistente",
  "fechaIngreso": "2024-03-01",
  "sueldoBasico": 1500,
  "sistemaPension": "ONP",
  "entidadPension": "ONP",
  "tipoComision": null,
  "estado": "ACTIVO"
}
```

### `GET /api/trabajadores`
Devuelve una lista de objetos trabajador. Solo incluye los de estado `ACTIVO`.

### `POST /api/trabajadores` → 201
Entrada:

```json
{
  "dni": "45678912",
  "nombres": "Luis",
  "apellidos": "Rojas",
  "cargo": "Vendedor",
  "fechaIngreso": "2023-08-15",
  "sueldoBasico": 2200,
  "idSistemaPension": 2,
  "sistemaPension": "AFP",
  "entidadPension": "AFP Integra",
  "tipoComision": "FLUJO"
}
```

| Campo | Obligatorio | Reglas |
|---|---|---|
| `dni` | Sí | Exactamente 8 dígitos; no puede repetirse (422) |
| `nombres`, `apellidos`, `cargo` | Sí | Texto no vacío |
| `fechaIngreso` | Sí | `YYYY-MM-DD`, no puede ser futura |
| `sueldoBasico` | Sí | Mayor que cero |
| `idSistemaPension` | Sí | Id tomado de `GET /api/sistemas-pension` |
| `sistemaPension` | Sí | `"ONP"` o `"AFP"` (debe coincidir con el sistema elegido) |
| `entidadPension` | No | Si falta, se usa `"ONP"` o `"AFP"` |
| `tipoComision` | Solo AFP | `"FLUJO"` o `"MIXTA"`. En ONP se envía `null` o no se envía |
| `estado` | No | `"ACTIVO"` (por defecto) o `"CESADO"` |

Salida: el objeto trabajador creado, con su `id`.

### `PUT /api/trabajadores/{id}` → 200
Mismo cuerpo que el `POST`: se envían **todos** los campos. Devuelve el trabajador actualizado.
Para dar de baja a alguien, enviar `"estado": "CESADO"`; deja de aparecer en el listado.
Si el `id` no existe, responde 404.

## Períodos

Objeto período: `{ "idPeriodo": 1, "anio": 2026, "mes": 11, "estado": "ABIERTO" }`
(`estado` es `"ABIERTO"` o `"CERRADO"`).

### `GET /api/periodos`
Lista de períodos.

### `POST /api/periodos` → 201
Entrada: `{ "anio": 2026, "mes": 11 }`. Devuelve el período creado.
Si ya existe ese mes, responde 422.

### `POST /api/periodos/{id}/cerrar` → 200
Sin cuerpo. Devuelve el período con `estado: "CERRADO"`.
Un período cerrado ya no admite asistencias ni cálculo (422). Cerrarlo dos veces también da 422.

## Asistencias

### `GET /api/asistencias?idPeriodo=1`
```json
[
  { "idAsistencia": 1, "idTrabajador": 2, "trabajador": "Rojas, Luis",
    "idPeriodo": 1, "diasFalta": 2, "minutosTardanza": 30 }
]
```

### `POST /api/asistencias` → 200
Entrada:

```json
{ "idTrabajador": 2, "idPeriodo": 1, "diasFalta": 2, "minutosTardanza": 30 }
```

`diasFalta` va de 0 a 31 y `minutosTardanza` no puede ser negativo. Si ya había una asistencia
para ese trabajador y período, se reemplaza. Devuelve la asistencia guardada.
Errores: 404 si el trabajador o el período no existen; 422 si el período está cerrado.

## Planilla

### `POST /api/planilla/calcular` → 200
Entrada: `{ "idPeriodo": 1 }`. Calcula (o recalcula) una boleta por cada trabajador activo.
Devuelve la lista de boletas generadas.

```json
[
  {
    "idBoleta": 1,
    "idTrabajador": 2,
    "dni": "45678912",
    "trabajador": "Rojas, Luis",
    "idPeriodo": 1,
    "remuneracionBruta": 2200,
    "totalDescuentos": 310.40,
    "netoPagar": 1889.60,
    "aporteEssalud": 198,
    "fechaEmision": "2026-11-26",
    "detalles": [
      { "concepto": "Aporte AFP", "tipo": "DESCUENTO", "porcentaje": 10, "monto": 220 }
    ]
  }
]
```

`tipo` del detalle es `"INGRESO"`, `"DESCUENTO"` o `"APORTE_EMPLEADOR"`.
Los números del ejemplo son ilustrativos; los reales los calcula `CalculadoraPlanilla`.
Errores: 404 si el período no existe; 422 si está cerrado.

### `GET /api/planilla/resumen?idPeriodo=1`
```json
{
  "anio": 2026, "mes": 11, "cantidadBoletas": 2,
  "totalRemuneraciones": 3700, "totalDescuentos": 520.40,
  "totalNeto": 3179.60, "totalEssalud": 333
}
```

## Boletas

### `GET /api/boletas?idPeriodo=1`
Lista de boletas, con la misma forma que las de `calcular`.

### `GET /api/boletas/{id}/pdf`
Devuelve el archivo PDF (`Content-Type: application/pdf`), no JSON.
Si la boleta no existe, responde 404 con el JSON de error.

## Parámetros y tasas

### `GET /api/parametros`
```json
[ { "idParametro": 1, "codigo": "ONP", "valor": 13.00,
    "descripcion": "Aporte ONP del trabajador (%)", "vigenteDesde": "2026-01-01" } ]
```
Códigos existentes: `ONP` y `ESSALUD`.

### `PUT /api/parametros/{codigo}`
Entrada: `{ "valor": 13, "descripcion": "Aporte ONP del trabajador (%)" }`. Devuelve el parámetro actualizado.

### `GET /api/tasas-pension`
```json
[ { "idTasa": 1, "idSistemaPension": 2, "sistemaPension": "AFP Integra",
    "tipoComision": "FLUJO", "pctAporte": 10, "pctPrima": 1.37, "pctComision": 1.55,
    "pctTotal": 12.92, "vigenteDesde": "2026-01-01", "vigenteHasta": null } ]
```

### `PUT /api/tasas-pension/{id}`
Entrada: `{ "pctAporte": 10, "pctPrima": 1.37, "pctComision": 1.55 }`. Devuelve la tasa actualizada.

---

## Flujo típico desde la pantalla

1. `GET /api/sistemas-pension` para llenar el desplegable del formulario de trabajador.
2. `GET /api/periodos`; si no hay ninguno del mes actual, `POST /api/periodos`.
3. `POST /api/trabajadores` para registrar al personal.
4. `POST /api/asistencias` por cada trabajador con faltas o tardanzas.
5. `POST /api/planilla/calcular` y luego `GET /api/planilla/resumen`.
6. `GET /api/boletas/{id}/pdf` para imprimir cada boleta.
7. `POST /api/periodos/{id}/cerrar` al terminar el mes.

> La base de datos arranca **sin trabajadores ni períodos**: solo trae ONP, AFP Integra, las tasas
> y los parámetros. Hay que crear un período antes de registrar asistencias o calcular.

## Puntos a confirmar con Joe

- Los montos del ejemplo de boleta son ilustrativos; conviene reemplazarlos por una salida real.
- No existe `GET /api/trabajadores/{id}` ni un listado de cesados.
- Si la boleta de un PDF no existe, el servicio lanza una regla de negocio y la API la traduce a 404 (el resto de reglas da 422).
