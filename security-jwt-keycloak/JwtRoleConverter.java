package dev.julioroyo.knowledge.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Maps Keycloak's role model onto Spring Security authorities.
 *
 * <p>Keycloak does not put roles where Spring's defaults look ({@code scope} /
 * {@code scp}); it nests realm roles under {@code realm_access.roles}. This
 * converter reaches into that claim and turns each role into a
 * {@code ROLE_}-prefixed {@link GrantedAuthority}, so {@code hasRole(...)} in
 * {@link SecurityConfig} works as expected.
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
