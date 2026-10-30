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

También están disponibles `make quality` y `make test`. Se comprueban Spotless con el perfil de estilo del proyecto, Checkstyle, PMD, duplicados entre módulos, fronteras con ArchUnit y cobertura con JaCoCo. Los informes se generan por módulo bajo `target/`; GitHub Actions ejecuta los mismos scripts.

El [perfil de estilo inspirado en Clean Code](docs/code-style.md) combina `config/checkstyle-clean-code.xml` y `config/eclipse-formatter.xml`: cuatro espacios, hasta 120 columnas, llaves de métodos y constructores en la línea siguiente y cadenas multilínea con una llamada por línea. Maven lo aplica y comprueba sin depender de un IDE. Para verificar solo el estilo: `docker compose -f compose.style.yaml run --rm --no-deps check`. Para corregirlo: `docker compose -f compose.style.yaml run --rm --no-deps format`. La guía incluye la instalación del hook de precommit y los comandos del Maven Wrapper.

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

## Despliegue temporal en AWS

Terraform declara la infraestructura; Helm declara los recursos Kubernetes.
La entrega se activa expresamente después de preparar plataforma, certificado
y credenciales. Los datos del laboratorio son ficticios.

### Controles del código de despliegue

Bash usa dos espacios, nombres snake_case y bloques separados por intención;
shfmt comprueba el formato con `-i 2 -ci -bn`, y ShellCheck revisa errores de shell.
Terraform usa su formato nativo de dos espacios. YAML y JSON usan dos espacios;
Helm y kubeconform verifican los manifiestos renderizados. Las acciones están
fijadas a SHA completos y actionlint comprueba los workflows.

Los auxiliares Python siguen PEP 8 y PEP 257: cuatro espacios, 79 columnas,
comillas dobles, imports explícitos ordenados y firmas anotadas. Ruff comprueba
estilo, imports, nombres, docstrings y complejidad ciclomática máxima 10; mypy
comprueba los contratos en modo estricto. La regla propia de una clase por archivo
se verifica mediante AST, sin importar ni ejecutar los módulos. Las pruebas del
script de secretos usan procesos simulados; no necesitan Docker ni acceso AWS.
La revisión manual complementa estos controles de formato, diseño y comportamiento.

Instalar el hook después de clonar. En PowerShell:

```powershell
python -m venv .venv-style
.\.venv-style\Scripts\python.exe -m pip install -r requirements-style.txt
.\.venv-style\Scripts\python.exe -m pre_commit install
.\.venv-style\Scripts\python.exe scripts/cloud/check.py
```

En Linux o Git Bash con el entorno correspondiente:

```bash
python3 -m venv .venv-style
.venv-style/bin/python -m pip install -r requirements-style.txt
.venv-style/bin/python -m pre_commit install
.venv-style/bin/python scripts/cloud/check.py
```

En Git Bash de Windows, el intérprete del entorno está en `.venv-style/Scripts/python.exe`.
Los ejecutables portables locales pueden guardarse en `.tools/aws-workshop/`,
ignorado por Git; el comprobador añade esa carpeta a PATH. Instalar Terraform,
Helm y los validadores con las versiones de la tabla siguiente y verificar sus
checksums oficiales. `scripts/cloud/install-ci-tools.sh` instala actionlint,
kubeconform, ShellCheck y shfmt en Ubuntu usando versiones y SHA-256 fijados;
Terraform y Helm los instalan sus acciones de CI.

El precommit ejecuta estilo Java, calidad Java con pruebas unitarias y ArchUnit,
y el comprobador cloud cuando cambian archivos de su ámbito. Usa el contenido
preparado para commit y protege temporalmente los cambios sin preparar mediante
pre-commit. CI conserva un control Code style independiente, la suite completa
de Java y el mismo `scripts/cloud/check.py`; la entrega espera a los tres jobs.
### Qué vamos a comprobar

Un cambio supera calidad y pruebas, produce una imagen identificada por digest,
llega a EKS y responde por HTTPS. Una segunda entrega conserva una candidatura
ficticia. Al terminar se eliminan la aplicación, sus datos y la infraestructura.

El diagrama del recorrido es:

