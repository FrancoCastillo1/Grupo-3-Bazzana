package ar.odonto.turnos.infrastructure.whatsapp;

import ar.odonto.turnos.application.aviso.NotificadorDeTurnos;
import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitarios (JUnit puro, sin Spring) del generador de links click-to-chat {@code wa.me} (RN-WA-01, RN-WA-03).
 * Todos los datos son 100 % ficticios (regla D1 / RN-DP-01).
 */
class WaMeLinkGeneratorTest {

    private final NotificadorDeTurnos generador = new WaMeLinkGenerator();

    @Test
    void generaLinkValidoConTelefonoInternacionalYMensajeCodificado() {
        String mensaje = "Hola, le recordamos su turno de Control con Ana Ejemplo el 2026-11-24 a las 10:30.";
        String link = generador.generarLink("5491100000001", mensaje);

        assertThat(link).startsWith("https://wa.me/5491100000001?text=");
        URI uri = URI.create(link);
        assertThat(uri.getScheme()).isEqualTo("https");
        assertThat(uri.getHost()).isEqualTo("wa.me");
        assertThat(uri.getPath()).isEqualTo("/5491100000001");

        String textoDecodificado = URLDecoder.decode(
                link.substring("https://wa.me/5491100000001?text=".length()),
                StandardCharsets.UTF_8);
        assertThat(textoDecodificado).isEqualTo(mensaje);
    }

    @Test
    void codificaEnUtf8EspaciosTildesYEnie() {
        String mensaje = "Turno de Extracción con Iñaki Muñoz el 2026-11-24 a las 14:00.";
        String link = generador.generarLink("5491100000002", mensaje);

        // Debe ser una URI válida (sin espacios literales ni caracteres fuera de ASCII en la query)
        URI uri = URI.create(link);
        assertThat(uri.getRawQuery()).doesNotContain(" ");
        assertThat(uri.getRawQuery()).contains("%C3%B1"); // ñ en UTF-8
        assertThat(uri.getRawQuery()).contains("%C3%B3"); // ó en UTF-8

        String textoDecodificado = URLDecoder.decode(
                link.substring("https://wa.me/5491100000002?text=".length()),
                StandardCharsets.UTF_8);
        assertThat(textoDecodificado)
                .isEqualTo(mensaje)
                .contains("Iñaki Muñoz", "Extracción");
    }

    @Test
    void normalizaTelefonoQuitandoSignoMasEspaciosYGuiones() {
        String mensaje = "Turno de Control con Ana Ejemplo el 2026-11-24 a las 10:30.";
        String link = generador.generarLink("+54 9 11 0000-0003", mensaje);

        assertThat(link).startsWith("https://wa.me/5491100000003?text=");
    }

    @Test
    void rechazaTelefonoNullConPacienteSinTelefono() {
        assertThatThrownBy(() -> generador.generarLink(null, "Mensaje"))
                .isInstanceOfSatisfying(ReglaDeNegocioException.class,
                        e -> assertThat(e.codigo()).isEqualTo(CodigoError.PACIENTE_SIN_TELEFONO));
    }

    @Test
    void rechazaTelefonoVacioOSinDigitosConPacienteSinTelefono() {
        assertThatThrownBy(() -> generador.generarLink("   ", "Mensaje"))
                .isInstanceOfSatisfying(ReglaDeNegocioException.class,
                        e -> assertThat(e.codigo()).isEqualTo(CodigoError.PACIENTE_SIN_TELEFONO));

        assertThatThrownBy(() -> generador.generarLink("+ - ()", "Mensaje"))
                .isInstanceOfSatisfying(ReglaDeNegocioException.class,
                        e -> assertThat(e.codigo()).isEqualTo(CodigoError.PACIENTE_SIN_TELEFONO));
    }
}
