package dev.julioroyo.knowledge.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configura Spring Security como OAuth2 Resource Server: el backend valida los
 * JWT emitidos por un IdP externo (Keycloak) y autoriza por rol, sin gestionar
 * contraseñas ni sesiones. Cada petición llega con su propio bearer token. El
 * issuer y el JWK viven en configuración externa, nunca hardcodeados.
 */
@Slf4j
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configurando Resource Server OAuth2 (stateless, autorización por rol)");
        http
            // Sin estado: no hay sesión de servidor, así que la protección CSRF sobra.
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Catálogo público de solo lectura.
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                // Sondas de salud/info abiertas a la plataforma.
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Escribir en el catálogo requiere administrador.
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                // El área de admin es solo de administradores en cualquier método.
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // El resto solo exige un token válido y autenticado.
                .anyRequest().authenticated())
            // Resource server: valida el JWT y mapea sus roles a authorities.
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtRoleConverter())));

        return http.build();
    }
}
