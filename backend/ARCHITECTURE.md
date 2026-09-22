# Decisiones de arquitectura

Este documento consolida las decisiones de diseño tomadas durante el desarrollo del proyecto, junto con el razonamiento detrás de cada una. El objetivo es que sirva tanto de referencia para futuras extensiones del proyecto como de evidencia del proceso de pensamiento detrás de la arquitectura, más allá del código final.

---

## 1. Arquitectura monolítica modular

El proyecto está organizado por dominio (`customer`, `vehicle`, `rental`, `payment`, `maintenance`, `auth`, `user`, `role`, `security`, `shared`) en lugar de por capa técnica (todos los controllers juntos, todos los services juntos, etc.).

**Razón:** cada módulo agrupa todo lo relacionado a un concepto del negocio (entidad, DTOs, controller, service, repository, excepciones propias). Esto facilita ubicar y modificar la lógica de un dominio sin tener que navegar entre carpetas técnicas dispersas, y prepara el código para una eventual extracción a microservicios si el proyecto creciera, sin requerir una reescritura completa.

**Regla de comunicación entre módulos:** un módulo nunca accede al repositorio de otro módulo directamente. Si `RentalService` necesita datos de `Payment`, lo hace a través de `PaymentService`, nunca de `PaymentRepository`. Esto mantiene cada módulo como dueño exclusivo de su persistencia y evita que la lógica de validación de un dominio quede dispersa en otros módulos.

La única excepción documentada a esta regla es `DataInitializer` (ver sección 9), que por ser inicialización del sistema —no lógica de negocio— accede directamente a `UserRepository` y `RoleRepository`.

---

## 2. `shared`: qué pertenece ahí y qué no

`shared` contiene `ApiResponse<T>`, las excepciones base (`ResourceNotFoundException`, `ResourceAlreadyExistsException`, `BusinessRuleException`) y los eventos de dominio (`events/`).

**Criterio para decidir si algo va en `shared`:** un componente pertenece a `shared` si es una **utilidad sin lógica de negocio propia**, que otros módulos *componen* para construir su propia lógica. `ApiResponse<T>` no sabe nada de vehículos o rentas; `BusinessRuleException` es un contenedor que cada módulo llena con su propio mensaje.

Bajo este criterio, módulos como `security` y `auth` **no** pertenecen a `shared`, aunque sean usados transversalmente por todo el proyecto: ambos tienen lógica propia y completa (autenticación/autorización, reglas de registro), no son utilidades pasivas. La tentación de moverlos para "verse más organizado" se descartó conscientemente por esta razón.

---

## 3. `ApiResponse<T>` y excepciones personalizadas

Todas las respuestas de la API se envuelven en `ApiResponse<T>` (`success`, `message`, `data`), y los errores de negocio usan tres excepciones personalizadas mapeadas a códigos HTTP específicos:

| Excepción | HTTP | Significado |
|---|---|---|
| `ResourceNotFoundException` | 404 | El recurso solicitado no existe |
| `ResourceAlreadyExistsException` | 409 | Conflicto con un recurso existente (ej. email duplicado) |
| `BusinessRuleException` | 422 | La petición es válida en forma, pero viola una regla del dominio |

**Razón de la distinción 404/409/422:** permite que los clientes de la API distingan programáticamente entre "no encontré lo que pediste", "ya existe algo en conflicto" y "tu petición es correcta pero el negocio no la permite en este momento" — tres situaciones que requieren manejo distinto en un frontend.

---

## 4. Estrategias de "borrado" distintas para `Customer` y `Vehicle`

`Customer` usa un campo `active: boolean` (borrado lógico clásico) con una regla adicional: si se intenta registrar un cliente con una licencia (`license_number`) que ya existe pero está `active = false`, el registro existente se **reactiva** en lugar de crear un duplicado o rechazar la operación.

`Vehicle`, en cambio, **no** tiene un campo `active`. Su "baja" se representa con `VehicleStatus.INACTIVE`, un valor más dentro de su máquina de estados (`AVAILABLE`, `RENTED`, `MAINTENANCE`, `INACTIVE`).

