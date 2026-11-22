# Proposal

## Why

La secretaria hoy puede crear y consultar turnos, pero para avisarle al paciente del horario reservado tiene que tipear el mensaje y buscar el número a mano en WhatsApp, lo que consume tiempo y es propenso a errores de transcripción. Este change (C-06, US-030) agrega la generación de un link *click-to-chat* (`https://wa.me/{telefono}?text={mensaje}`) con los datos del turno precargados y sin costo ni credenciales externas (DD-09), para que la secretaria avise al paciente en un clic.

## What Changes

- Nuevo puerto de notificación `GeneradorLinkWhatsApp` (o interfaz en `application/aviso/`, respetando la regla anti-colisión E1 de no editar `domain/port/`) y su implementación MVP `WaMeLinkGenerator` en `infrastructure/whatsapp/`, que normaliza el teléfono a formato internacional sin `+` ni espacios y construye `https://wa.me/{telefono}?text={mensaje URL-encoded}` en UTF-8 (RN-WA-01).
- Nuevo caso de uso `GenerarLinkWhatsApp` en `application/aviso/`: carga el turno por ID (404 `RECURSO_INEXISTENTE` si no existe), verifica que el paciente tenga teléfono cargado (422 `PACIENTE_SIN_TELEFONO` si no tiene o está en blanco, RN-WA-03), arma el mensaje con fecha, hora, profesional y prestación sin datos clínicos (RN-WA-02), y delega la construcción del link sin enviar ningún mensaje automático (RN-WA-04).
- Nuevo endpoint `GET /api/turnos/{id}/whatsapp-link` en `web/api/aviso/` (controlador propio `AvisoWhatsAppController`, sin editar `TurnoController` de C-02) que responde `200 OK` con el DTO del link generado (`turnoId`, `url`, `mensaje`).
- Protección de datos personales en logs (RN-DP-03, regla D2): la capa de aviso nunca registra DNI, teléfono ni nombre completo del paciente en los logs (solo IDs).

## Capabilities

### New Capabilities

- `aviso-whatsapp`: generación bajo demanda del link click-to-chat de WhatsApp (`wa.me`) para un turno, con mensaje preformateado (fecha, hora, profesional y prestación, sin datos clínicos), normalización del teléfono internacional, rechazo cuando el paciente no tiene teléfono (`PACIENTE_SIN_TELEFONO`) y sin envío automático de mensajes (RN-WA-01 a RN-WA-04).

### Modified Capabilities

_(ninguna)_

## Impact

- **Código nuevo** (exclusivamente en los paquetes asignados a C-06 en `CHANGES.md`): `application/aviso/**`, `infrastructure/whatsapp/**`, `web/api/aviso/**` y sus tests espejo en `src/test/java/ar/odonto/turnos/`.
- **Código existente reutilizado sin modificar**: `Turno`, `Paciente`, `Profesional`, `Prestacion` (`domain/model`), `TurnoRepository` (`domain/port`), `CodigoError` (`RECURSO_INEXISTENTE` 404 y `PACIENTE_SIN_TELEFONO` 422 ya definidos en C-01), `ReglaDeNegocioException` y `ManejadorGlobalDeErrores`.
- **Archivos compartidos**: cero ediciones en archivos de C-01 o C-02 (regla E1). Como C-01 no incluyó `NotificadorDeTurnos` en `domain/port/`, el puerto se declara dentro de `application/aviso/` siguiendo la regla anti-colisión 4 y 1.
- **API**: nueva ruta `GET /api/turnos/{id}/whatsapp-link` (200 / 404 / 422 con Problem Details y campo `codigo`). El control de acceso por rol (SECRETARIA, ADMIN) lo aplica C-05 por ruta (regla anti-colisión 6).
- **Dependencias**: solo depende de `C-01` (ya en `main`); lo consume `C-10` (botón de link de WhatsApp en la UI) y `C-11`.
