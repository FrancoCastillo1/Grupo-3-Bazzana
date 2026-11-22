package ar.odonto.turnos.infrastructure.whatsapp;

import ar.odonto.turnos.application.aviso.NotificadorDeTurnos;
import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Implementación MVP click-to-chat ({@code wa.me}) del puerto {@link NotificadorDeTurnos} (DD-09, RN-WA-01..04).
 * <p>
 * Normaliza el teléfono conservando únicamente los dígitos y codifica el mensaje en UTF-8 para URL.
 * No envía mensajes ni realiza llamadas HTTP externas (RN-WA-04).
 */
@Component
public class WaMeLinkGenerator implements NotificadorDeTurnos {

    private static final String BASE_URL = "https://wa.me/";

    @Override
    public String generarLink(String telefono, String mensaje) {
        String telefonoNormalizado = normalizarTelefono(telefono);
        String mensajeSeguro = mensaje == null ? "" : mensaje;
        String mensajeCodificado = URLEncoder.encode(mensajeSeguro, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return BASE_URL + telefonoNormalizado + "?text=" + mensajeCodificado;
    }

    private static String normalizarTelefono(String telefono) {
        if (telefono == null) {
            throw new ReglaDeNegocioException(CodigoError.PACIENTE_SIN_TELEFONO);
        }
        StringBuilder digitos = new StringBuilder(telefono.length());
        for (int i = 0; i < telefono.length(); i++) {
            char c = telefono.charAt(i);
            if (c >= '0' && c <= '9') {
                digitos.append(c);
            }
        }
        if (digitos.isEmpty()) {
            throw new ReglaDeNegocioException(CodigoError.PACIENTE_SIN_TELEFONO);
        }
        return digitos.toString();
    }
}
