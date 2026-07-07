# Security JWT + Keycloak — OAuth2 Resource Server

> Demo standalone de un patrón de arquitectura backend. Código Java/Spring Boot
> real y simplificado, pensado para leerse y también **ejecutable** de forma
> autónoma (ver [Cómo ejecutar](#cómo-ejecutar)).

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
- **Sin URLs ni realms reales en el código**: el `issuer-uri` y el realm
  (`demo-realm`) viven en configuración externa, nunca hardcodeados.

## Cómo ejecutar

Esta demo es un proyecto Maven autónomo (Java 21 / Spring Boot 3.x). A diferencia
del resto del repositorio, **es una app web que necesita un Keycloak accesible**:
el Resource Server descarga el JWK del `issuer-uri` al arrancar y sin un IdP real
no hay tokens que validar. No hay `CommandLineRunner`: el flujo de demostración son
peticiones HTTP con un token real, descritas abajo.

```bash
mvn test              # corre los tests (no necesita Keycloak)
mvn spring-boot:run   # arranca la API en http://localhost:8080 (necesita Keycloak)
```

El `issuer-uri` por defecto apunta a `http://localhost:8080/realms/demo-realm` y se
puede sobreescribir con la variable de entorno `KEYCLOAK_ISSUER_URI`.

> Los pasos concretos para levantar Keycloak con Docker, crear el realm/usuario y
> obtener un token están en `README.local.md` (no versionado).

### Peticiones de prueba

Con la API arrancada y un `$TOKEN` obtenido de Keycloak:

```bash
# GET público: no necesita token, siempre 200
curl http://localhost:8080/api/products
# -> ["keyboard","mouse","monitor"]

# POST sin token: 401 Unauthorized
curl -i -X POST http://localhost:8080/api/products

# POST con token de un usuario SIN rol ADMIN: 403 Forbidden
curl -i -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN"

# POST con token de un usuario CON rol ADMIN: 200 OK
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN"
# -> product created by <preferred_username>
```

| Petición | Sin token | Token sin ADMIN | Token con ADMIN |
|----------|:---------:|:---------------:|:---------------:|
| `GET /api/products`  | 200 | 200 | 200 |
| `POST /api/products` | 401 | 403 | 200 |

El `POST` con ADMIN lee el `preferred_username` del token validado
(`@AuthenticationPrincipal Jwt`) y lo devuelve, mostrando cómo se accede a los
claims sin volver a consultar al IdP.

## Cómo navegar el código

Lee los ficheros en este orden:

1. **`SecurityConfig.java`** — empieza aquí: el `SecurityFilterChain` con las reglas
   de acceso y el cableado del Resource Server.
2. **`JwtRoleConverter.java`** — la pieza específica de Keycloak: de
   `realm_access.roles` a authorities de Spring.
3. **`ProductController.java`** — las reglas en la práctica y cómo leer el token
   validado (`@AuthenticationPrincipal Jwt`).
4. **`SecurityDemoApplication.java`** — punto de entrada ejecutable; arranca la API
   sin escenario automático (el flujo son las peticiones HTTP de arriba).

> **Nota de simplificación**: la demo no incluye un Keycloak real ni credenciales;
> el `issuer-uri` y el realm (`demo-realm`) se resuelven por configuración. Lo
> relevante del patrón es el cableado del Resource Server y el mapeo de roles, no la
> infraestructura concreta del IdP.