**Razón de la diferencia:** para `Customer`, "activo/inactivo" es una propiedad binaria independiente de cualquier otro estado. Para `Vehicle`, "inactivo" es **un estado más entre varios mutuamente excluyentes** — un vehículo no puede estar simultáneamente `RENTED` e `INACTIVE`. Modelarlo como un enum evita la necesidad de combinar un booleano con un enum de estado, lo que generaría combinaciones inválidas (ej. `active=false` + `status=RENTED`).

La reactivación por licencia duplicada en `Customer` es la razón por la cual `createMaintenance` valida explícitamente que el vehículo esté `AVAILABLE` (y por lo tanto bloquea `INACTIVE`): un vehículo dado de baja no debería recibir mantenimiento nuevo; si se necesita reactivarlo, ese es un flujo distinto (cambio manual de estado), no parte de este feature.

---

## 5. Snapshot de tarifa y cálculo de `expectedAmount` en `Rental`

`Rental` almacena `dailyRateSnapshot` (copia de `vehicle.dailyRate` al momento de crear la renta) y `expectedAmount` (`dailyRateSnapshot × días de renta`), calculados una sola vez al crear la renta.

**Razón:** la tarifa diaria de un vehículo puede cambiar con el tiempo. Si `Rental` leyera `vehicle.getDailyRate()` dinámicamente, una renta creada hace meses cambiaría de "precio esperado" cada vez que se actualiza la tarifa del vehículo — rompiendo la integridad histórica del contrato. El snapshot congela el valor económico acordado en el momento de la renta.

El cálculo de días usa `ChronoUnit.DAYS.between(startDate, expectedReturnDate)`, bajo la regla de negocio de que toda renta se mide en días completos (mínimo 1 día) — nunca fracciones. Esto se decidió explícitamente: el sistema sí registra horas (para poder calcular retrasos en el futuro), pero el *mínimo facturable* siempre es un día completo, por lo que `expectedReturnDate` siempre representa un número entero de días después de `startDate`.

---

## 6. Validaciones de negocio en `createRental`

Antes de crear una renta, el sistema valida:

1. `vehicle.status == AVAILABLE` — un vehículo rentado, en mantenimiento o inactivo no puede rentarse de nuevo
2. `customer.active == true` — un cliente dado de baja no puede generar nuevas rentas
3. `días >= 1` — la fecha de devolución esperada debe ser al menos un día posterior a la de inicio

Las tres se implementan como `BusinessRuleException` (422) dentro de `RentalService`, no como validaciones de Jakarta a nivel de DTO.

**Razón:** Jakarta Validation valida la *forma* de un campo de manera aislada (`@NotNull`, `@Future`, etc.). Estas tres reglas son relaciones entre **entidades distintas** (vehículo, cliente, fechas entre sí) evaluadas en el contexto del dominio — son reglas de negocio, no de formato, y por tanto viven en el service junto al resto de las validaciones de negocio del mismo método.

---

## 7. Recalculo de `totalAmount`: diseño y resolución de un ciclo de dependencias

### Decisión: recalcular, no acumular

Cuando se confirma un pago (`PaymentService.changePaymentToConfirmed`), `Rental.totalAmount` se **recalcula desde cero** sumando todos los pagos `CONFIRMED` de esa renta, en lugar de sumar incrementalmente el monto del pago recién confirmado al valor existente.

**Razón:** un enfoque de "sumar/restar" requiere que *cada* operación que afecte pagos (confirmaciones, cancelaciones, futuros reembolsos parciales) mantenga manualmente el invariante del total. Cualquier operación que se olvide de actualizarlo deja el dato desincronizado de forma silenciosa. Recalcular desde la fuente de verdad (`payments` con estado `CONFIRMED`) es más robusto: cualquier cambio futuro en cómo se manejan los pagos sigue produciendo un total correcto sin tocar esta lógica.

### El ciclo de dependencias y su resolución

La primera implementación tenía `RentalService` dependiendo de `PaymentService` (para obtener los pagos confirmados) **y** `PaymentService` dependiendo de `RentalService` (para notificar el nuevo total) — un ciclo bidireccional que Spring rechaza al arrancar (dependencia circular entre beans).

