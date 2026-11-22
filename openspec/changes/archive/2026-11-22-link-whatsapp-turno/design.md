# Design

## Context

C-01 dejó el modelo (`Turno`, `Paciente`, `Profesional`, `Prestacion`), el puerto `TurnoRepository`, el catálogo `CodigoError` (que ya incluye `RECURSO_INEXISTENTE` 404 y `PACIENTE_SIN_TELEFONO` 422), `ReglaDeNegocioException` y `ManejadorGlobalDeErrores`. Aunque la ficha de C-06 en `CHANGES.md` menciona el puerto `NotificadorDeTurnos` en `domain/port`, el scaffold de C-01 no creó ese archivo en `domain/port/`, y la regla dura E1 (y anti-colisión 1 y 4 de `CHANGES.md`) prohíbe editar o agregar archivos en `domain/port/` desde la ola 1. Además, `spring.jpa.open-in-view=false` obliga a resolver las relaciones `@ManyToOne(fetch = LAZY)` de `Turno` (`paciente`, `profesional`, `prestacion`) dentro de una transacción `@Transactional(readOnly = true)`. Ver `proposal.md` para la motivación y `specs/aviso-whatsapp/spec.md` para los requisitos.

## Goals / Non-Goals

**Goals:**
- Exponer `GET /api/turnos/{id}/whatsapp-link` cumpliendo RN-WA-01 a RN-WA-04 y RN-DP-03, con un test por escenario de la spec.
- Mantener la inversión de dependencias entre la capa de aplicación (`application/aviso/`) y el adaptador de infraestructura (`infrastructure/whatsapp/WaMeLinkGenerator`) mediante el puerto `NotificadorDeTurnos`, sin tocar archivos compartidos de C-01 ni de C-02.
- Tests independientes de C-02: arman el `Turno` directamente con los repositorios de C-01.

**Non-Goals:**
- Envío automático de mensajes o integración con WhatsApp Business API (RN-WA-04, DD-09; queda para US-101).
- Autorización por rol en el controlador: la matriz RBAC (SECRETARIA y ADMIN) la aplica C-05 por ruta en su `SecurityFilterChain` (regla anti-colisión 6).
- Botón en la interfaz Thymeleaf (pertenece a C-10).

## Decisions

### D1. Puerto `NotificadorDeTurnos` en `application/aviso/` e implementación `WaMeLinkGenerator` en `infrastructure/whatsapp/`
Como `domain/port/` pertenece exclusivamente a C-01 (regla E1) y no contiene `NotificadorDeTurnos`, el puerto `NotificadorDeTurnos` se declara en `ar.odonto.turnos.application.aviso` (siguiendo el mismo patrón que C-02 usó con `BloqueoDeRecursos` en `application/turnos/` y C-03 con `RepositorioDeHorarioDeConfiguracion` en `application/configuracion/persistencia/`).
- `NotificadorDeTurnos` define `String generarLink(String telefono, String mensaje)`.
- `WaMeLinkGenerator` (`@Component` en `ar.odonto.turnos.infrastructure.whatsapp`) implementa `NotificadorDeTurnos`.
- *Alternativa descartada*: agregar `NotificadorDeTurnos` en `domain/port/` → violaría la regla dura E1 y el chequeo de colisiones de PR (`git diff --name-only origin/main...HEAD`).

### D2. Normalización del teléfono y codificación URL en `WaMeLinkGenerator`
`WaMeLinkGenerator` recibe el teléfono y el mensaje:
1. Normaliza el teléfono conservando únicamente los dígitos (`telefono.replaceAll("[^0-9]", "")` o filtrando `Character::isDigit`). Si el resultado queda vacío, lanza `ReglaDeNegocioException(CodigoError.PACIENTE_SIN_TELEFONO)`. Así cubre tanto un teléfono ya limpio (`5491100000001`) como el caso de borde con `+`, espacios o guiones (`+54 9 11 0000-0003` → `5491100000003`).
2. Codifica el mensaje en UTF-8 con `URLEncoder.encode(mensaje, StandardCharsets.UTF_8).replace("+", "%20")` (o `UriUtils.encodeQueryParam(mensaje, StandardCharsets.UTF_8)`), de modo que los espacios queden como `%20` (estándar RFC 3986 para URIs y compatible con cualquier parser de URL y `wa.me`) y los caracteres como tildes y `ñ` se codifiquen en UTF-8 (`%C3%B1`, etc.).
3. Devuelve `"https://wa.me/" + telefonoNormalizado + "?text=" + mensajeCodificado`.
- *Alternativa descartada*: concatenar sin normalizar confiando en que C-04 ya validó el teléfono → descartada porque el escenario de borde de C-06 en `CHANGES.md` pide explícitamente normalizar teléfonos con `+` o espacios si hubieran sido cargados por seed o migración.

