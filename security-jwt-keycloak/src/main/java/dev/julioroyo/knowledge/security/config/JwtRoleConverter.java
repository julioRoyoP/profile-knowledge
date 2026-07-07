package dev.julioroyo.knowledge.security.config;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Traduce el modelo de roles de Keycloak a authorities de Spring Security.
 * Keycloak anida los roles de realm bajo el claim realm_access.roles, donde
 * Spring no los busca por defecto; este converter los extrae y les antepone el
 * prefijo ROLE_ para que hasRole(...) funcione en SecurityConfig.
 */
public class JwtRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String REALM_ACCESS = "realm_access";
    private static final String ROLES = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, extractAuthorities(jwt));
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        return Optional.ofNullable(jwt.getClaimAsMap(REALM_ACCESS))
                .map(realmAccess -> (List<String>) realmAccess.get(ROLES))
                .orElseGet(List::of)
                .stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}