**Resolución:** se invirtió la responsabilidad. `PaymentService`, que ya es dueño de `PaymentRepository`, calcula la suma de pagos confirmados directamente y le pasa **solo el resultado** (`BigDecimal`) a `RentalService.updateTotalAmount(rentalId, total)` — un método que únicamente asigna el valor. `RentalService` deja de depender de `PaymentService` por completo.

**Principio general extraído:** ante un ciclo de dependencias entre dos servicios donde cada uno necesita "un solo dato" del otro, conviene preguntarse cuál de los dos puede *calcular* ese dato con sus propios recursos (su propio repositorio) y entregarle al otro solo el resultado, en lugar de pedirle el dato crudo.

---

## 8. Eventos de dominio: cancelación en cascada de pagos pendientes

Cuando se cancela una renta (`RentalService.cancelRental`), cualquier pago `PENDING` asociado se cancela automáticamente.

### Por qué un evento y no una llamada directa

Implementar esto como una llamada directa (`RentalService` → `PaymentService.cancelPendingPayments(...)`) reintroduce el mismo tipo de ciclo descrito en la sección 7: `PaymentService` ya depende de `RentalService` (para `updateTotalAmount`), y agregar la dirección inversa volvería a crear un ciclo bidireccional.

**Resolución:** `RentalService.cancelRental` publica un `RentalCancelledEvent` (definido en `shared.events`, un paquete neutral que ambos módulos pueden conocer sin depender directamente entre sí) usando `ApplicationEventPublisher`. `PaymentServiceImpl` expone un método `@EventListener` que escucha ese evento y cancela los pagos `PENDING` correspondientes.

`RentalService` no sabe ni le importa quién escucha el evento — termina su responsabilidad publicándolo. Esto elimina la dependencia `RentalService → PaymentService` sin necesidad de un endpoint manual ni de un ciclo.

### Trade-off reconocido

Los eventos resuelven el problema de dependencia, pero a costa de **trazabilidad**: leyendo únicamente `cancelRental`, no es evidente que algo ocurre en el módulo `Payment` como consecuencia. Esta decisión se tomó conscientemente porque (a) era la única forma razonable de resolver el ciclo, y (b) la acción secundaria (cancelar pagos pendientes) es genuinamente periférica al flujo principal — si fallara, no debería impedir la cancelación de la renta en sí.

**Detalles de implementación relevantes:**
- El listener tiene su propio `@Transactional` — `@EventListener` es síncrono por defecto, pero no hereda automáticamente la transacción del método que publicó el evento; sin transacción propia, los cambios de estado del pago no se persistirían.
- El evento se publica **después** de guardar la renta como `CANCELLED`, para que cualquier validación que el listener (u otros procesos) hagan sobre el estado de la renta vea el estado ya actualizado.

---

## 9. Inicialización de datos (`DataInitializer`)

Al arrancar, `DataInitializer` (un `CommandLineRunner` en el módulo `auth`) verifica que existan los roles `ADMIN` y `EMPLOYEE` (creándolos si faltan, de forma independiente entre sí) y que exista al menos un usuario `ADMIN`, creándolo con credenciales provistas por variables de entorno si no existe.

**Por qué accede a `RoleRepository`/`UserRepository` directamente:** esta es la única excepción documentada a la regla de "no repositorios ajenos" (sección 1). `DataInitializer` no es lógica de negocio del dominio — es infraestructura de arranque. Crear un `UserService.createDefaultAdmin()` solo para este caso habría añadido un método de un solo uso a la API pública del servicio, sin beneficio real.

**Por qué `ddl-auto=update` y no `create`/`create-drop`:** con `create-drop`, cada reinicio del proyecto borraría las tablas — incluyendo el usuario administrador creado por `DataInitializer` — obligando a recrearlo cada vez. `update` permite que el esquema se cree la primera vez y persista en reinicios posteriores, mientras `DataInitializer` sigue verificando idempotentemente que los datos base existan. Para un proyecto de portafolio que debe poder levantarse con un solo comando y funcionar de inmediato, esto se priorizó sobre el comportamiento más estricto de `validate` (que requiere que el esquema ya exista) recomendado para producción con migraciones controladas.

---

## 10. Roles, permisos y registro de administradores

El sistema tiene dos roles: `ADMIN` y `EMPLOYEE`.

