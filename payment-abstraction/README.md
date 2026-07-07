# Payment Abstraction — Gateway pattern e inversión de dependencias

> Demo standalone de un patrón de arquitectura backend. Código Java/Spring Boot
> real y simplificado, pensado para leerse y también ejecutable de forma
> independiente (ver *Cómo ejecutar*).

## Contexto de uso real

Un sistema empieza cobrando con un único proveedor de pago (por ejemplo, Stripe),
pero se sabe que en el futuro podría añadirse otro distinto (una pasarela bancaria
local, otro PSP por costes o por mercado). Si el código que cobra está acoplado al
SDK concreto de Stripe, ese cambio futuro toca medio sistema.

## Qué resuelve

El patrón aplica **inversión de dependencias**: el código consumidor depende de una
**interfaz propia** (`PaymentGateway`), no del SDK de ningún proveedor. Cada
proveedor es una implementación de esa interfaz; añadir uno nuevo es añadir una
clase, sin tocar a quien consume el pago.

Decisiones de diseño que la demo transmite:

- **`PaymentGateway` como contrato propio** (`processPayment`, `refundPayment`,
  `getPaymentStatus`): la frontera que aísla al sistema del proveedor concreto.
- **Tipo de retorno agnóstico** (`PaymentResult`, sealed): el contrato no devuelve
  la respuesta cruda de Stripe, sino un resultado tipado del dominio. Es lo que
  hace que dos gateways sean realmente intercambiables. (Mismo modelado que la demo
  `saga-pattern/`, por coherencia.)
- **Selección por clave vía `Map<String, PaymentGateway>`**: Spring construye ese
  mapa automáticamente con una entrada por bean (clave = nombre del bean). El
  `PaymentService` elige el gateway por clave sin conocer las implementaciones
  concretas — patrón registry/strategy idéntico al del sistema real.
- **Dos implementaciones de ejemplo** (`StripePaymentGateway`, `MockPaymentGateway`):
  refuerzan visualmente que el mismo contrato admite proveedores completamente
  distintos; el mock además sirve como gateway determinista para tests. `MockPaymentGateway`
  es determinista de punta a punta: `processPayment` aprueba cualquier importe
  positivo y rechaza el resto, y `refundPayment`/`getPaymentStatus` resuelven según
  el `transactionId` (contiene `fail` → `PaymentFailure`, contiene `pending` →
  `PaymentPending`, en otro caso → `PaymentSuccess`), de forma que la demo puede
  escenificar los tres resultados. `StripePaymentGateway` mantiene esos dos métodos
  simulando solo éxito, ya que su propósito es mostrar la forma de una integración
  real, no la casuística.
- **Llamada externa simulada**: `StripePaymentGateway` no usa el SDK real, pero
  mantiene la forma idiomática (construir petición → llamar → mapear la respuesta a
  `PaymentResult`).

## Cómo ejecutar

Esta demo es un proyecto Maven autónomo (Java 21 / Spring Boot 3.x). Desde esta
carpeta:

```bash
mvn spring-boot:run   # ejecuta la demo y muestra el flujo completo por consola
mvn test              # corre los tests
```

Al arrancar, `PaymentDemoApplication` cobra a través del mismo `PaymentService`
con dos proveedores y escenifica cada resultado:

1. **El mismo servicio con dos proveedores** — un cobro con `stripe` y otro con
   `mock`, sin cambiar nada del consumidor.
2. **Camino de fallo del mock** — un importe no positivo devuelve `PaymentFailure`.
3. **Variaciones de refund/status del mock** — la misma llamada produce
   `PaymentSuccess`, `PaymentPending` o `PaymentFailure` según el `transactionId`.
4. **Proveedor desconocido** — pedir `paypal` lanza `UnknownGatewayException`.

## Cómo navegar el código

Lee los ficheros en este orden:

1. **`PaymentGateway.java`** — el contrato. Empieza aquí: es la abstracción de la
   que cuelga todo lo demás.
2. **`PaymentService.java`** — el consumidor; cómo Spring inyecta el
   `Map<String, PaymentGateway>` y cómo se selecciona el proveedor por clave.
3. **`StripePaymentGateway.java`** — una implementación con la llamada externa
   simulada pero idiomática.
4. **`MockPaymentGateway.java`** — una segunda implementación que evidencia la
   intercambiabilidad.
5. **`PaymentResult.java`** + **`PaymentRequest.java`** — los tipos agnósticos que
   viajan por el contrato.
6. **`PaymentDemoApplication.java`** — punto de entrada ejecutable: un
   `CommandLineRunner` escenifica los cuatro casos al arrancar.

> **Nota de simplificación**: ninguna implementación llama a un proveedor real ni
> usa credenciales; la llamada externa está simulada. Lo relevante del patrón es la
> frontera (`PaymentGateway` + `PaymentResult`) y la selección por clave, no la
> integración concreta con ningún PSP.
