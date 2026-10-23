# ADR-0008: Importación con previsualización y transacción por fila

- Estado: Aceptada
- Fecha: 2026-10-23

## Contexto

Un CSV puede contener errores y repetirse desde varias pestañas.

## Decisión

Filtros sin escrituras, confirmación de filas válidas y clave única source/external_id. Reserva y candidatura se guardan juntas; cambios de contenido se presentan como conflictos.

## Alternativas

Lote atómico; actualizar ofertas por coincidencia de textos; guardar durante la previsualización.

## Consecuencias

Puede haber éxito parcial. La transacción local acopla la confirmación al mismo gestor transaccional.

## Revisar cuando

Se necesita una importación masiva, sincronización de fuentes o reconciliación con un proveedor real.
