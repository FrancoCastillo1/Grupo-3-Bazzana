package ar.odonto.turnos.application.aviso;

/**
 * Puerto de generación de link de notificación de turnos (US-030, DD-09, RN-WA-01).
 * <p>
 * Declarado en {@code application/aviso} para no editar {@code domain/port} de C-01 (regla anti-colisión E1).
 */
public interface NotificadorDeTurnos {

    /**
     * Genera un link click-to-chat con el teléfono normalizado y el mensaje codificado.
     *
     * @param telefono teléfono del paciente (se normaliza a solo dígitos; si no tiene dígitos lanza PACIENTE_SIN_TELEFONO)
     * @param mensaje  texto plano del mensaje del turno
     * @return URL completa {@code https://wa.me/{telefono}?text={mensaje URL-encoded}}
     */
    String generarLink(String telefono, String mensaje);
}