```text
GitHub Actions: calidad + tests + validación de infraestructura
                           ↓
                construir → ECR → digest
                           ↓ OIDC / IAM / RBAC
                    Helm → EKS → aplicación → PostgreSQL/EBS
Navegador → ALB/HTTPS ──────────────↑
```

Terraform crea los recursos AWS. Helm declara los recursos Kubernetes. El
controlador de balanceo convierte el Ingress en un ALB y EBS CSI crea el disco del
PVC. Son dos niveles de infraestructura: hay que retirar primero lo creado por
los controladores para que Terraform pueda retirar después el clúster y su red.

### Límites y coste

La aplicación tiene una réplica y estrategia Recreate: actualizar puede cerrar
sesiones e interrumpir brevemente el servicio. Sus recordatorios son tareas locales;
replicar sin coordinación puede ejecutar el mismo trabajo varias veces. PostgreSQL
tiene una réplica y un volumen de una zona. Dos nodos no convierten ese diseño en
una instalación con alta disponibilidad.

El laboratorio usa dos subredes públicas sin NAT Gateway, sin SSH y con IMDSv2
obligatorio. Los nodos tienen IP pública para descargar imágenes y comunicarse con
AWS; no se abre el puerto de la aplicación ni PostgreSQL a Internet. El ALB y sus
reglas de destino los administra el controlador. La aplicación procesa las cabeceras de protocolo solo desde las direcciones de
la VPC del ALB y conserva el Host original; no confía en X-Forwarded-Host.
El endpoint público de EKS admite
runners alojados de GitHub y exige IAM y RBAC. Una instalación permanente necesita
revisar estas decisiones, la gestión de sesiones y las copias de datos.

EKS en soporte estándar cuesta 0,10 USD/h: 2,40 USD por 24 horas. Reservar inicialmente
6–10 USD por una jornada pequeña, antes de créditos e impuestos; confirmar región,
instancias, ALB, EBS, IPv4, logs y duración en AWS Pricing Calculator. Es una
estimación, no un tope. Los tiempos de creación y retirada también cuentan.

Comprobar plan, saldo, expiración y cobertura de créditos en la cuenta real. No
se da por confirmado que toda la configuración esté disponible en Free account
plan. El tipo de nodo también depende del plan: t3.medium puede no estar admitido en
Free account plan. En ese caso, comprobar un tipo x86_64 con al menos 4 GiB permitido
por el plan (por ejemplo, c7i-flex.large si está disponible), cambiar instance_type
y recalcular el coste. No cambiar automáticamente a Paid plan.
Si aparece una exigencia de Paid plan, revisar antes sus condiciones:
puede generar cargos fuera de créditos. Los avisos de Budgets no cortan el gasto
instantáneamente. Crear el clúster después de preparar y verificar el código.

### 1. Preparar el equipo, sin encender EKS

Los comandos siguientes se ejecutan desde la raíz del repositorio en Bash.
En Windows sirven Git Bash y Python; los comandos de lectura interactiva y de
continuación de línea de esta guía no son sintaxis PowerShell.

| Herramienta | Versión del ejercicio |
| --- | --- |
| Docker y Compose | Docker en ejecución, Compose v2 |
| Terraform | 1.17.0 |
| Proveedor AWS | 6.70.0, lock file incluido |
| Helm | 4.3.1 |
| kubectl | 1.35.8 para el clúster 1.35; comprobar compatibilidad |
| AWS CLI | v2 |
| Python | 3.12 |
| jq, curl, sha256sum | Disponibles en la terminal |
| ShellCheck | 0.11.0 |
| shfmt | 3.14.1 |
| Ruff | 0.17.3 |
| mypy | 2.4.1 |
| pre-commit | 4.7.0 |
| actionlint | 1.7.12 |
| kubeconform | 0.8.0 |

Instalar desde las distribuciones oficiales y comprobar sus checksums. El workflow
instala sus herramientas con versiones fijas; las actions están fijadas a commits.
El script install-ci-tools.sh es para runners Ubuntu y utiliza sudo.

```bash
bash scripts/quality.sh
bash scripts/test.sh
bash scripts/cloud/validate.sh
```