**Decisión: `EMPLOYEE` tiene acceso operativo completo**, sin restricciones adicionales por rol en las operaciones del día a día (clientes, vehículos, rentas, pagos, mantenimientos). La alternativa —restringir operaciones "sensibles" (cancelaciones, etc.) a `ADMIN`— se descartó porque añadiría fricción operativa real (un empleado necesitaría un supervisor para tareas cotidianas) a cambio de un beneficio que ya está cubierto: **toda entidad registra `created_by`**, dando trazabilidad completa de quién hizo cada operación sin necesidad de restringir el "qué".

**La única operación exclusiva de `ADMIN`** es `POST /auth/admin/register` (crear nuevas cuentas de administrador), protegida con `@PreAuthorize("hasRole('ADMIN')")` a nivel de método. `POST /auth/register` permanece público y siempre asigna el rol `EMPLOYEE` — un empleado puede crear su propia cuenta sin intervención de un admin.

### Bug encontrado y resuelto: `shouldNotFilter` bloqueaba la autenticación

Al implementar `/auth/admin/register`, el endpoint devolvía 403 incluso con un token válido de administrador. La causa: `JwtAuthFilter.shouldNotFilter` excluía **todo** lo que empezara con `/auth/` del procesamiento JWT — una regla escrita cuando solo existían `/auth/register` y `/auth/login` (ambos públicos, sin necesidad de token). Al agregar `/auth/admin/register` (que sí requiere autenticación), esa misma regla impedía que el filtro JWT autenticara al usuario, dejando la request como anónima antes de que `@PreAuthorize` pudiera evaluarla.

**Resolución:** `shouldNotFilter` se cambió de un prefijo (`startsWith("/auth/")`) a una lista explícita de rutas exactas (`/auth/register`, `/auth/login`). Cualquier otra ruta bajo `/auth/` ahora pasa por el filtro JWT normalmente.

**Lección general:** una regla de exclusión escrita por prefijo puede ser correcta en el momento, pero se vuelve incorrecta silenciosamente cuando el espacio de rutas bajo ese prefijo crece. Las exclusiones de seguridad deben revisarse cada vez que se agregan rutas nuevas bajo un prefijo ya excluido.

---

## 11. Validaciones de consistencia entre módulos (revisión sistemática)

Durante una revisión dedicada del proyecto, se aplicó sistemáticamente un mismo conjunto de preguntas a cada transición de estado relevante:

1. ¿Qué estados de la entidad permiten ejecutar esta operación? (whitelist explícita)
2. ¿Esta transición debería afectar el estado de una entidad de otro módulo?
3. ¿Pueden existir datos "huérfanos" o inconsistentes como resultado de esta operación?

Esta revisión encontró y corrigió los siguientes huecos:

- **`returnVehicle`** no validaba el estado de la renta (podía ejecutarse sobre una renta `CANCELLED`/`COMPLETED`) ni si el pago estaba completo. Se agregó: whitelist `ACTIVE`/`OVERDUE`, y bloqueo si `totalAmount < expectedAmount` (manejando `totalAmount == null` como `BigDecimal.ZERO` cuando no hay pagos confirmados aún).
- **`cancelMaintenance`** cancelaba el mantenimiento pero nunca devolvía el vehículo de `MAINTENANCE` a `AVAILABLE`, dejándolo "atrapado" — no podía rentarse ni recibir un nuevo mantenimiento. Se corrigió devolviendo el vehículo a `AVAILABLE` tras la cancelación.
- **`createRental`** no validaba `customer.active`, permitiendo generar rentas para clientes dados de baja. Se agregó la validación (sección 6).
- **Transiciones de `Vehicle.status`** se verificaron como consistentes *por construcción*: todas las operaciones que cambian el estado de un vehículo (`createRental`, `returnVehicle`, `cancelRental`, `createMaintenance`, `completeMaintenance`, `cancelMaintenance`) parten de una precondición de estado específica, lo que hace imposible que un vehículo quede en un estado intermedio inconsistente sin que ninguna operación pueda recuperarlo.

---

## 12. Seguridad de configuración y Docker

