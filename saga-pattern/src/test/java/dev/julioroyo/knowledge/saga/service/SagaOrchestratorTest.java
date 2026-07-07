package dev.julioroyo.knowledge.saga.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.julioroyo.knowledge.saga.exception.SagaExecutionException;
import dev.julioroyo.knowledge.saga.model.Order;
import dev.julioroyo.knowledge.saga.model.SagaContext;
import dev.julioroyo.knowledge.saga.model.SagaStepRecord;
import dev.julioroyo.knowledge.saga.model.StepStatus;
import dev.julioroyo.knowledge.saga.repository.SagaStateRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Ejercita la garantía central de la saga: ejecución forward en el camino feliz,
 * y compensación en orden inverso cuando un paso falla. Los pasos se mockean para
 * que el test se centre puramente en la lógica de orquestación, no en el efecto
 * de negocio de cada paso.
 */
@ExtendWith(MockitoExtension.class)
class SagaOrchestratorTest {

    private final SagaStateRepository stateRepository = new SagaStateRepository();
    private SagaOrchestrator orchestrator;
    private SagaContext context;

    @Mock private SagaStep reserveStock;
    @Mock private SagaStep chargePayment;
    @Mock private SagaStep confirmShipment;

    @BeforeEach
    void setUp() {
        orchestrator = new SagaOrchestrator(stateRepository);
        context = new SagaContext(new Order("order-1", BigDecimal.TEN));
        when(reserveStock.name()).thenReturn("reserve-stock");
        when(chargePayment.name()).thenReturn("charge-payment");
        when(confirmShipment.name()).thenReturn("confirm-shipment");
    }

    @Test
    void shouldExecuteEveryStepAndCompensateNoneWhenAllSucceed() {
        orchestrator.execute("saga-ok", List.of(reserveStock, chargePayment, confirmShipment), context);

        verify(reserveStock).execute(context);
        verify(chargePayment).execute(context);
        verify(confirmShipment).execute(context);
        verify(reserveStock, never()).compensate(context);
        verify(chargePayment, never()).compensate(context);
        verify(confirmShipment, never()).compensate(context);

        assertThat(stateRepository.history("saga-ok"))
                .extracting(SagaStepRecord::status)
                .containsExactly(
                        StepStatus.STARTED, StepStatus.COMPLETED,
                        StepStatus.STARTED, StepStatus.COMPLETED,
                        StepStatus.STARTED, StepStatus.COMPLETED);
    }

    @Test
    void shouldCompensatePreviousStepsInReverseOrderWhenAStepFails() {
        doThrow(new RuntimeException("carrier down")).when(confirmShipment).execute(context);

        assertThatThrownBy(() ->
                orchestrator.execute("saga-ko", List.of(reserveStock, chargePayment, confirmShipment), context))
                .isInstanceOf(SagaExecutionException.class)
                .extracting(e -> ((SagaExecutionException) e).failedStep())
                .isEqualTo("confirm-shipment");

        // Los pasos ya completados se deshacen del más reciente al más antiguo: pago antes que stock.
        InOrder inOrder = inOrder(reserveStock, chargePayment);
        inOrder.verify(chargePayment).compensate(context);
        inOrder.verify(reserveStock).compensate(context);
        // El paso que falló a mitad de execute nunca se completó, así que no se compensa.
        verify(confirmShipment, never()).compensate(context);
    }

    @Test
    void shouldKeepCompensatingRemainingStepsWhenOneCompensationFails() {
        doThrow(new RuntimeException("carrier down")).when(confirmShipment).execute(context);
        doThrow(new RuntimeException("refund gateway timeout")).when(chargePayment).compensate(context);

        assertThatThrownBy(() ->
                orchestrator.execute("saga-partial", List.of(reserveStock, chargePayment, confirmShipment), context))
                .isInstanceOf(SagaExecutionException.class);

        // Una compensación que falla no debe dejar colgado el paso anterior: el stock igualmente se libera.
        verify(reserveStock).compensate(context);
        assertThat(stateRepository.history("saga-partial"))
                .extracting(SagaStepRecord::status)
                .contains(StepStatus.COMPENSATION_FAILED, StepStatus.COMPENSATED);
    }
}
