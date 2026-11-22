package ar.odonto.turnos.application.aviso;

import ar.odonto.turnos.domain.error.CodigoError;
import ar.odonto.turnos.domain.error.ReglaDeNegocioException;
import ar.odonto.turnos.domain.model.Consultorio;
import ar.odonto.turnos.domain.model.Paciente;
import ar.odonto.turnos.domain.model.Prestacion;
import ar.odonto.turnos.domain.model.Profesional;
import ar.odonto.turnos.domain.model.Sillon;
import ar.odonto.turnos.domain.model.Turno;
import ar.odonto.turnos.domain.port.ConsultorioRepository;
import ar.odonto.turnos.domain.port.HorarioAtencionRepository;
import ar.odonto.turnos.domain.port.PacienteRepository;
import ar.odonto.turnos.domain.port.PrestacionRepository;
import ar.odonto.turnos.domain.port.ProfesionalRepository;
import ar.odonto.turnos.domain.port.SillonRepository;
import ar.odonto.turnos.domain.port.TurnoRepository;
import ar.odonto.turnos.support.DatosDePrueba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de aplicación del caso de uso {@link GenerarLinkWhatsApp} (US-030, RN-WA-01..04).
 * Arma los turnos directamente con los repositorios de C-01 (independiente de C-02).
 * Datos 100 % ficticios (regla D1 / RN-DP-01).
 */
@SpringBootTest
@Transactional
class GenerarLinkWhatsAppTest {

    @Autowired GenerarLinkWhatsApp generarLinkWhatsApp;
    @Autowired ConsultorioRepository consultorios;
    @Autowired HorarioAtencionRepository horarios;
    @Autowired ProfesionalRepository profesionales;
    @Autowired SillonRepository sillones;
    @Autowired PacienteRepository pacientes;
    @Autowired PrestacionRepository prestaciones;
    @Autowired TurnoRepository turnos;

    Profesional ana;
    Sillon sillon1;
    Prestacion control;
    Prestacion limpieza;

    @BeforeEach
    void contextoBase() {
        Consultorio consultorio = consultorios.save(DatosDePrueba.consultorio());
        for (DayOfWeek dia : EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)) {
            horarios.save(DatosDePrueba.horario(consultorio, dia));
        }
        ana = profesionales.save(DatosDePrueba.profesional("MP-0001"));
        sillon1 = sillones.save(DatosDePrueba.sillon("Sillón 1"));
        control = prestaciones.save(DatosDePrueba.prestacion("Control", 30));
        limpieza = prestaciones.save(DatosDePrueba.prestacion("Limpieza", 45));
    }

    @Test
    void generaLinkYMensajeSinDniNiDatosClinicos() {
        Paciente paciente = pacientes.save(DatosDePrueba.paciente(1));
        Turno turno = turnos.save(DatosDePrueba.turno(ana, sillon1, paciente, limpieza,
                "2026-11-24T14:00", "2026-11-24T14:45"));

        LinkWhatsAppVista vista = generarLinkWhatsApp.ejecutar(turno.getId());

        assertThat(vista.turnoId()).isEqualTo(turno.getId());
        assertThat(vista.url()).startsWith("https://wa.me/5491100000001?text=");
        assertThat(vista.mensaje())
                .contains("2026-11-24", "14:00", "Ana Ejemplo", "Limpieza")
                .doesNotContain(paciente.getDni());
    }

    @Test
    void turnoInexistenteLanzaRecursoInexistente() {
        assertThatThrownBy(() -> generarLinkWhatsApp.ejecutar(999999L))
                .isInstanceOfSatisfying(ReglaDeNegocioException.class,
                        e -> assertThat(e.codigo()).isEqualTo(CodigoError.RECURSO_INEXISTENTE));
    }

    @Test
    void pacienteSinTelefonoLanzaPacienteSinTelefono() {
        Paciente sinTelefono = pacientes.save(new Paciente("Paciente", "SinTel", "00000008", null, null));
        Turno turno = turnos.save(DatosDePrueba.turno(ana, sillon1, sinTelefono, control,
                "2026-11-24T10:30", "2026-11-24T11:00"));

        assertThatThrownBy(() -> generarLinkWhatsApp.ejecutar(turno.getId()))
                .isInstanceOfSatisfying(ReglaDeNegocioException.class,
                        e -> assertThat(e.codigo()).isEqualTo(CodigoError.PACIENTE_SIN_TELEFONO));
    }

    @Test
    void pacienteConTelefonoEnBlancoLanzaPacienteSinTelefono() {
        Paciente telefonoBlanco = pacientes.save(new Paciente("Paciente", "TelBlanco", "00000009", "   ", null));
        Turno turno = turnos.save(DatosDePrueba.turno(ana, sillon1, telefonoBlanco, control,
                "2026-11-24T10:30", "2026-11-24T11:00"));

        assertThatThrownBy(() -> generarLinkWhatsApp.ejecutar(turno.getId()))
                .isInstanceOfSatisfying(ReglaDeNegocioException.class,
                        e -> assertThat(e.codigo()).isEqualTo(CodigoError.PACIENTE_SIN_TELEFONO));
    }
}
