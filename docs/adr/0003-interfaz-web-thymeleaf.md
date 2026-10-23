# ADR-0003: Interfaz web con Spring MVC y Thymeleaf

- **Estado:** Aceptada
- **Fecha:** 2026-10-22

## Contexto

La aplicación se usa desde el navegador para listados, filtros, fichas y formularios. No hay clientes móviles ni consumidores externos de una API.

## Decisión

- HTML renderizado en el servidor con Thymeleaf y patrón POST/Redirect/GET.
- Modelos de formulario propios, distintos del dominio y de las entidades JPA.
- Controladores pequeños por pantalla u operación (`OverviewController`, `ApplicationFormController`, `StatusController`, `ActivityController`) que solo traducen HTTP a casos de uso o consultas.
- Una única hoja de estilos (`app.css`) con tipografía del sistema, sin recursos externos. Etiquetas de estado con texto, foco visible, controles de al menos 44 px y contraste mínimo de 4,5:1.

## Alternativas consideradas

- **SPA con API REST.** Interacción más rica y despliegue independiente del cliente, a cambio de herramientas JavaScript, autenticación entre aplicaciones y más contratos.
- **Solo API, sin interfaz.** Menos trabajo inicial, pero no cubre el uso diario.
- **Biblioteca de componentes.** Más controles preparados, pero obliga a asumir sus recursos y convenciones.

## Consecuencias

**Positivas**

- Un solo proceso sirve toda la aplicación y funciona sin JavaScript.
- Una API futura puede reutilizar los mismos casos de uso.

**Negativas**

- La navegación se basa en recargas.
- Accesibilidad y adaptación a móvil se mantienen a mano y se verifican con el recorrido de navegador.

## Revisar cuando

Aparece un cliente externo o una interfaz con mucho estado en el navegador. Comprobar antes si basta con mejoras de presentación.