### D3. Caso de uso `GenerarLinkWhatsApp` en `application/aviso/`
`@Service`, `@Transactional(readOnly = true)`, inyección por constructor (regla J3):
1. Busca el turno con `turnoRepository.findById(turnoId)` → si no existe, lanza `ReglaDeNegocioException(CodigoError.RECURSO_INEXISTENTE, "No existe el turno indicado")`.
2. Dentro de la transacción (`open-in-view=false`), accede a `turno.getPaciente()`, `turno.getProfesional()` y `turno.getPrestacion()`.
3. Verifica `paciente.tieneTelefono()` y que tras normalizar contenga dígitos; si no, lanza `ReglaDeNegocioException(CodigoError.PACIENTE_SIN_TELEFONO)`.
4. Arma el mensaje sin datos clínicos ni DNI (RN-WA-02):
   `"Hola, le recordamos su turno de %s con %s %s el %s a las %s."`
   donde la fecha se formatea en ISO (`yyyy-MM-dd`) junto con `dd/MM/yyyy` o `yyyy-MM-dd` y la hora en `HH:mm` (`DateTimeFormatter.ofPattern("yyyy-MM-dd")` y `DateTimeFormatter.ofPattern("HH:mm")`), incluyendo nombre y apellido del profesional y nombre de la prestación.
5. Llama a `notificador.generarLink(paciente.getTelefono(), mensaje)` y devuelve el record `LinkWhatsAppVista(Long turnoId, String url, String mensaje)`.
6. Si registra logs, solo loguea `turno.getId()`, nunca DNI, teléfono ni nombre del paciente (RN-DP-03, regla D2).

### D4. Controlador propio `AvisoWhatsAppController` en `web/api/aviso/`
`@RestController`, `@RequestMapping("/api/turnos")`, método `@GetMapping("/{id}/whatsapp-link")` que delega en `GenerarLinkWhatsApp.ejecutar(id)` y devuelve `LinkWhatsAppResponse(Long turnoId, String url, String mensaje)` con HTTP 200.
- *Alternativa descartada*: agregar el endpoint a `TurnoController` de C-02 → prohibido por la regla anti-colisión de `CHANGES.md` ("controlador propio; no edita el de C-02").

## Risks / Trade-offs

- **[`open-in-view=false` con relaciones `LAZY` en `Turno`]** → `GenerarLinkWhatsApp.ejecutar` lleva `@Transactional(readOnly = true)` y construye el record `LinkWhatsAppVista` antes de salir del método transaccional.
- **[Diferencia entre `+` y `%20` en query strings de `URLEncoder`]** → `URLEncoder` produce `+` para espacios (formato `application/x-www-form-urlencoded`), mientras que RFC 3986 usa `%20`; reemplazar `+` por `%20` tras `URLEncoder.encode` (habiendo codificado un `+` literal como `%2B` primero) produce un URI estándar que decodifica idéntico con `URLDecoder.decode(..., UTF_8)` y `UriComponentsBuilder`.
- **[Fuga accidental de teléfono en logs si se loguea la URL completa]** → la URL de `wa.me` contiene el teléfono del paciente en el path; por lo tanto, **nunca** se debe loguear la URL generada en los logs del servidor (RN-DP-03, regla D2). Solo se loguea el `turnoId`.

## Migration Plan

No requiere migración de base de datos ni nuevas variables de entorno. El despliegue es aditivo; el rollback consiste en revertir la rama `feat/link-whatsapp-turno`.

## Open Questions

Ninguna.
