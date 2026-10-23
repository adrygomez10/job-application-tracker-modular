# ADR-0009: Exportadores registrados en el arranque

- Estado: Aceptada
- Fecha: 2026-10-23

## Contexto

Se necesitan CSV de candidaturas e ICS de entrevistas con el mismo mecanismo de selección.

## Decisión

ExportPlugin recibe datos inmutables, no consulta persistencia; un registro rechaza identificadores repetidos.

## Alternativas

Condicionales por formato en el controlador; carga dinámica de JAR.

## Consecuencias

El contrato tabular usa columnas documentadas; un plugin comparte memoria y proceso, sin aislamiento.

## Revisar cuando

Los conjuntos de datos o formatos requieren estructuras que este contrato no expresa con claridad.
