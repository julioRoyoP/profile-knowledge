# profile-knowledge

Colección de demos técnicas **standalone** que ilustran patrones de
arquitectura/backend en **Java 21 / Spring Boot 3.x**. Cada demo se entiende por sí
sola leyendo su README y su código.

> ⚠️ Este repositorio es **material de lectura/consulta**: no se ejecuta ni se
> despliega.

## Demos disponibles

| Demo | Ilustra |
|------|---------|
| [`saga-pattern/`](./saga-pattern) | Orquestación de transacciones distribuidas con compensación en orden inverso. |
| [`outbox-pattern/`](./outbox-pattern) | Consistencia eventual para efectos secundarios asíncronos, con reintentos y sin duplicados. |
| [`payment-abstraction/`](./payment-abstraction) | Inversión de dependencias (Gateway pattern): proveedores de pago intercambiables. |
| [`security-jwt-keycloak/`](./security-jwt-keycloak) | OAuth2 Resource Server: validación de JWT y mapeo de roles desde un IdP externo. |

## Visualización

Este repositorio se explora de forma navegable (estilo IDE, con razonamiento técnico
por fichero) desde el portfolio personal:
**[ → profile-display-ten.vercel.app](https://profile-display-ten.vercel.app)**
