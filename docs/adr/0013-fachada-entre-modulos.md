# ADR-0013: Fachada de candidaturas para los consumidores de otros módulos

- Estado: Aceptada
- Fecha: 2026-10-24

## Contexto

Importaciones necesita registrar ofertas guardadas. Exportaciones, recordatorios y estadísticas necesitan consultas específicas. El módulo de candidaturas ya contiene casos de uso y consultas con tipos y paginación internos. Conectarlos directamente con cada consumidor repartiría el conocimiento de esos detalles entre varios módulos.

## Decisión

Exponer las operaciones necesarias mediante `ApplicationsApi`, con DTO propios de la frontera, e implementar el contrato en `ApplicationsFacade`. Los consumidores dependen de la interfaz; Spring proporciona su implementación. La fachada vive en `adapter.module`, adapta las representaciones públicas y delega en las operaciones existentes.

Para registrar una oferta, convierte `Offer` en `ApplicationDetails` mediante `OfferMapping`, utiliza `RegisterApplication` con estado `SAVED` y sin fecha de solicitud y devuelve el identificador. Para exportar candidaturas, transforma los criterios y reúne las páginas en proyecciones públicas bajo una transacción de lectura repetible.

El dominio y los casos de uso conservan sus reglas e independencia de Spring. Las políticas de importación, formatos de exportación y cálculo de avisos siguen perteneciendo a sus módulos. Esta decisión complementa ADR-0012 y no modifica sus contratos de lectura ni las transacciones de importación de ADR-0008.

## Alternativas consideradas

- Exponer las clases internas directamente: reduce la adaptación inicial, pero acopla cada consumidor a criterios, resultados y reorganizaciones internas.
- Separar desde ahora los contratos de registro y consulta: permite dependencias más pequeñas, con más interfaces y conexiones. Se mantiene una interfaz acotada mientras sus consumidores y operaciones actuales permitan entenderla con facilidad.
- Añadir una fachada a cada componente: introduce delegaciones sin una adaptación o frontera que las justifique. Una operación pública existente puede ser suficiente.

## Consecuencias

Hay una implementación y mapeos adicionales. Las pruebas deben cubrir su adaptación y las garantías de las operaciones públicas. Las dependencias sobre capacidades de candidaturas continúan existiendo: una fachada no permite cambiar su semántica sin revisar a los consumidores.

La fachada no impide por sí misma que alguien importe una implementación pública accesible en el classpath. ArchUnit comprueba el acceso mediante paquetes `api`; Maven controla el grafo de artefactos con los límites ya documentados.

Esta colaboración utiliza llamadas locales dentro del proceso. No exige HTTP, otro despliegue ni aplicar el patrón a todos los módulos.

## Revisar cuando

La interfaz acumule operaciones que sus consumidores no comparten, registro y consulta necesiten evolucionar por separado o la implementación concentre reglas de otras capacidades. En esos casos, revisar responsabilidades y contratos antes de añadir más métodos.
