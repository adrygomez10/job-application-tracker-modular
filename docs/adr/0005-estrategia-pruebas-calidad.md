# ADR-0005: Pruebas y calidad reproducibles con Docker

- **Estado:** Aceptada
- **Fecha:** 2026-10-22

## Contexto

Arrancar y verificar el proyecto solo debe requerir Git y Docker. Hay que comprobar reglas sin infraestructura, SQL real, seguridad, concurrencia y el recorrido completo en un navegador, y la calidad del código no puede depender de revisiones manuales.

## Decisión

- JUnit y AssertJ para dominio, casos de uso y contratos; ArchUnit para las fronteras.
- Un contrato de `ApplicationStore` que se ejecuta contra la implementación en memoria y contra PostgreSQL.
- Integración separada por riesgo: `JpaApplicationStoreIT`, `PersistenceIT` (transacciones, concurrencia y consultas), `WebIT` (HTTP y seguridad) y `BrowserWorkflowIT` (Playwright con Chromium).
- PostgreSQL real en una base temporal de Docker Compose; JaCoCo para informes de cobertura.
- Spotless (Google Java Style), Checkstyle, PMD y CPD obligatorios en producción y tests. Cualquier infracción hace fallar la construcción.
- El CI ejecuta los mismos scripts que el entorno local.

## Alternativas consideradas

- **Testcontainers.** Adecuado cuando los tests controlan Docker desde el host; dentro de un contenedor exige acceso al socket del motor.
- **SonarQube.** Aporta tendencias y una interfaz centralizada, a costa de otro servicio que mantener.

## Consecuencias

**Positivas**

- Una sola forma de ejecutar la suite, igual en local y en CI.
- Las reglas de calidad están versionadas con el código.

**Negativas**

- La imagen de pruebas es pesada porque incluye Chromium.
- La cobertura indica huecos, no ausencia de fallos; se prueban comportamientos, no getters.

## Revisar cuando

El equipo pasa a trabajar sobre todo con un JDK local, se necesitan dependencias dinámicas por test o el tiempo de construcción empieza a frenar el trabajo.
