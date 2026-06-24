# profile-knowledge

Demos técnicas **standalone** que ilustran patrones de arquitectura/backend
aplicados en proyectos reales, simplificados para poder entenderse **sin contexto
externo**: leyendo solo el README de cada carpeta y su código.

Cada demo es código **Java 21 / Spring Boot 3.x** real e idiomático (records,
sealed interfaces, switch con pattern matching, `Optional`, streams), pero reducido
a lo esencial del patrón: sin proyectos, paquetes, esquemas de base de datos ni
infraestructura concreta de ningún sistema en producción.

> ⚠️ **Este repositorio NO se ejecuta ni se despliega.** Es **material de lectura**.
> No hay `pom.xml` ni estructura Maven: los ficheros se leen, no se compilan. Las
> dependencias externas (base de datos, proveedor de pago, IdP, canal de
> notificación) se representan con abstracciones propias de cada demo, no con
> implementaciones reales.

## Demos disponibles

| Demo | Ilustra |
|------|---------|
| [`saga-pattern/`](./saga-pattern) | Orquestación de transacciones distribuidas con **compensación manual** en orden inverso (alternativa a 2PC cuando los sistemas no comparten transacción). |
| [`outbox-pattern/`](./outbox-pattern) | **Consistencia eventual** para efectos secundarios asíncronos: el evento se persiste en la transacción de negocio y se entrega con reintentos/backoff, sin duplicados. |
| [`payment-abstraction/`](./payment-abstraction) | **Inversión de dependencias** (Gateway pattern): proveedores de pago intercambiables sin tocar el código consumidor. |
| [`security-jwt-keycloak/`](./security-jwt-keycloak) | **OAuth2 Resource Server**: validación de JWT y mapeo de roles desde un IdP externo (Keycloak), sin gestionar contraseñas ni sesiones. |

Cada carpeta sigue el mismo formato de README: **Contexto de uso real** · **Qué
resuelve** · **Cómo navegar el código**.

## Visualización

Este repositorio se explora de forma navegable (estilo IDE, con razonamiento técnico
curado por fichero) desde el portfolio personal:
<!-- TODO: reemplazar con la URL real del portfolio -->
**[ → enlace al portfolio (pendiente) ]**
