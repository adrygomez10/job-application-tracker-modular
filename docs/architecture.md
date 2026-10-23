# Arquitectura del taller 2

Un ejecutable, una base de datos y ocho módulos Maven. Cada capacidad conserva su lógica y adaptadores. Dominio y aplicación no dependen de Spring; las configuraciones conectan implementaciones mediante inyección por constructor.

| Módulo | Contrato y responsabilidad |
|---|---|
| tracker-applications | `ApplicationsApi`: registro de ofertas, proyecciones de candidaturas, entrevistas, actividades y estadísticas. Conserva reglas, transacciones y persistencia existentes. |
| tracker-imports | Tubería sin escrituras, sesión de previsualización, confirmación por fila y registro idempotente. |
| tracker-exports | `ExportPlugin`, `ExportDescriptor`, `ExportData`; registro y fuentes de datos. |
| tracker-export-csv | Implementa el contrato y genera CSV. |
| tracker-export-ics | Implementa el contrato y genera iCalendar. |
| tracker-reminders | Política con reloj, almacenamiento de avisos y trabajo programado. |
| tracker-insights | Consulta estadísticas mediante `ApplicationsApi` y calcula la presentación de la tasa. |
| tracker-app | Arranque, seguridad, recursos comunes y composición. |

Importaciones, exportaciones, recordatorios y estadísticas dependen de la API de candidaturas. Los plugins dependen solo de los contratos de exportación en su código. Maven puede hacer visibles dependencias transitivas: ArchUnit impide usar esas implementaciones desde otros módulos.

`ApplicationsApi` define el contrato entre módulos y `ApplicationsFacade`, situada en `adapter.module`, lo implementa adaptando las llamadas a los casos de uso y consultas existentes. Los consumidores utilizan la interfaz y la composición proporciona la implementación. La fachada evita repartir los detalles internos de candidaturas; añade mapeos y debe mantenerse libre de políticas de otros módulos. La elección y sus alternativas se documentan en [ADR-0013](adr/0013-fachada-entre-modulos.md). No se exige una fachada por cada componente.

Las interfaces públicas no devuelven entidades JPA ni `JobApplication`. Cada módulo escribe sus tablas y los consumidores no consultan tablas ajenas. La composición puede conocer implementaciones para conectarlas. No se añade un módulo genérico `common` con reglas de negocio.

La reserva de la clave importada y el registro de la candidatura comparten una transacción local. PostgreSQL arbitra las confirmaciones concurrentes con una clave única y `ON CONFLICT DO NOTHING`. La política permite resultados parciales entre filas, pero no una candidatura huérfana de registro o viceversa.

Los límites Maven y ArchUnit son diferentes. `BoundaryDemonstrationTest` muestra que un tipo ausente del classpath no compila y que un tipo público disponible puede ser rechazado por una regla de arquitectura. La segunda comprobación usa una clase inválida exclusivamente de pruebas.
