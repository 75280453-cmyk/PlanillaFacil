# PlanillaFácil

Sistema de planilla para microempresas peruanas (1 a 10 trabajadores).
Proyecto final de Construcción de Software.

## Equipo

| Integrante | Rol |
|---|---|
| Joe | Jefe de Proyecto, estructura y reglas de negocio |
| Marina | Planificación |
| Miguel | Boleta, pruebas |
| Gabriel | Frontend y API KEYS|
## Arquitectura

Por capas: `dominio` → `aplicacion` → `infraestructura`.

```
src/main/java/pe/edu/continental/planillafacil/
├── dominio/
│   ├── modelo/        records y enums
│   ├── excepcion/     excepciones propias
│   └── CalculadoraPlanilla.java
├── aplicacion/        (en desarrollo)
└── infraestructura/   (en desarrollo)
```

## Cómo ejecutar las pruebas

```
mvn test
```

Requisitos: JDK 26 y Maven.

## Estado del proyecto

| Hito | Estado |
|---|---|
| Dominio y cálculo de planilla | En progreso |
| Aplicación (puertos y servicio) | Pendiente |
| Persistencia (JPA) | Pendiente |
| Interfaz | Pendiente |
| Boletas PDF y reportes | Pendiente |

## Pruebas

Ver [docs/pruebas.md](docs/pruebas.md).

## Fuera de alcance (mejoras futuras)

Quinta categoría, gratificaciones, CTS y conexión con SUNAT (T-Registro y PLAME).
