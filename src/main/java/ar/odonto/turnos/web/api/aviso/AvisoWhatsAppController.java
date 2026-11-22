package ar.odonto.turnos.web.api.aviso;

import ar.odonto.turnos.application.aviso.GenerarLinkWhatsApp;
import ar.odonto.turnos.application.aviso.LinkWhatsAppVista;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API de generación de link click-to-chat de WhatsApp para un turno (C-06, US-030).
 * <p>
 * Controlador PROPIO: no edita {@code TurnoController} de C-02 (reglas anti-colisión de CHANGES.md).
 * Sin lógica de negocio (regla J1): solo delega en {@link GenerarLinkWhatsApp}; los errores los traduce
 * el manejador global de C-01 a Problem Details con {@code codigo} (404 / 422).
 */
@RestController
@RequestMapping("/api/turnos")
class AvisoWhatsAppController {

    private final GenerarLinkWhatsApp generarLinkWhatsApp;

    AvisoWhatsAppController(GenerarLinkWhatsApp generarLinkWhatsApp) {
        this.generarLinkWhatsApp = generarLinkWhatsApp;
    }

    @GetMapping("/{id}/whatsapp-link")
    ResponseEntity<LinkWhatsAppResponse> obtenerLinkWhatsApp(@PathVariable Long id) {
        LinkWhatsAppVista vista = generarLinkWhatsApp.ejecutar(id);
        return ResponseEntity.ok(LinkWhatsAppResponse.de(vista));
    }
}
