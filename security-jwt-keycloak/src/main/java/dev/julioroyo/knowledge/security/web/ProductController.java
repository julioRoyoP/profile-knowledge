package dev.julioroyo.knowledge.security.web;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller mínimo que muestra en la práctica las reglas de SecurityConfig y
 * cómo leer el token ya validado. GET /api/products es público; POST
 * /api/products exige ROLE_ADMIN. Los endpoints son ilustrativos: importa el
 * límite de acceso, no la lógica de negocio.
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public List<String> list() {
        return List.of("keyboard", "mouse", "monitor");
    }

    @PostMapping
    public String create(@AuthenticationPrincipal Jwt jwt) {
        // El Jwt es el token validado; los claims se leen directamente de él.
        String username = jwt.getClaimAsString("preferred_username");
        log.info("Producto creado por {}", username);
        return "product created by " + username;
    }
}