**Secretos vs. configuración:** `JWT_SECRET`, las credenciales de base de datos y las credenciales del administrador inicial se externalizaron a variables de entorno — un `JWT_SECRET` hardcodeado y subido a un repositorio público permitiría a cualquiera firmar tokens válidos, incluyendo tokens que se autoasignen el rol `ADMIN`. En cambio, `jwt.expiration` permaneció como configuración fija en `application.properties`: no es un secreto, es un parámetro de comportamiento sin implicaciones de seguridad si es público.

**Dos archivos de entorno:** `.env` (en `.gitignore`, credenciales reales de desarrollo local) y `.env.docker` (versionado, valores de demostración para que cualquiera pueda levantar una instancia local funcional vía Docker Compose). `.env.docker` es seguro de publicar porque describe una base de datos *nueva y local* que se crea en la máquina de quien ejecuta `docker-compose up` — no expone ningún sistema real.

**Dockerfile multi-stage:** la imagen final se construye en dos etapas — una con JDK + Maven para compilar (`eclipse-temurin:21-jdk`), y otra solo con JRE (`eclipse-temurin:21-jre`) que copia únicamente el `.jar` resultante. Esto evita que la imagen de ejecución cargue con herramientas de compilación que nunca se usan en runtime. Los tests se omiten durante el build de la imagen (`-DskipTests`) porque requieren una base de datos que no está disponible en esa etapa — el flujo real de CI/CD correría los tests en un paso separado, con la base de datos ya disponible.

**Hostnames dentro de Docker:** dentro de la red de `docker-compose`, los contenedores se referencian por el nombre del servicio (`db`), no por `localhost` — cada contenedor tiene su propio `localhost` aislado. El mapeo de puertos (`8080:8080`) es lo que permite acceder a la API desde `localhost:8080` *fuera* de Docker, desde la máquina host.

---

## 13. Mejoras futuras (fuera de alcance, identificadas conscientemente)

### Historial completo de mantenimientos por vehículo

`GET /api/maintenances/vehicle` consulta únicamente si un vehículo tiene **actualmente** un mantenimiento `IN_PROGRESS` — responde con 0 o 1 resultado, pensado para validaciones operativas (¿puedo rentar/intervenir este vehículo ahora?).

Durante la documentación de este endpoint surgió una necesidad distinta: consultar el **historial completo** de mantenimientos de un vehículo (`COMPLETED`, `CANCELLED`, `IN_PROGRESS` pasados y presentes), útil para reportes y decisiones de mantenimiento preventivo.

**Se decidió no implementarlo en este ciclo** porque es una funcionalidad nueva con un propósito distinto al endpoint existente (verificación de estado actual vs. consulta de historial), no una corrección de un hueco. Cuando se implemente, debería ser un endpoint independiente, paginado (`Page<MaintenanceResponse>`), por ejemplo `GET /api/maintenances/vehicle/{vehicleId}/history`, sin modificar el endpoint actual.

### Otras líneas abiertas

- **Roles más granulares**: actualmente solo existe la distinción `ADMIN`/`EMPLOYEE` con `ADMIN` exclusivo para registrar otros administradores. Si el negocio creciera, podrían surgir roles intermedios (ej. "supervisor") — se decidió no anticipar esta necesidad sin un caso de uso real (ver sección 10).
- **Cálculo de cargos por retraso (`OVERDUE`)**: el sistema registra fecha y hora de inicio/devolución con precisión suficiente para calcular retrasos, y el estado `OVERDUE` ya existe en `RentalStatus`, pero la lógica que determina automáticamente cuándo una renta pasa a `OVERDUE` y cómo se calculan cargos adicionales no se implementó — quedó fuera del alcance de `rental-payment-totals`.
- **Pruebas automatizadas (JUnit + Mockito)**: el proyecto actualmente no tiene tests unitarios ni de integración. Esta es una omisión reconocida, no un descuido oculto — durante el desarrollo se priorizó la corrección de la lógica de negocio y la consistencia entre módulos mediante revisión manual sistemática (ver sección 11). Pruebas unitarias sobre los `Service` (mockeando repositorios) cubrirían naturalmente las validaciones de negocio documentadas en este archivo (estados válidos, cálculo de `expectedAmount`, recalculo de `totalAmount`, cascada del evento `RentalCancelledEvent`), y son el siguiente paso de aprendizaje natural tras cerrar este proyecto.