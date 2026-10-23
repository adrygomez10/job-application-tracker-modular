# ADR-0001: Monolito con arquitectura hexagonal

- **Estado:** Aceptada
- **Fecha:** 2026-10-22

## Contexto

El producto tiene un único usuario y un proceso principal: seguir candidaturas. Las reglas de negocio (fechas de solicitud, motivos de reapertura, historial) se comparten entre operaciones y deben poder verificarse sin arrancar la base de datos ni el servidor web. El código de cada operación tiene que ser fácil de localizar y el ORM no debe condicionar el modelo.

## Decisión

Un único ejecutable Spring Boot en un único módulo Maven, organizado con arquitectura hexagonal:

- `domain`: modelo y reglas, sin dependencias de frameworks.
- `application`: un caso de uso por operación, agrupados en `details`, `status` y `activities`, sin dependencias entre esos paquetes. Los puertos de salida (`ApplicationStore`, `UnitOfWork`) y el protocolo de versión (`VersionedUpdate`) se comparten desde la raíz del paquete.
- `adapter/web` y `adapter/persistence`: adaptadores de entrada y salida.
- `configuration`: punto único de composición.

Los casos de uso son clases finales que reciben sus colaboradores por constructor. Las fronteras se verifican con ArchUnit.

## Alternativas consideradas

- **Paquetes por capa técnica (controller/service/repository).** Familiar, pero dispersa cada operación y no impide por sí solo dependencias en sentido contrario.
- **Varios módulos Maven.** El compilador impondría las fronteras, a cambio de una construcción y una configuración más complejas.
- **Microservicios.** Añaden red, despliegue y coordinación de datos sin que exista necesidad de desplegar o escalar por separado.
- **Un servicio de aplicación con todas las operaciones.** Menos clases, pero acumula colaboradores y dependencias de todo el producto.

## Consecuencias

**Positivas**

- Las reglas se prueban sin Spring y cada caso de uso se lee y se prueba por separado.
- El sentido de las dependencias está verificado por tests y no depende de la disciplina del equipo.

**Negativas**

- Más clases y traducciones entre el modelo y los adaptadores.
- Las fronteras solo se detectan al ejecutar ArchUnit, no al compilar.

## Revisar cuando

Varios equipos necesitan entregas independientes, aparecen funcionalidades con fronteras propias o ArchUnit deja de bastar para contener las dependencias. Medir el problema antes de separar módulos o servicios.
