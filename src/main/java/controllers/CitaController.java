package controllers;

import models.Cita;
import models.EstadoCita;
import models.Medico;
import persistence.CitaDAO;
import persistence.MedicoDAO;
import utils.LoggerSystem;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CitaController {

    private final CitaDAO citaDAO;
    private final MedicoDAO medicoDAO;

    public CitaController() {
        this.citaDAO = new CitaDAO();
        this.medicoDAO = new MedicoDAO();
    }


    public void programarCita(String idPaciente, String uuidMedico, LocalDate fecha,
                              LocalTime horaInicio, String motivo) throws Exception {

        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de la consulta es obligatorio.");
        }

        Medico medico = obtenerMedicoPorUuid(uuidMedico);
        if (medico == null) {
            throw new Exception("El médico seleccionado no existe.");
        }

        if (!medico.isActivo()) {
            throw new Exception("No se pueden programar citas con un médico inactivo.");
        }

        if (horaInicio.isBefore(medico.getHorarioInicio()) || horaInicio.isAfter(medico.getHorarioFin())) {
            throw new Exception("La hora de la cita está fuera del horario de atención del médico ("
                    + medico.getHorarioInicio() + " - " + medico.getHorarioFin() + ").");
        }

        if (existeColisionDeHorario(uuidMedico, fecha, horaInicio)) {
            throw new Exception("El médico ya tiene una cita programada en esa fecha y hora.");
        }

        Cita nuevaCita = new Cita(idPaciente, uuidMedico, fecha, horaInicio, motivo);
        citaDAO.registrarCita(nuevaCita);
        LoggerSystem.registrarAccion("CITAS", "CREACIÓN", "Se programó cita para el paciente ID: " + idPaciente);
    }

    public List<Cita> consultarTodasLasCitas() throws IOException {
        return citaDAO.consultarTodas();
    }


    public Cita buscarCitaPorUuid(String uuid) throws IOException {
        return citaDAO.buscarPorUuid(uuid);
    }

    public List<Cita> consultarCitasPorPaciente(String idPaciente) throws IOException {
        return citaDAO.buscarPorPaciente(idPaciente);
    }


    public List<Cita> consultarCitasPorMedico(String uuidMedico) throws IOException {
        return citaDAO.buscarPorMedico(uuidMedico);
    }

    public List<Cita> consultarCitasPorFecha(LocalDate fecha) throws IOException {
        return citaDAO.buscarPorFecha(fecha);
    }

    public List<Cita> consultarCitasPorEstado(EstadoCita estado) throws IOException {
        return citaDAO.buscarPorEstado(estado);
    }

    public void modificarCita(String uuid, String nuevoMotivo, String nuevasObservaciones) throws Exception {
        Cita citaExistente = buscarCitaPorUuid(uuid);
        if (citaExistente == null || citaExistente.isEliminado()) {
            throw new Exception("Cita no encontrada o ha sido eliminada.");
        }

        citaExistente.setMotivoConsulta(nuevoMotivo);
        citaExistente.setObservaciones(nuevasObservaciones != null ? nuevasObservaciones : "");

        boolean actualizado = citaDAO.actualizarCita(citaExistente);
        if (!actualizado) {
            throw new Exception("Error al actualizar la cita en el archivo.");
        }
        LoggerSystem.registrarAccion("CITAS", "ACTUALIZACIÓN", "Se modificó el motivo/observaciones de la cita UUID: " + uuid);
    }

    public void cambiarEstadoCita(String uuid, EstadoCita nuevoEstado) throws Exception {
        Cita citaExistente = buscarCitaPorUuid(uuid);
        if (citaExistente == null || citaExistente.isEliminado()) {
            throw new Exception("Cita no encontrada o ha sido eliminada.");
        }

        citaExistente.setEstado(nuevoEstado);
        citaDAO.actualizarCita(citaExistente);
        LoggerSystem.registrarAccion("CITAS", "ACTUALIZACIÓN", "El estado de la cita UUID: " + uuid + " cambió a " + nuevoEstado.name());
    }


    public void eliminarCita(String uuid) throws Exception {
        boolean eliminado = citaDAO.eliminarCita(uuid);
        if (!eliminado) {
            throw new Exception("No se encontró la cita para eliminar.");
        }
        LoggerSystem.registrarAccion("CITAS", "ELIMINACIÓN", "Se eliminó lógicamente la cita UUID: " + uuid);
    }

    private Medico obtenerMedicoPorUuid(String uuid) throws IOException {
        List<Medico> resultados = medicoDAO.buscarGeneral(uuid);
        for (Medico m : resultados) {
            if (m.getUuid().equals(uuid)) {
                return m;
            }
        }
        return null;
    }

    private boolean existeColisionDeHorario(String uuidMedico, LocalDate fecha, LocalTime hora) throws IOException {
        List<Cita> citasMedico = consultarCitasPorMedico(uuidMedico);
        for (Cita c : citasMedico) {
            if (c.getEstado() == EstadoCita.PROGRAMADA && c.getFecha().equals(fecha) && c.getHoraInicio().equals(hora)) {
                return true;
            }
        }
        return false;
    }
}
