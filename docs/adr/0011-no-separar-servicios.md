# ADR-0011: Mantener recordatorios y estadísticas en el monolito

- Estado: Aceptada
- Fecha: 2026-10-23

## Contexto

Las fronteras nuevas invitan a convertir módulos en servicios.

## Decisión

Conservar un proceso: no hay equipos con ritmos independientes ni escalado distinto que compensen la red.

## Alternativas

Servicio HTTP de avisos; proceso trabajador separado; proveedor de notificaciones.

## Consecuencias

Se comparten memoria, recursos y despliegue. Un fallo del proceso afecta a todas las capacidades.

## Revisar cuando

Medidas demuestran necesidades sostenidas de aislamiento, capacidad o despliegue independiente. Un nuevo cliente por sí solo no basta.
