package dev.julioroyo.knowledge.saga.service;

import dev.julioroyo.knowledge.saga.model.SagaContext;

/**
 * Un único paso reversible dentro de una saga. Cada paso sabe ejecutar su acción
 * (execute) y deshacerla (compensate). El orquestador depende solo de este
 * contrato, nunca de pasos concretos.
 */
public interface SagaStep {

    /** Identificador estable en kebab-case que se usa al persistir el estado del paso. */
    String name();

    /** Realiza la acción; lanza una excepción si no puede completarse, para disparar la compensación. */
    void execute(SagaContext context);

    /** Deshace lo que hizo execute. Debe ser idempotente y tolerante a estado parcial. */
    void compensate(SagaContext context);
}
