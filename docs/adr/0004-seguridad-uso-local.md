# ADR-0004: Usuario único y acceso solo local

- **Estado:** Aceptada
- **Fecha:** 2026-10-22

## Contexto

La aplicación guarda nombres de empresas, contactos y notas personales. Se ejecuta en el equipo de su propietario y debe exigir inicio de sesión.

## Decisión

- Spring Security con un único usuario configurado por variables de entorno y contraseña con hash bcrypt; sesión y protección CSRF.
- La aplicación solo escucha en el loopback del host y PostgreSQL no publica puerto.
- Un asistente crea `.env`, que se excluye de Git y de las imágenes Docker.
- Los registros de petición incluyen ID de correlación, método, estado y duración, nunca textos, URL de ofertas ni credenciales.

## Alternativas consideradas

- **Sin inicio de sesión.** Menos configuración, pero deja expuestos los datos a cualquier proceso local.
- **OAuth/SSO o usuarios en base de datos.** Necesarios con varios usuarios; aquí solo añadirían cuentas externas y funcionalidad sin uso.

## Consecuencias

**Positivas**

- Configuración mínima adecuada a un único usuario local.

**Negativas**

- El modo local usa HTTP y no está preparado para exponerse a Internet.
- Copias de seguridad y volúmenes contienen datos sensibles y deben protegerse como archivos personales.

## Revisar cuando

Se plantea el acceso desde otro equipo o desde Internet. Antes de abrir puertos hay que revisar TLS, cookies, control de acceso, recuperación de credenciales, protección frente a intentos repetidos y copias de seguridad.
