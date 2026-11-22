# Spec Delta

## Purpose

Permite generar bajo demanda un link click-to-chat de WhatsApp (`wa.me`) para un turno existente, con el mensaje preformateado con fecha, hora, profesional y prestación sin datos clínicos, validando que el paciente tenga teléfono cargado y sin enviar mensajes automáticos (US-030, RN-WA-01 a RN-WA-04).

## ADDED Requirements

### Requirement: Generación de link de WhatsApp para un turno

El sistema SHALL exponer `GET /api/turnos/{id}/whatsapp-link`, que responde 200 con el identificador del turno (`turnoId`), el mensaje en texto plano (`mensaje`) y la URL click-to-chat (`url`) con formato `https://wa.me/{telefono}?text={mensaje URL-encoded}` (RN-WA-01). El sistema MUST NOT enviar mensajes a servicios externos al consultar este recurso (RN-WA-04).

#### Scenario: Link generado para un turno con paciente con teléfono válido

- **GIVEN** existe un turno para el paciente ficticio con teléfono "5491100000001", profesional "Ana Ejemplo", prestación "Control" e inicio "2026-11-24T10:30"
- **WHEN** se consulta `GET /api/turnos/{id}/whatsapp-link`
- **THEN** la respuesta es 200 con `turnoId` igual al id del turno, `url` que comienza con `https://wa.me/5491100000001?text=` y `mensaje` con la fecha, la hora, el profesional y la prestación

### Requirement: Contenido del mensaje sin datos clínicos

El mensaje generado SHALL incluir la fecha y la hora de inicio del turno, el nombre y apellido del profesional y el nombre de la prestación (RN-WA-02). El mensaje MUST NOT incluir DNI del paciente, diagnóstico, observaciones ni otros datos clínicos.

#### Scenario: Mensaje incluye fecha, hora, profesional y prestación sin DNI

- **GIVEN** existe un turno con paciente DNI "00000001" y teléfono "5491100000001", profesional "Ana Ejemplo", prestación "Limpieza" e inicio "2026-11-24T14:00"
- **WHEN** se consulta `GET /api/turnos/{id}/whatsapp-link`
- **THEN** la respuesta es 200 y el campo `mensaje` (y el parámetro `text` decodificado de `url`) contiene "2026-11-24" (o "24/11/2026"), "14:00", "Ana Ejemplo" y "Limpieza", y no contiene el DNI "00000001"

### Requirement: Codificación URL de caracteres especiales

El parámetro `text` de la URL SHALL estar codificado en UTF-8 (URL-encoded) de modo que espacios, tildes, eñes (`ñ`, `Ñ`) y signos de puntuación sean válidos en una URI y al decodificarse en UTF-8 reconstruyan exactamente el `mensaje` (RN-WA-01).

#### Scenario: Caracteres con tildes, espacios y eñe codificados en el link

- **GIVEN** existe un turno con profesional "Iñaki Muñoz", prestación "Extracción" y paciente con teléfono "5491100000002"
- **WHEN** se consulta `GET /api/turnos/{id}/whatsapp-link`
- **THEN** la respuesta es 200, el campo `url` es una URI válida sin espacios literales ni caracteres fuera de ASCII en la query, y al decodificar `text` en UTF-8 contiene exactamente "Iñaki Muñoz" y "Extracción"

### Requirement: Normalización del teléfono en el link

El teléfono incluido en la ruta `https://wa.me/{telefono}` SHALL estar en formato internacional compuesto únicamente por dígitos, sin signo `+`, espacios ni guiones (RN-WA-01). Si el teléfono almacenado en el paciente contiene `+`, espacios o guiones alrededor de los dígitos, el sistema SHALL normalizarlo quitando esos caracteres antes de armar la URL.

#### Scenario: Teléfono con signo más y espacios se normaliza a solo dígitos

- **GIVEN** existe un turno cuyo paciente tiene cargado el teléfono "+54 9 11 0000-0003"
- **WHEN** se consulta `GET /api/turnos/{id}/whatsapp-link`
- **THEN** la respuesta es 200 y el campo `url` comienza con `https://wa.me/5491100000003?text=`

### Requirement: Rechazo cuando el paciente no tiene teléfono

Si el paciente del turno no tiene teléfono cargado (`null`, vacío o sin dígitos), el sistema SHALL rechazar la generación del link respondiendo 422 con Problem Details y `codigo` igual a `PACIENTE_SIN_TELEFONO` (RN-WA-03).

#### Scenario: Paciente sin teléfono cargado

- **GIVEN** existe un turno cuyo paciente tiene `telefono` null
- **WHEN** se consulta `GET /api/turnos/{id}/whatsapp-link`
- **THEN** la respuesta es 422 con tipo `application/problem+json` y `codigo` igual a `PACIENTE_SIN_TELEFONO`

#### Scenario: Paciente con teléfono en blanco

- **GIVEN** existe un turno cuyo paciente tiene `telefono` formado solo por espacios
- **WHEN** se consulta `GET /api/turnos/{id}/whatsapp-link`
- **THEN** la respuesta es 422 con `codigo` igual a `PACIENTE_SIN_TELEFONO`

### Requirement: Turno inexistente al solicitar el link

Si el identificador de turno solicitado no existe, el sistema SHALL responder 404 con Problem Details y `codigo` igual a `RECURSO_INEXISTENTE`.

#### Scenario: Turno inexistente

- **WHEN** se consulta `GET /api/turnos/99999/whatsapp-link` y no existe un turno con ese id
- **THEN** la respuesta es 404 con `codigo` igual a `RECURSO_INEXISTENTE`

### Requirement: Sin datos personales del paciente en logs

La generación del link de WhatsApp MUST NOT registrar en los logs el DNI, el teléfono ni el nombre completo del paciente (RN-DP-03, regla D2).

#### Scenario: Generación de link sin fuga de datos personales en logs

- **GIVEN** existe un turno para el paciente ficticio "Paciente Uno", DNI "00000001" y teléfono "5491100000001"
- **WHEN** se consulta `GET /api/turnos/{id}/whatsapp-link` capturando la salida de logging
- **THEN** ningún evento de log contiene "00000001", "5491100000001" ni "Paciente Uno"