La última orden verifica formato y esquema de Terraform, usa un proveedor AWS
simulado para comprobar límites de IAM, renderiza Helm, valida los manifiestos
contra Kubernetes y rechaza un digest inválido. No crea recursos.

No compartir .env, estados ni planes. El estado de Terraform se mantiene local y
está ignorado por Git. Guardar una copia privada y conservarla hasta completar la
retirada. Para trabajo compartido habría que configurar un backend remoto con
bloqueo; el laboratorio no admite varios operadores a la vez.

### 2. Cuenta, GitHub y sujeto OIDC

Completar el alta de AWS y comprobar Billing, plan, créditos y condiciones. Para la
preparación, usar un usuario IAM administrativo con acceso a la consola y la política
SignInLocalDevelopmentAccess para iniciar sesión desde AWS CLI 2.32 o posterior.
El acceso local usa credenciales temporales; no se crean access keys permanentes.
Configurar MFA en la cuenta según los requisitos de AWS y la política de seguridad.
El rol de Actions se crea después y no recibe permisos administrativos.

```bash
aws configure set region eu-west-1 --profile tracker-workshop
aws login --profile tracker-workshop
export AWS_PROFILE=tracker-workshop
aws sts get-caller-identity --query Arn --output text
```

En PowerShell, la asignación equivalente es `$env:AWS_PROFILE = 'tracker-workshop'`.
Mantener ese perfil al ejecutar Terraform y los scripts; renovar la sesión con
`aws login --profile tracker-workshop` si caduca.

En GitHub, revisar los archivos y subirlos al repositorio. La variable
**de repositorio** AWS_DEPLOY_ENABLED debe quedar ausente o en false.

Crear el environment aws-workshop en Settings → Environments y restringir sus
deployment branches a main. Este control es obligatorio: cuando el sujeto OIDC
incluye un environment, el nombre de rama no forma parte de ese sujeto. Proteger
también main frente a cambios no revisados cuando sea posible.

Ejecutar Actions → Inspect AWS OIDC subject → Run workflow, sobre main. El job
muestra solo iss, aud, sub e identificadores públicos; nunca muestra el token.
Copiar el sub exacto a Terraform, sin comodines. Esto todavía no necesita AWS.

Los repositorios recientes pueden usar identificadores inmutables dentro del sub.
No sustituirlo por el formato de un tutorial antiguo. La audiencia debe ser
sts.amazonaws.com.

### 3. Concretar variables y revisar el plan

```bash
cp infra/terraform.tfvars.example infra/terraform.tfvars
```

Editar ese archivo privado con región, hostname y ARN real de la identidad
administradora. El ARN debe ser de IAM, no el ARN STS de una sesión. Si se usa un
rol de IAM Identity Center, consultar el ARN del rol en IAM. Copiar el sujeto OIDC
del paso anterior. Elegir un subdominio controlado, por ejemplo uno del dominio
existente del portfolio: no es necesario comprar otro dominio ni cambiar el hosting.

Si la cuenta ya tiene token.actions.githubusercontent.com como proveedor OIDC,
establecer create_github_oidc_provider = false. No importar ni destruir un proveedor
compartido como parte del laboratorio.

```bash
aws sts get-caller-identity
aws eks describe-cluster-versions --region eu-west-1
terraform -chdir=infra init
terraform -chdir=infra plan -out=create.tfplan
```

Comprobar que 1.35 continúa en soporte estándar en la región; si se cambia de
versión, actualizar kubectl, validación, etiqueta de Pod Security y pruebas.
Los addons se resuelven mediante su versión compatible al elaborar el plan; ese
plan guarda la selección concreta que se aplicará.

Revisar cuenta y región, dos nodos, dos subredes, ECR, roles, EKS, addons y certificado.
No hay NAT, SSH ni RDS. El plan no contiene la contraseña de la aplicación.
Revisar el resumen del plan y la estimación de coste antes de aplicar.

### 4. Crear infraestructura y validar el certificado

Este paso empieza el consumo de la jornada.

```bash
terraform -chdir=infra apply create.tfplan
terraform -chdir=infra output
```

Terraform solicita un certificado ACM con validación DNS. Mostrar sus registros:

