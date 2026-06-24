# Security JWT + Keycloak — OAuth2 Resource Server

> Demo standalone de un patrón de arquitectura backend. Código Java/Spring Boot
> real y simplificado, pensado para leerse, no para ejecutarse.

## Contexto de uso real

Una API REST necesita proteger sus endpoints **por roles** (catálogo público de
solo lectura, escritura solo para administradores, resto solo para usuarios
autenticados). La autenticación —login, contraseñas, registro— la gestiona un
**servidor de identidad externo** (Keycloak), no el backend. El backend solo recibe
en cada petición un **JWT** (bearer token) y debe **validarlo y mapear sus roles**.

## Qué resuelve

Configurar Spring Security como **OAuth2 Resource Server**: el backend no guarda
contraseñas ni sesiones; cada request llega con su propio token, que el servidor
valida (firma, emisor, expiración) contra el JWK del proveedor y del que extrae los
roles para autorizar.

Decisiones de diseño que la demo transmite:

- **Resource Server, no gestión propia de identidad**: delegar autenticación al IdP
  externo simplifica el backend (sin contraseñas, sin sesiones) y centraliza la
  identidad.
- **Stateless + sin CSRF**: al autenticar por token en cada petición no hay sesión
  de servidor; la protección CSRF, pensada para sesiones con cookies, sobra.
- **`JwtRoleConverter`**: Keycloak no pone los roles donde Spring los busca por
  defecto, sino bajo el claim `realm_access.roles`. El converter alcanza ese claim
  y mapea cada rol a una `GrantedAuthority` con prefijo `ROLE_`, de modo que
  `hasRole("ADMIN")` funciona en la configuración.
- **Reglas de acceso declarativas por ruta/método/rol** (`SecurityConfig`): 5 reglas
  ilustrativas que cubren los casos típicos (público de solo lectura, escritura por
  rol, área de admin, y "cualquier autenticado" como red de seguridad final).
- **Sin URLs ni realms reales**: el `issuer-uri` y el realm (`demo-realm`) viven en
  configuración externa, nunca hardcodeados.

## Cómo navegar el código

Lee los ficheros en este orden:

1. **`SecurityConfig.java`** — empieza aquí: el `SecurityFilterChain` con las reglas
   de acceso y el cableado del Resource Server.
2. **`JwtRoleConverter.java`** — la pieza específica de Keycloak: de
   `realm_access.roles` a authorities de Spring.
3. **`ProductController.java`** — las reglas en la práctica y cómo leer el token
   validado (`@AuthenticationPrincipal Jwt`).

> **Configuración esperada** (no incluida como fichero, va en `application.yml` del
> proyecto que use este patrón; sin valores reales):
>
> ```yaml
> spring:
>   security:
>     oauth2:
>       resourceserver:
>         jwt:
>           issuer-uri: https://<idp-host>/realms/demo-realm
> ```
>
> **Nota de simplificación**: no hay servidor Keycloak real, ni realm, ni URLs, ni
> credenciales. Lo relevante del patrón es el cableado del Resource Server y el
> mapeo de roles, no la infraestructura concreta del IdP.
