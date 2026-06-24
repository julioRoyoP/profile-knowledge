package dev.julioroyo.knowledge.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security as an OAuth2 <em>Resource Server</em>: the backend validates
 * JWTs issued by an external identity provider (Keycloak) and authorizes by
 * role. It never sees passwords and keeps no session — every request carries its
 * own bearer token.
 *
 * <p>The issuer/JWK URIs live in configuration (e.g.
 * {@code spring.security.oauth2.resourceserver.jwt.issuer-uri:
 * https://<idp-host>/realms/demo-realm}); no real URL is hardcoded here.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Stateless: no server session, CSRF protection is unnecessary for a
            // token-authenticated API.
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public, read-only catalog browsing.
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                // Health/info probes open to the platform.
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Writing to the catalog requires an admin.
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                // The admin area is admin-only across all methods.
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Everything else just needs a valid, authenticated token.
                .anyRequest().authenticated())
            // Resource server: validate the JWT and map its roles to authorities.
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtRoleConverter())));

        return http.build();
    }
}