```bash
terraform -chdir=infra output -json certificate_validation_records
```

Crear en el proveedor DNS del dominio los CNAME indicados, respetando si su panel
añade automáticamente el sufijo del dominio. La guía no cambia el DNS existente.
No apuntar todavía el hostname de la aplicación al ALB: aún no existe.

```bash
export AWS_REGION=$(terraform -chdir=infra output -raw region)
export AWS_ACCOUNT_ID=$(terraform -chdir=infra output -raw account_id)
export AWS_CLUSTER_NAME=$(terraform -chdir=infra output -raw cluster_name)
export CERTIFICATE_ARN=$(terraform -chdir=infra output -raw certificate_arn)
aws acm wait certificate-validated --region "$AWS_REGION" --certificate-arn "$CERTIFICATE_ARN"
bash scripts/cloud/bootstrap.sh
```

bootstrap configura kubectl, namespace, StorageClass y RBAC, e instala el controlador
ALB 3.6.2 verificando su archivo por checksum. La política IAM vendorizada proviene
de esa misma versión. Mantener versión, política y CRDs coherentes al actualizar;
Helm no actualiza automáticamente los CRDs de una versión anterior.

### Política del controlador ALB

`infra/policies/alb-controller.json` conserva sin modificaciones la
[política oficial del controlador v3.6.2](https://github.com/kubernetes-sigs/aws-load-balancer-controller/blob/v3.6.2/docs/install/iam_policy.json).
El chart Helm usa la misma versión y bootstrap comprueba el SHA-256 del archivo.
Revisar política, chart y CRDs juntos al actualizar. Algunas acciones de consulta
de la política necesitan Resource "*"; esta política pertenece al rol de la cuenta
de servicio del controlador, separado del rol de GitHub Actions.
### 5. Crear las credenciales del laboratorio

Construir localmente la imagen para generar el hash con la herramienta del proyecto.
No ejecutar setup ni reutilizar el .env local:

```bash
docker build --target runtime -t job-application-tracker-modular:local .
python scripts/cloud/create_secret.py \
  --context "arn:aws:eks:$AWS_REGION:$AWS_ACCOUNT_ID:cluster/$AWS_CLUSTER_NAME"
```

El script pide usuario y contraseña, genera una contraseña aleatoria de PostgreSQL
y crea el Secret tracker-runtime mediante stdin. No escribe un archivo de
credenciales. Si ya existe el Secret, se detiene: cambiar POSTGRES_PASSWORD después
de inicializar un volumen no cambia automáticamente la contraseña dentro de la base.

El namespace aplica Pod Security restricted. La aplicación y PostgreSQL ejecutan
sin root, sin escalada de privilegios y con sistema de archivos raíz de solo lectura;
los volúmenes proporcionan las rutas que sí necesitan escritura.

### 6. Activar la entrega en GitHub

Crear estas variables en el environment aws-workshop:

| Variable | Valor |
| --- | --- |
| AWS_REGION | terraform output -raw region |
| AWS_ACCOUNT_ID | terraform output -raw account_id |
| AWS_CLUSTER_NAME | terraform output -raw cluster_name |
| AWS_ROLE_ARN | terraform output -raw delivery_role_arn |
| ECR_REPOSITORY | terraform output -raw ecr_repository_name |
| TRACKER_HOST | terraform output -raw app_hostname |
| CERTIFICATE_ARN | terraform output -raw certificate_arn |

Las órdenes de la tabla se ejecutan con terraform -chdir=infra. Los valores son
configuración, no contraseñas. No se necesita AWS_ACCESS_KEY_ID en GitHub.

Cuando la plataforma, el Secret y el certificado estén listos, establecer la variable
**del repositorio** AWS_DEPLOY_ENABLED = true. Debe ser de repositorio porque se
evalúa antes de entrar al environment.

Ejecutar Verify manualmente sobre main. Primero pasan estilo, tests y validación de
infraestructura; después el job deliver publica la imagen y ejecuta Helm.
También se despliega con cada push posterior a main. Los pull requests solo verifican.

La etiqueta de imagen incluye commit, ejecución e intento para permitir reintentos
con ECR inmutable. La entrega utiliza el digest que ECR devuelve. La comprobación
de HTTPS conecta al ALB con el nombre y SNI correctos, sin desactivar la validación
del certificado; por eso funciona antes de crear el CNAME público de la aplicación.

El análisis ECR se solicita al publicar, pero su resultado asíncrono todavía no
bloquea la entrega. No describirlo como un control de vulnerabilidades obligatorio.

### 7. Acceso, actualización y recuperación

Obtener el hostname del ALB:

```bash
export WORKSHOP_CONTEXT="arn:aws:eks:$AWS_REGION:$AWS_ACCOUNT_ID:cluster/$AWS_CLUSTER_NAME"
kubectl --context "$WORKSHOP_CONTEXT" -n tracker get ingress tracker
```

Crear el CNAME del hostname de la aplicación hacia el ALB y abrir la URL HTTPS.
Registrar una candidatura ficticia y probar navegación, importación y exportación.

Para el segundo despliegue, hacer un cambio pequeño revisable y enviarlo a main.
Comprobar el nuevo commit/digest y que la candidatura sigue en la base. La estrategia
Recreate evita solapar dos instancias de los recordatorios; puede ser necesario
iniciar sesión de nuevo.

Para practicar la recuperación, elegir un cambio que no altere incompatiblemente
el esquema y consultar primero las revisiones:

```bash
helm history tracker --kube-context "$WORKSHOP_CONTEXT" -n tracker
helm rollback tracker NUMERO_DE_REVISION_COMPATIBLE \
  --kube-context "$WORKSHOP_CONTEXT" -n tracker --wait --timeout 10m
kubectl --context "$WORKSHOP_CONTEXT" -n tracker rollout status deployment/tracker-app
```

Comprobar la aplicación por HTTPS después. Helm también solicita rollback automático
si falla la actualización Kubernetes. Un fallo posterior en la comprobación HTTPS
hace fallar el workflow y requiere diagnóstico; no revierte automáticamente la base.

Antes de una migración importante se necesita una copia y una prueba de restauración.
En este ejercicio solo hay datos ficticios. El rollback de código no deshace Flyway.
El rol de entrega puede leer los Secrets del namespace por cómo Helm guarda sus
revisiones; por eso se limita a main y al environment protegido.

### 8. Cerrar el laboratorio

1. Cambiar AWS_DEPLOY_ENABLED a false y esperar a que termine cualquier entrega.
2. Conservar las versiones, los resultados y el digest; no guardar secretos.
3. Ejecutar la retirada en la cuenta y región correctas:

```bash
bash scripts/cloud/cleanup.sh
```

El script pide escribir DELETE-DISPOSABLE-DATA. Retira el Ingress con el controlador
activo, desinstala el chart, elimina expresamente el PVC retenido y espera la
desaparición del PV y del volumen EBS. Comprueba que no queden balanceadores en la VPC. Genera el plan
de destrucción y lo aplica tras esa confirmación. Solo apunta a recursos de este
estado y del laboratorio.

Si algo falla, conservar el estado, leer el error y corregirlo antes de repetir.
No eliminar finalizers a ciegas: pueden estar protegiendo un ALB o un disco
que todavía existe. No destruir el clúster mientras sus controladores sean
necesarios para retirar esos recursos.

Después comprobar en la consola: EKS, instancias, ALB/target groups, EBS/snapshots,
EIP/NAT, ECR y CloudWatch en la región utilizada. Los nodos se retiran con su grupo;
los discos raíz tienen delete_on_termination y el certificado lo gestiona Terraform.
El Secret desaparece con el clúster. Retirar también los CNAME creados manualmente
para la aplicación y validación del certificado.

Apagar máquinas no elimina el clúster ni su tarifa. Billing puede tardar en reflejar
el último consumo: revisar el coste final cuando se haya actualizado y capturarlo.
Conservar la evidencia del destroy y de la revisión del inventario.

[Decisión y límites de la arquitectura](docs/adr/0014-entrega-temporal-en-eks.md).

## Código de partida

Este taller evoluciona el [gestor con arquitectura hexagonal](https://github.com/adrygomez10/job-application-tracker-hexagonal).
