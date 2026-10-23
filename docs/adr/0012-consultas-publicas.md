# ADR-0012: Proyecciones de consulta en la frontera

- Estado: Aceptada
- Fecha: 2026-10-23

## Contexto

Exportadores, avisos y estadísticas necesitan datos del gestor sin cargar su historial.

## Decisión

ApplicationsApi ofrece DTO de consulta; candidaturas es dueño del SQL. Las lecturas de varias páginas usan una transacción de lectura repetible.

## Alternativas

Acceder directamente a tablas desde cada consumidor; devolver entidades JPA; cargar cada candidatura completa.

## Consecuencias

Hay más mapeo y consultas específicas. Los contratos de lectura también necesitan pruebas de comportamiento.

## Revisar cuando

El tamaño de las exportaciones necesita streaming, paginación de trabajos o un contrato distinto.
