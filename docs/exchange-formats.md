# Importación y exportación

## CSV de ofertas

Formato propio del taller, no la exportación real de un portal. UTF-8 (se admite BOM), coma, cabecera y comillas CSV. Máximo 1 MiB y 1.000 registros. Las cabeceras pueden cambiar de orden. No se admiten nombres desconocidos ni duplicados; el archivo ilegible se rechaza completo.

| Campo | Regla |
|---|---|
| source | Obligatorio, hasta 160 caracteres. Se recorta y convierte a minúsculas con Locale.ROOT. |
| external_id | Obligatorio, hasta 180 caracteres. Se recorta conservando mayúsculas/minúsculas. |
| company | Obligatorio, hasta 160 caracteres. |
| role | Obligatorio, hasta 180 caracteres. |
| url | Opcional, hasta 2.000 caracteres; HTTP(S), host válido y sin credenciales. |
| location | Opcional, hasta 160 caracteres. |
| work_mode | Opcional: UNSPECIFIED, REMOTE, HYBRID u ONSITE; se normaliza a mayúsculas. Vacío = UNSPECIFIED. |
| technologies | Opcional, hasta 500 caracteres. |

Los campos se recortan. Las ofertas se crean en SAVED sin fecha de solicitud, contacto ni notas personales. La previsualización utiliza filtros sin efectos externos y caduca a los 15 minutos; el contenido se conserva en sesión, no en campos ocultos. Confirmar vuelve a validar el estado actual y guarda cada fila válida por separado.

Resultados técnicos: NEW, REJECTED, DUPLICATE, CONFLICT, IMPORTED, PENDING. El número corresponde al registro de datos CSV, no a la línea física: una celda puede contener saltos de línea.

Clave: `(source normalizada, external_id)`. Una huella SHA-256 de los campos normalizados originales permite distinguir repetición de cambio. No se compara con las ediciones personales vigentes ni se sobrescriben. Las claves repetidas dentro del mismo archivo usan el mismo criterio. Las filas pendientes tras un fallo se revisan mediante una nueva previsualización.

Un archivo sin registros es válido y no guarda nada. No se buscan duplicados por similitud de empresa o puesto ni se cruzan portales: eso exigiría una política distinta.

## Plantilla de prueba

[examples/offers-template.csv](../examples/offers-template.csv) contiene dos registros válidos y las ocho cabeceras admitidas. El README explica cómo añadir un tercero y comprobar nuevas filas, repeticiones y conflictos. El mismo fichero se descarga desde la pantalla **Importar ofertas** mediante `/imports/template`, con autenticación. Maven lo incluye como recurso desde `examples/`, sin mantener otra copia editable.

El ejemplo `examples/offers.csv` del taller incluye además una fila inválida intencionada; la plantilla de prueba permite empezar con todas las filas válidas.

## CSV de candidaturas

Plugin applications-csv: id, company, role, status, work_mode, applied_on, updated_at. Incluye todas las coincidencias de los filtros del listado y su orden estable, con fecha de solicitud ISO y fechas de actualización ISO en UTC. Es un formato de salida, no una copia de seguridad ni el CSV de entrada.

El escape de CSV conserva comas, comillas y saltos de línea. Los textos que podrían interpretarse como fórmulas se anteponen con apóstrofo. Esa transformación se documenta y no debe utilizarse para reconstruir una copia exacta de datos arbitrarios.

## ICS de entrevistas

Plugin interviews-ics: entrevistas SCHEDULED de candidaturas no archivadas. Las fechas del formulario son inclusivas en la zona configurada; internamente se consulta un intervalo de instantes `[desde, hasta)`. Sin fechas se utiliza hoy y el mes siguiente.

UID estable basado en el identificador de entrevista; DTSTAMP de generación y DTSTART en UTC. Texto escapado, CRLF y plegado a 75 octetos sin cortar caracteres UTF-8. El modelo no tiene hora de fin: no se añaden DTEND ni DURATION. Las canceladas y completadas se excluyen. Reimportar el archivo en un calendario depende de ese cliente: no se promete sincronización ni retirada de citas antiguas.
