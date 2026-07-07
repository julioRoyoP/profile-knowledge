package dev.julioroyo.knowledge.outbox;

import dev.julioroyo.knowledge.outbox.model.OutboxEvent;
import dev.julioroyo.knowledge.outbox.model.OutboxStatus;
import dev.julioroyo.knowledge.outbox.repository.OutboxRepository;
import dev.julioroyo.knowledge.outbox.service.OrderConfirmationService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada ejecutable de la demo outbox-pattern. @EnableScheduling activa
 * el relay @Scheduled y @EnableAsync el dispatcher @Async. Al arrancar confirma un
 * pedido de ejemplo (encolando un evento) y deja correr el sistema hasta que se
 * entrega, mostrando el ciclo PENDING → PROCESSING → SENT; luego cierra el contexto.
 */
@Slf4j
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class OutboxDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OutboxDemoApplication.class, args);
    }

    @Bean
    CommandLineRunner demo(OrderConfirmationService orders,
                           OutboxRepository outbox,
                           ConfigurableApplicationContext context) {
        return args -> {
            log.info("================ DEMO: entrega vía outbox ================");

            orders.confirmOrder("order-42");

            OutboxStatus finalStatus = awaitTerminalStatus(outbox, Duration.ofSeconds(20));

            log.info("Estado final del evento: {}", finalStatus);
            log.info("================ Demo finalizada ================");

            context.close();
        };
    }

    /**
     * Sondea el repositorio hasta que el (único) evento de la demo alcanza un
     * estado terminal SENT o FAILED o se agota el tiempo,
     * logueando cada cambio de estado que observa.
     */
    private OutboxStatus awaitTerminalStatus(OutboxRepository outbox, Duration timeout) throws InterruptedException {
        Instant deadline = Instant.now().plus(timeout);
        OutboxStatus lastSeen = null;

        while (Instant.now().isBefore(deadline)) {
            List<OutboxEvent> events = outbox.findAll();
            if (!events.isEmpty()) {
                OutboxEvent event = events.get(0);
                if (event.status() != lastSeen) {
                    log.info("Evento {} -> {}", event.id(), event.status());
                    lastSeen = event.status();
                }
                if (event.status() == OutboxStatus.SENT || event.status() == OutboxStatus.FAILED) {
                    return event.status();
                }
            }
            Thread.sleep(200);
        }
        return lastSeen;
    }
}
