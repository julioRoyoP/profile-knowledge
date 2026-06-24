# Outbox Pattern — Efectos secundarios asíncronos con consistencia eventual

> Demo standalone de un patrón de arquitectura backend. Código Java/Spring Boot
> real y simplificado, pensado para leerse, no para ejecutarse.

## Contexto de uso real

Tras **confirmar un pedido** hay que notificar al cliente por email. Pero el envío
de email depende de un proveedor externo que es **lento y a veces falla**. Si se
envía el email dentro de la misma transacción que confirma el pedido, aparecen dos
problemas: la transacción queda bloqueada esperando a un sistema externo, y un
fallo del proveedor puede arrastrar (rollback) la confirmación del pedido —o, al
revés, si la transacción hace rollback después de enviar, el cliente recibe un
email de un pedido que nunca se confirmó.

## Qué resuelve

El patrón Outbox **desacopla** el efecto secundario de la transacción principal:

1. Al confirmar el pedido, en la **misma transacción local** se inserta una fila en
   una tabla *outbox* describiendo la notificación pendiente. O confirman ambas
   cosas, o ninguna — la notificación nunca se pierde ni se emite por un pedido
   fantasma.
2. Un **relay** independiente (`@Scheduled`) lee periódicamente los eventos
   pendientes y los entrega de forma **asíncrona** (`@Async`), con reintentos y
   *backoff* exponencial. Un proveedor de email lento o caído ya no bloquea ni
   arriesga la operación de negocio.

Decisiones de diseño que la demo transmite:

- **Evento persistido en la transacción de negocio** (`OrderConfirmationService`,
  `@Transactional`): es la garantía atómica que sostiene todo el patrón.
- **Anti-duplicado vía estado `PENDING → PROCESSING → SENT`**: el relay marca el
  evento como `PROCESSING` y lo persiste **antes** de despachar. Así, un segundo
  tick del scheduler (o una segunda instancia del servicio) ya no lo ve como
  `PENDING` y no lo reenvía. En una base de datos real esto sería un
  `UPDATE ... WHERE status='PENDING' ... FOR UPDATE SKIP LOCKED`.
- **`@Async` en un bean aparte** (`OutboxDispatcher`, separado de `OutboxRelay`):
  `@Async` solo actúa a través del proxy de Spring, así que invocarlo desde el
  mismo bean lo ejecutaría en realidad de forma síncrona. Separarlo es la forma
  correcta de que el envío sea realmente asíncrono.
- **Backoff exponencial con presupuesto de reintentos**: cada fallo incrementa el
  contador y reprograma el siguiente intento (10s, 20s, 40s…); agotado el
  presupuesto, el evento pasa a `FAILED` para tratamiento manual / dead-letter.
- **`@Async` solo donde aporta valor**: la entrega de notificaciones es un efecto
  no crítico que tolera latencia; por eso es asíncrona. La consistencia inmediata
  (confirmar el pedido) permanece síncrona y transaccional.

## Cómo navegar el código

Lee los ficheros en este orden:

1. **`OrderConfirmationService.java`** — el productor. Empieza aquí: muestra por
   qué existe el outbox (evento + cambio de negocio en una sola transacción).
2. **`OutboxEvent.java`** + **`OutboxStatus.java`** — la entidad pendiente y su
   ciclo de vida; fíjate en `markRetry` (backoff) y `markProcessing` (claim).
3. **`OutboxRelay.java`** — el `@Scheduled` que reclama eventos y los pasa al
   dispatcher; aquí vive la lógica anti-duplicado.
4. **`OutboxDispatcher.java`** — el `@Async` que entrega con reintentos y backoff.
5. **`NotificationSender.java`** + **`LoggingNotificationSender.java`** — el
   contrato del canal externo y un stub que solo loggea (sin SMTP).
6. **`OutboxRepository.java`** — el almacén (aquí en memoria; en producción la
   tabla outbox con claim atómico).

> **Nota de simplificación**: no hay plantillas de email, configuración SMTP ni
> proveedor real — `LoggingNotificationSender` representa el canal externo. El
> almacén es en memoria; lo relevante del patrón es *que* el evento se persiste en
> la transacción de negocio y *que* la entrega se reclama de forma idempotente,
> no la tecnología concreta de almacenamiento o de envío.
