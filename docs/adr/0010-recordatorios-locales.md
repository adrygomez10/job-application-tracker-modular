# ADR-0010: Recordatorios como trabajo local con reloj explícito

- Estado: Aceptada
- Fecha: 2026-10-23

## Contexto

El usuario necesita revisar seguimientos sin depender de proveedores externos.

## Decisión

Recalcular al arrancar y cada minuto; almacenar avisos únicos y resolver los obsoletos de forma atómica.

## Alternativas

Correo externo; calcular solo al abrir una página; servicio separado.

## Consecuencias

La aplicación apagada no avisa. La pantalla puede mostrar datos de la última revisión y debe indicarlo.

## Revisar cuando

Se necesita entrega externa, mayor disponibilidad o retención limitada del historial de avisos.
