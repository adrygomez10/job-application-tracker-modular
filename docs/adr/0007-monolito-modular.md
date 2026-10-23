# ADR-0007: Monolito modular con Maven y ArchUnit

- Estado: Aceptada
- Fecha: 2026-10-23

## Contexto

El proyecto necesita incorporar capacidades con cambios localizados.

## Decisión

Ocho módulos, APIs mínimas y un ejecutable. ArchUnit comprueba acceso a internos y ciclos; Maven construye el grafo.

## Alternativas

Mantener un único módulo con convenciones; separar artefactos API/implementación; JPMS.

## Consecuencias

Más POM y configuración de pruebas. Los tipos públicos siguen accesibles si están en el classpath.

## Revisar cuando

El coste de verificar fronteras o el riesgo de uso accidental exigen encapsulación adicional.
