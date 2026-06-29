package dev.julioroyo.knowledge.security.web;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tiny controller showing the rules from {@code SecurityConfig} in practice and
 * how to read the validated token. The endpoints are illustrative stubs — the
 * point is the access boundary, not the business logic.
 *
 * <ul>
 *   <li>{@code GET  /api/products} — public (permitAll).</li>
 *   <li>{@code POST /api/products} — requires {@code ROLE_ADMIN}.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public List<String> list() {
        return List.of("keyboard", "mouse", "monitor");
    }

    @PostMapping
    public String create(@AuthenticationPrincipal Jwt jwt) {
        // The Jwt is the validated token; claims are read straight from it.
        String username = jwt.getClaimAsString("preferred_username");
        return "product created by " + username;
    }
}
