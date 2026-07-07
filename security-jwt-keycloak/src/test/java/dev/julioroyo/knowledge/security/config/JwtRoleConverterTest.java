package dev.julioroyo.knowledge.security.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Cubre la única lógica de seguridad propia: extraer el claim anidado
 * realm_access.roles de Keycloak y convertirlo en authorities con prefijo ROLE_,
 * incluido el caso en que el claim no existe. El Jwt se construye a mano, sin
 * contexto de Spring ni IdP.
 */
class JwtRoleConverterTest {

    private final JwtRoleConverter converter = new JwtRoleConverter();

    private Jwt jwtWith(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "none");
        claims.forEach(builder::claim);
        return builder.build();
    }

    @Test
    void shouldMapRealmRolesToPrefixedAuthorities() {
        Jwt jwt = jwtWith(Map.of(
                "sub", "user-1",
                "realm_access", Map.of("roles", List.of("ADMIN", "CATALOG_EDITOR"))));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_CATALOG_EDITOR");
    }

    @Test
    void shouldYieldNoAuthoritiesWhenRealmAccessClaimIsMissing() {
        Jwt jwt = jwtWith(Map.of("sub", "user-1"));

        AbstractAuthenticationToken token = converter.convert(jwt);

        assertThat(token.getAuthorities()).isEmpty();
    }

    @Test
    void shouldKeepTheValidatedJwtAsThePrincipal() {
        Jwt jwt = jwtWith(Map.of(
                "sub", "user-1",
                "realm_access", Map.of("roles", List.of("USER"))));

        JwtAuthenticationToken token = (JwtAuthenticationToken) converter.convert(jwt);

        assertThat(token.getToken()).isSameAs(jwt);
    }
}
