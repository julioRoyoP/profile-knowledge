package dev.julioroyo.knowledge.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada ejecutable de la demo security-jwt-keycloak. Arranca una API
 * REST protegida como OAuth2 Resource Server. A diferencia de las otras demos no
 * trae un CommandLineRunner: el flujo de demostración son peticiones HTTP reales
 * con un token emitido por Keycloak, descritas en el README.
 */
@SpringBootApplication
public class SecurityDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityDemoApplication.class, args);
    }
}
