# Tasks

## 1. Puerto y generador de link `wa.me`

- [x] 1.1 Crear la interfaz `NotificadorDeTurnos` en `application/aviso/` y la implementación `WaMeLinkGenerator` (`@Component`) en `infrastructure/whatsapp/` que normaliza el teléfono a solo dígitos (lanzando `ReglaDeNegocioException(PACIENTE_SIN_TELEFONO)` si no quedan dígitos) y codifica el mensaje en UTF-8 (`https://wa.me/{telefono}?text={mensaje URL-encoded}`); verificar con `WaMeLinkGeneratorTest` (JUnit puro, sin Spring): link válido con teléfono internacional, codificación UTF-8 de espacios, tildes y `ñ` ("Iñaki Muñoz", "Extracción"), normalización de `+`, espacios y guiones ("+54 9 11 0000-0003"), y rechazo con `PACIENTE_SIN_TELEFONO` cuando el teléfono es `null`, vacío o sin dígitos.

## 2. Caso de uso `GenerarLinkWhatsApp`

- [x] 2.1 Crear `LinkWhatsAppVista` y `GenerarLinkWhatsApp` (`@Service`, `@Transactional(readOnly = true)`, inyección por constructor) en `application/aviso/` que busca el turno en `TurnoRepository` (404 `RECURSO_INEXISTENTE` si no existe), verifica el teléfono del paciente (422 `PACIENTE_SIN_TELEFONO` si no tiene), arma el mensaje con fecha (`yyyy-MM-dd`), hora (`HH:mm`), nombre completo del profesional y nombre de la prestación sin DNI ni datos clínicos (RN-WA-02) y delega en `NotificadorDeTurnos`; verificar con `GenerarLinkWhatsAppTest`: caso feliz, turno inexistente → `RECURSO_INEXISTENTE`, paciente sin teléfono o teléfono en blanco → `PACIENTE_SIN_TELEFONO`, y ausencia de DNI del paciente en el mensaje.

## 3. Endpoint `GET /api/turnos/{id}/whatsapp-link` y protección de logs

- [x] 3.1 Crear `LinkWhatsAppResponse` y `AvisoWhatsAppController` en `web/api/aviso/` con `GET /api/turnos/{id}/whatsapp-link` → 200 sin editar `TurnoController` de C-02; verificar con `AvisoWhatsAppApiTest` (`@SpringBootTest` + `@AutoConfigureMockMvc`, armando el turno directamente con los repositorios de C-01): escenario feliz 200 con `turnoId`, `url` (`https://wa.me/5491100000001?text=...`) y `mensaje` (fecha, hora, profesional, prestación, sin DNI), escenario de caracteres con tildes y `ñ`, y escenario de teléfono con `+` y espacios normalizado.
- [x] 3.2 Agregar a `AvisoWhatsAppApiTest` los escenarios de error y privacidad: paciente con teléfono `null` o en blanco → 422 `PACIENTE_SIN_TELEFONO` (`application/problem+json`); turno inexistente → 404 `RECURSO_INEXISTENTE`; y captura de logs con `ListAppender` verificando que ningún evento de log contiene el DNI, el teléfono ni el nombre completo del paciente (RN-DP-03, regla D2).

## 4. Verificación integral y reglas anti-colisión

- [x] 4.1 Ejecutar `./mvnw test` completo en verde sin tests `@Disabled` ni `-DskipTests` (regla J4) y `openspec validate link-whatsapp-turno --strict` sin errores.
- [x] 4.2 Verificar con `git status` y `git diff --name-only origin/main` que ningún archivo compartido de C-01 ni de otros changes fue modificado (regla E1).

## Workflow follow-up

- Archivar el change con `openspec-archive-change` (`openspec archive link-whatsapp-turno`) una vez revisado y verificado.
- Marcar `[x]` en `CHANGES.md` §C-06.
- Abrir PR desde `feat/link-whatsapp-turno` hacia `main` con conventional commits en español (reglas E2 y E4).
