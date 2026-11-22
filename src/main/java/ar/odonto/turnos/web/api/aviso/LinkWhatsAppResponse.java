package ar.odonto.turnos.web.api.aviso;

import ar.odonto.turnos.application.aviso.LinkWhatsAppVista;

/**
 * DTO de respuesta de {@code GET /api/turnos/{id}/whatsapp-link} (regla J2).
 */
public record LinkWhatsAppResponse(Long turnoId, String url, String mensaje) {

    static LinkWhatsAppResponse de(LinkWhatsAppVista vista) {
        return new LinkWhatsAppResponse(vista.turnoId(), vista.url(), vista.mensaje());
    }
}
