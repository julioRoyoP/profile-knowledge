# Saga Pattern — Transacciones distribuidas con compensación manual

> Demo standalone de un patrón de arquitectura backend. Código Java/Spring Boot
> real y simplificado, pensado para leerse, no para ejecutarse.

## Contexto de uso real

En un flujo de **checkout de e-commerce**, una sola compra dispara varios pasos
que tocan sistemas independientes: reservar stock en inventario, cobrar a través
de una pasarela de pago externa y confirmar el envío con un transportista. Cada
uno de esos sistemas tiene su propia base de datos y su propia transacción; no
comparten un gestor transaccional común, así que **no se pueden agrupar en una
única transacción ACID** (ni en un 2PC/XA razonable).

El problema aparece cuando un paso tardío falla: el stock ya está reservado y la
tarjeta ya está cobrada cuando el transportista rechaza el envío. Hay que
**deshacer lo ya hecho**, en orden inverso, sin dejar al cliente cobrado por algo
que nunca se enviará.

## Qué resuelve

El patrón Saga cambia atomicidad por **consistencia eventual**: cada paso confirma
su efecto localmente y aporta una **acción compensatoria** que lo revierte. Un
orquestador ejecuta los pasos en orden y, ante el primer fallo, dispara la
compensación de los pasos ya completados **en orden LIFO** (se deshace primero lo
más reciente: se reembolsa el cobro antes de liberar el stock que ese cobro pagó).

Decisiones de diseño que la demo transmite:

- **Saga y no 2PC**: los sistemas implicados no comparten transaction manager; un
  2PC los bloquearía a los tres durante todo el checkout. La saga deja que cada
  uno confirme por separado y desenreda los fallos con compensaciones.
- **Interfaz `SagaStep`**: el orquestador depende solo del contrato
  `execute`/`compensate`, nunca de pasos concretos. Añadir o reordenar pasos no
  toca la lógica de orquestación.
- **Estado persistido por paso**: cada transición (`STARTED`, `COMPLETED`,
  `FAILED`, `COMPENSATED`, `COMPENSATION_FAILED`) se registra. Es lo que permite
  auditar o reanudar una saga tras un reinicio.
- **Fallo de compensación que no aborta el rollback**: si una compensación falla,
  se registra como `COMPENSATION_FAILED` y se continúa con el resto, en lugar de
  dejar los demás pasos a medio deshacer.
- **`PaymentResult` como sealed interface**: el resultado del pago se consume con
  pattern matching exhaustivo en un `switch` sin `default`, de modo que añadir un
  nuevo estado de pago obliga a tratarlo en tiempo de compilación.

## Cómo navegar el código

Lee los ficheros en este orden:

1. **`SagaOrchestrator.java`** — la pieza central. Empieza aquí: el bucle de
   ejecución y el método `compensate` resumen todo el patrón.
2. **`SagaStep.java`** — el contrato que desacopla el orquestador de los pasos
   concretos.
3. **`ChargePaymentStep.java`** + **`PaymentResult.java`** — ejemplo de paso real
   y del uso de la sealed interface con `switch`.
4. **`ReserveStockStep.java`** / **`ConfirmShipmentStep.java`** — los otros dos
   pasos; `ConfirmShipmentStep` simula el fallo tardío que dispara la compensación.
5. **`CheckoutService.java`** — cómo se ensambla la secuencia ordenada de pasos y
   se lanza la saga.
6. **`SagaStateRepository.java`** / **`SagaStepRecord.java`** / **`StepStatus.java`**
   — el rastro de estado persistido (aquí en memoria, en producción una tabla).

> **Nota de simplificación**: la reserva de stock usaría en la implementación real
> un *lock pesimista* a nivel de base de datos (`SELECT ... FOR UPDATE`) para
> evitar sobreventa bajo concurrencia. Ese detalle pertenece al módulo de
> inventario, no al patrón Saga, y se omite a propósito.
