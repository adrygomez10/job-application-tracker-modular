# ADR-0006: Java 25 y Spring Boot 4 con estándares Jakarta

- **Estado:** Aceptada
- **Fecha:** 2026-10-22

## Contexto

Se necesita un stack Java actual, con soporte a largo plazo y habitual en desarrollo empresarial, sin depender de características experimentales.

## Decisión

Java 25 LTS (Temurin), Maven Wrapper, Spring Boot 4.1.2 con sus dependencias alineadas por el BOM, Jakarta Persistence (Hibernate y Spring Data JPA), Jakarta Validation, PostgreSQL 18.6 y Flyway. Sin características preview.

## Alternativas consideradas

- **Java 21 y Spring Boot 3.** Más extendidos hoy en muchas empresas; se descartan por tener un horizonte de soporte más corto.
- **Plataforma Jakarta EE completa.** Exige otro modelo de ejecución y un servidor compatible, sin necesidad en este producto.

## Consecuencias

**Positivas**

- Versiones fijadas que se pueden reproducir.
- Hibernate y Spring Framework quedan alineados por el BOM, sin forzar versiones sueltas.

**Negativas**

- Las versiones explícitas exigen mantenimiento periódico.

## Revisar cuando

Cambia el soporte de las versiones o una actualización corrige fallos relevantes. Ejecutar la suite completa tras cada actualización. Referencias: [Spring Boot](https://docs.spring.io/spring-boot/system-requirements.html), [Java](https://www.oracle.com/java/technologies/java-se-support-roadmap.html), [Jakarta EE 11](https://jakarta.ee/release/11/) y [PostgreSQL](https://www.postgresql.org/support/versioning/).
