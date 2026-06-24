# Payment Abstraction — Gateway pattern e inversión de dependencias

> Demo standalone de un patrón de arquitectura backend. Código Java/Spring Boot
> real y simplificado, pensado para leerse, no para ejecutarse.

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
  distintos; el mock además sirve como gateway determinista para tests.
- **Llamada externa simulada**: `StripePaymentGateway` no usa el SDK real, pero
  mantiene la forma idiomática (construir petición → llamar → mapear la respuesta a
  `PaymentResult`).

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

> **Nota de simplificación**: ninguna implementación llama a un proveedor real ni
> usa credenciales; la llamada externa está simulada. Lo relevante del patrón es la
> frontera (`PaymentGateway` + `PaymentResult`) y la selección por clave, no la
> integración concreta con ningún PSP.
