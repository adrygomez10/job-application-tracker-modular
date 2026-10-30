# Registro de decisiones de arquitectura

Cada decisión sigue la misma estructura: contexto, decisión, alternativas consideradas, consecuencias y cuándo revisarla. Una decisión no se edita para cambiarla: se registra una nueva que la sustituye y la anterior pasa a «Sustituida por ADR-XXXX».

| ADR | Decisión | Estado |
|---|---|---|
| [ADR-0001](0001-monolito-hexagonal.md) | Monolito con arquitectura hexagonal | Aceptada |
| [ADR-0002](0002-persistencia-postgresql-jpa.md) | PostgreSQL con JPA detrás de un adaptador | Aceptada |
| [ADR-0003](0003-interfaz-web-thymeleaf.md) | Interfaz web con Spring MVC y Thymeleaf | Aceptada |
| [ADR-0004](0004-seguridad-uso-local.md) | Usuario único y acceso solo local | Aceptada |
| [ADR-0005](0005-estrategia-pruebas-calidad.md) | Pruebas y calidad reproducibles con Docker | Aceptada |
| [ADR-0006](0006-stack-tecnologico.md) | Java 25 y Spring Boot 4 con estándares Jakarta | Aceptada |


## Evolución del taller 2

- [0007 · Monolito modular con Maven y ArchUnit](0007-monolito-modular.md)
- [0008 · Importación con previsualización y transacción por fila](0008-importacion-por-fila.md)
- [0009 · Exportadores registrados en el arranque](0009-exportadores-plugins.md)
- [0010 · Recordatorios como trabajo local con reloj explícito](0010-recordatorios-locales.md)
- [0011 · Mantener recordatorios y estadísticas en el monolito](0011-no-separar-servicios.md)
- [0012 · Proyecciones de consulta en la frontera](0012-consultas-publicas.md)
- [0013 · Fachada de candidaturas para los consumidores de otros módulos](0013-fachada-entre-modulos.md)

- [ADR-0014: entrega temporal en EKS](0014-entrega-temporal-en-eks.md).
