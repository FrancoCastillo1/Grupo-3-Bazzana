package ar.odonto.turnos.application.aviso;

/**
 * Vista de salida del caso de uso {@link GenerarLinkWhatsApp} (regla J2: nunca devolver entidades JPA).
 */
public record LinkWhatsAppVista(Long turnoId, String url, String mensaje) {
}
