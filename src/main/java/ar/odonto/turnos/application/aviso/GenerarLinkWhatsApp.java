package ar.odonto.turnos.application.aviso;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.model.Paciente;
import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.model.Turno;
import ar.odonto.turnos.domain.port.TurnoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

/**
 * Caso de uso "generar link de WhatsApp de un turno" (US-030, Flujo 4, RN-WA-01 a RN-WA-04).
 * <p>
 * Carga el turno dentro de una transacción de solo lectura ({@code open-in-view=false}), valida que el paciente
 * tenga teléfono cargado, arma el mensaje con fecha, hora, profesional y prestación sin datos clínicos (RN-WA-02),
 * y delega la construcción del link {@code wa.me} en {@link NotificadorDeTurnos} sin enviar mensajes (RN-WA-04).
 * Nunca registra DNI, teléfono ni nombre completo del paciente en los logs (RN-DP-03, regla D2).
 */
@Service
public class GenerarLinkWhatsApp {

    private static final Logger log = LoggerFactory.getLogger(GenerarLinkWhatsApp.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final TurnoRepository turnos;
    private final NotificadorDeTurnos notificador;

    public GenerarLinkWhatsApp(TurnoRepository turnos, NotificadorDeTurnos notificador) {
        this.turnos = turnos;
        this.notificador = notificador;
    }

    @Transactional(readOnly = true)
    public LinkWhatsAppVista ejecutar(Long turnoId) {
        Turno turno = turnos.findById(turnoId)
                .orElseThrow(() -> new ReglaDeNegocioException(CodigoError.RECURSO_INEXISTENTE,
                        "No existe el turno indicado"));

        Paciente paciente = turno.getPaciente();
        if (paciente == null || !paciente.tieneTelefono()) {
            throw new ReglaDeNegocioException(CodigoError.PACIENTE_SIN_TELEFONO);
        }

        Profesional profesional = turno.getProfesional();
        Prestacion prestacion = turno.getPrestacion();
        String fecha = turno.getInicio().format(FORMATO_FECHA);
        String hora = turno.getInicio().format(FORMATO_HORA);
        String nombreProfesional = profesional.getNombre() + " " + profesional.getApellido();

        String mensaje = "Hola, le recordamos su turno de %s con %s el %s a las %s."
                .formatted(prestacion.getNombre(), nombreProfesional, fecha, hora);

        String url = notificador.generarLink(paciente.getTelefono(), mensaje);

        // Regla D2 / RN-DP-03: solo se loguea el ID del turno, nunca teléfono, DNI ni nombre del paciente
        log.info("Link de WhatsApp generado para turno {}", turno.getId());
        return new LinkWhatsAppVista(turno.getId(), url, mensaje);
    }
}
