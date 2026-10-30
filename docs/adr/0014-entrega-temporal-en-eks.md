# ADR-0014: entrega temporal en EKS

Estado: aceptada para preparar el laboratorio; validación real en AWS pendiente.

## Contexto

El taller debe enseñar CI/CD y Kubernetes en AWS y permanecer activo aproximadamente
una jornada. El repositorio ya cuenta con calidad, pruebas y Docker para desarrollo.
Conservar el entorno local permite reproducir los talleres anteriores.

## Decisión

Usar EKS estándar, dos nodos administrados, ECR, ALB con ACM, Terraform y Helm.
GitHub Actions verifica código y configuración antes de construir y desplegar
una imagen por digest. Un rol OIDC separado del administrador permite publicar
en un único repositorio ECR y entregar dentro del namespace tracker.

Para evitar NAT Gateway en este laboratorio se usan dos subredes públicas.
Los nodos tienen IPv4 públicas, IMDSv2 y ningún acceso SSH. La entrada de la
aplicación se controla mediante el ALB. El API de EKS es público, autenticado
mediante IAM y autorizado mediante RBAC, para admitir runners alojados de GitHub.
Las decisiones de red se revisarían para una instalación permanente.

La aplicación tiene una réplica y estrategia Recreate: sus sesiones son locales
y los recordatorios no coordinan múltiples instancias. PostgreSQL tiene una réplica
y un EBS; el PVC sobrevive a la retirada de Helm y se elimina expresamente al cerrar.

## Alternativas

Compose en EC2 reduciría complejidad y gasto, pero no permite enseñar Kubernetes.
K3s evita la tarifa del plano de control, a cambio de administrarlo. Mantener EKS
una jornada permite practicar Kubernetes gestionado con un gasto reducido.
RDS y réplicas adicionales quedan fuera del primer ejercicio.

## Consecuencias

El despliegue puede interrumpir las sesiones. No se promete alta disponibilidad.
Helm puede volver a una imagen anterior, pero no revertir las migraciones de Flyway.
El rol de entrega puede leer secretos del namespace porque Helm almacena sus
revisiones como Secrets: se confía solo a código revisado de main.
ECR solicita análisis al publicar; ese análisis asíncrono no es un bloqueo de entrega.

El backend de Terraform es local para que el primer laboratorio no necesite
infraestructura adicional de arranque. Su estado y planes están ignorados; hay que
conservarlos de forma privada hasta completar destroy. No permite varios operadores
concurrentes y un entorno compartido necesitaría backend remoto con bloqueo.

La retirada forma parte del ejercicio: detener entregas, retirar ALB y volúmenes
con sus controladores aún activos, destruir Terraform y comprobar el inventario.
