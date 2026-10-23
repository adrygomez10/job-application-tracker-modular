# Job Application Tracker · Modular

Evolución a monolito modular del [gestor del primer taller](https://adriangm.dev/post/arquitectura-gestor-candidaturas-java-spring).
Un gestor personal local con Java, Spring Boot, Thymeleaf y PostgreSQL. El taller 2 añade un monolito modular, importación CSV con previsualización, exportadores CSV/ICS, recordatorios internos y estadísticas.

## Primer arranque

Necesitas Docker con Compose v2 y el motor en ejecución. Descarga el proyecto o clónalo con Git y abre una terminal en su carpeta raíz. Java y Maven se incluyen en las imágenes de Docker.

En **Windows, con PowerShell**:

```powershell
.\scripts\setup.ps1
docker compose up -d --wait
```

En **Linux, macOS o Git Bash**:

```bash
bash scripts/setup.sh
docker compose up -d --wait
```

El primer comando construye la imagen, pide un usuario y una contraseña y crea el archivo privado `.env`. La contraseña debe tener al menos 12 caracteres y ocupar como máximo 72 bytes en UTF-8. Si `.env` ya existe, el script se detiene para evitar sobrescribirlo; puedes arrancar directamente con el segundo comando.

Abre **http://localhost:8091** e inicia sesión. La base comienza vacía. PostgreSQL conserva los datos en un volumen de Docker y no publica un puerto en el equipo. Puedes cambiar el puerto de la web con `APP_PORT` y la zona horaria con `TRACKER_ZONE` en `.env`; la zona predeterminada es `Europe/Madrid`.

| Operación | Comando |
|---|---|
| Consultar el estado | `docker compose ps` |
| Ver los registros | `docker compose logs --follow --tail=100 app` |
| Detener conservando los datos | `docker compose down` |
| Reconstruir y arrancar tras cambiar el código | `docker compose up -d --build --wait` |

Si utilizas GNU Make y Bash, `make help` muestra los atajos disponibles; `make setup` y `make up` realizan el primer arranque.

## Pruebas y calidad

Las pruebas completas utilizan una base PostgreSQL temporal y Chromium. Sus proyectos Compose están separados del entorno de uso y los scripts los retiran al terminar.

En PowerShell:

```powershell
.\scripts\quality.ps1
.\scripts\test.ps1
```

Con Bash:

```bash
bash scripts/quality.sh
bash scripts/test.sh
```

También están disponibles `make quality` y `make test`. Se comprueban Spotless, Google Java Style, Checkstyle, PMD, duplicados entre módulos, fronteras con ArchUnit y cobertura con JaCoCo. Los informes se generan por módulo bajo `target/`; GitHub Actions ejecuta los mismos scripts.

Para aplicar el formato de manera explícita: `docker compose -f compose.quality.yaml run --rm --build format` o `make format`.

Para trabajar fuera de Docker necesitas JDK 25. `./mvnw test` (`.\mvnw.cmd test` en PowerShell) ejecuta las pruebas unitarias y de arquitectura del reactor; utiliza los scripts anteriores para la suite con PostgreSQL y navegador.

## Uso del taller 2

1. Accede con las credenciales locales.
2. En **Importar ofertas**, carga `examples/offers.csv`: incluye dos ofertas válidas y una fila rechazada. La previsualización no guarda.
3. Confirma las filas válidas. Reimportar el archivo omite las ya guardadas. Cambiar una oferta con la misma clave produce un conflicto, sin sobrescribir datos personales.
4. En **Exportaciones**, descarga todas las candidaturas coincidentes en CSV, o entrevistas programadas del intervalo en ICS.
5. En **Recordatorios**, consulta tareas vencidas y entrevistas en las próximas 24 horas. Se recalculan al arrancar y cada minuto con la aplicación encendida.
6. En **Estadísticas**, consulta estados y tasa de entrevistas, excluyendo archivadas.

Con la aplicación apagada no se producen avisos. No se envían correos ni notificaciones. ICS es una descarga, no una suscripción ni una sincronización de calendario.

## Plantilla para probar la importación

Descarga o copia [examples/offers-template.csv](examples/offers-template.csv). Contiene dos ofertas ficticias válidas, con las ocho cabeceras admitidas. También puedes descargar el mismo fichero desde **Importar ofertas → Descargar plantilla CSV de prueba**.

1. Guarda una copia del fichero como UTF-8, con separador coma, conservando la primera fila de cabeceras.
2. Cárgala en **Importar ofertas** y pulsa **Previsualizar**. En una base sin esas claves aparecerán dos filas nuevas; todavía no se guarda nada.
3. Pulsa **Confirmar filas válidas** para guardarlas. Vuelve a subir el fichero: ambas aparecerán como repetidas, sin crear duplicados.
4. Para añadir otra oferta, agrega una fila con una pareja `source` y `external_id` nueva. Puedes copiar esta fila al final del fichero:

```csv
demo-template,template-003,Another Example,Platform Developer,https://example.test/jobs/template-003,Madrid,HYBRID,"Java, PostgreSQL"
```

Los campos obligatorios son `source`, `external_id`, `company` y `role`. Los opcionales pueden quedar vacíos, manteniendo sus comas; una modalidad vacía se interpreta como `UNSPECIFIED`. Conserva las comillas cuando una celda contenga comas, como en la empresa y las tecnologías del primer ejemplo.

Si ya has importado la plantilla, la previsualización de la copia ampliada mostrará las dos filas repetidas y la nueva oferta. Cambiar los datos de una clave existente produce un conflicto para revisar, sin sobrescribir la candidatura. Las reglas completas y los límites de 1 MiB y 1.000 registros están en [formatos de intercambio](docs/exchange-formats.md).

`examples/offers.csv` se conserva como ejemplo del taller con una fila inválida intencionada. El CSV de exportación sigue siendo un informe de candidaturas y no es un archivo de importación.

## Documentación

- [Mapa y contratos](docs/architecture.md)
- [Formato CSV y exportaciones](docs/exchange-formats.md)
- [Sistema visual de backoffice](docs/design.md)
- [Decisiones ADR](docs/adr/README.md)

## Copias de seguridad

Con la base en ejecución, `.\scripts\backup.ps1` en PowerShell o `bash scripts/backup.sh` guarda una copia en `backups/`.

Para comprobar una copia en una base separada, utiliza `.\scripts\restore.ps1 -Path backups/FILE.dump` o `bash scripts/restore.sh backups/FILE.dump`. El destino predeterminado es `tracker_restore`, sin sustituir la base de la aplicación. Con Make: `make backup` y `make restore FILE="backups/FILE.dump"`.

## Datos existentes y seguridad local

La migración V1 original se mantiene intacta. V2 añade el registro de importaciones y V3 los avisos; ninguna transforma las candidaturas anteriores. El entorno de prueba verifica la actualización desde V1 en un esquema aislado con datos ficticios.

El proyecto nuevo comienza con un volumen vacío. Para usar tus datos, conserva primero una copia del gestor anterior y restaúrala expresamente en el entorno nuevo: no compartas sus volúmenes. Las versiones antiguas deben ejecutarse con sus propias bases; no se garantiza compatibilidad con un esquema posterior.

Se conservan sesión, contraseña bcrypt, CSRF, comprobaciones de versión y registros por petición. No publiques `.env`, copias de la base ni datos personales. La aplicación mantiene el alcance local del primer taller; no incluye una API pública ni clientes web o móviles separados.

## Código de partida

Este taller evoluciona el [gestor con arquitectura hexagonal](https://github.com/adrygomez10/job-application-tracker-hexagonal).
