# ADR-0002: PostgreSQL con JPA detrás de un adaptador

- **Estado:** Aceptada
- **Fecha:** 2026-10-22

## Contexto

Las candidaturas deben sobrevivir a reinicios y cada cambio de estado debe guardarse junto con su entrada de historial. La misma ficha puede editarse desde dos pestañas, así que una escritura obsoleta no puede sobrescribir otra más reciente.

## Decisión

- PostgreSQL como base de datos y Flyway para versionar el esquema; Hibernate solo lo valida.
- JPA (Hibernate y Spring Data JPA) confinado en `adapter/persistence`, con entidades propias separadas del modelo de dominio.
- `ApplicationStore` es el puerto de salida para cargar y guardar. `ApplicationQueries` es el contrato de lectura que usa la web, implementado con proyecciones; las lecturas no pasan por casos de uso porque no aplican reglas.
- `UnitOfWork`, implementado con `TransactionTemplate`, delimita la transacción desde el caso de uso sin anotaciones de Spring.
- Concurrencia optimista: versión esperada en cada guardado, `@Version` en la entidad y un contador técnico que marca la fila padre cuando solo cambian sus colecciones.

## Alternativas consideradas

- **Usar las entidades JPA como modelo de dominio.** Ahorra traducciones, pero introduce el ORM en el núcleo y permite que colecciones diferidas lleguen a la vista.
- **JDBC directo.** Control total del SQL, con bastante más código de traducción.
- **H2 en las pruebas.** Arranque más simple, pero no comprueba el dialecto ni las garantías reales de PostgreSQL.

## Consecuencias

**Positivas**

- Estado e historial se confirman en la misma transacción.
- Los conflictos de edición se detectan incluso entre la lectura y la escritura.
- El listado consulta solo las columnas que necesita y no carga colecciones.

**Negativas**

- Entrevistas, tareas e historial se guardan como colecciones dependientes: cada guardado las reescribe, y el coste crece con fichas muy largas.
- El historial no es event sourcing ni una auditoría inalterable.

## Revisar cuando

El volumen de actividades vuelve costosas las escrituras, las actividades adquieren vida propia o aparecen otras entradas con necesidades de consistencia distintas. Medir SQL y tiempos antes de cambiar el almacenamiento.
