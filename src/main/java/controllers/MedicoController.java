package controllers;

import models.Medico;
import persistence.MedicoDAO;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class MedicoController {
    private final MedicoDAO medicoDAO;

    public MedicoController() {
        this.medicoDAO = new MedicoDAO();
    }

    public void registrarMedico(String nombres, String apellidos, String especialidad,
                                String telefono, String correoElectronico,
                                LocalTime horarioInicio, LocalTime horarioFin) throws Exception {

        validarCamposObligatorios(nombres, apellidos, especialidad);

        if (horarioInicio == null || horarioFin == null || horarioInicio.isAfter(horarioFin)) {
            throw new IllegalArgumentException("El horario de inicio debe ser anterior al horario de fin.");
        }

        Medico nuevoMedico = new Medico(nombres, apellidos, especialidad, telefono,
                correoElectronico, horarioInicio, horarioFin);

        medicoDAO.registrarMedico(nuevoMedico);
        // Aquí se llamaría al LoggerSystem para registrar la acción de creación.
    }

    public List<Medico> consultarTodosLosMedicos() throws IOException {
        return medicoDAO.consultarTodos();
    }

    public List<Medico> buscarMedicos(String criterio) throws IOException {
        return medicoDAO.buscarGeneral(criterio);
    }

    public List<Medico> consultarMedicosPorEstado(boolean activo) throws IOException {
        return medicoDAO.buscarPorEstado(activo);
    }

    public List<Medico> consultarMedicosPorEspecialidad(String especialidad) throws IOException {
        return medicoDAO.buscarPorEspecialidad(especialidad);
    }

    public void modificarMedico(String uuid, String nombres, String apellidos, String especialidad,
                                String telefono, String correoElectronico,
                                LocalTime nuevoInicio, LocalTime nuevoFin) throws Exception {

        validarCamposObligatorios(nombres, apellidos, especialidad);

        // TODO: Validar que la modificación del horario no genere inconsistencias con citas programadas.
        // Esto requerirá buscar en CitaDAO si el médico tiene citas fuera del nuevo rango.

        List<Medico> todos = medicoDAO.consultarTodos();
        Medico medicoExistente = todos.stream()
                .filter(m -> m.getUuid().equals(uuid))
                .findFirst()
                .orElseThrow(() -> new Exception("Médico no encontrado."));

        medicoExistente.setNombres(nombres);
        medicoExistente.setApellidos(apellidos);
        medicoExistente.setEspecialidad(especialidad);
        medicoExistente.setTelefono(telefono);
        medicoExistente.setCorreoElectronico(correoElectronico);
        medicoExistente.setHorarioInicio(nuevoInicio);
        medicoExistente.setHorarioFin(nuevoFin);

        boolean actualizado = medicoDAO.actualizarMedico(medicoExistente);
        if (!actualizado) {
            throw new Exception("Error al actualizar el médico en el archivo.");
        }
        // Aquí se registraría la actualización en el log.
    }

    public void cambiarEstadoMedico(String uuid, boolean nuevoEstado) throws Exception {
        List<Medico> todos = medicoDAO.consultarTodos();
        Medico medicoExistente = todos.stream()
                .filter(m -> m.getUuid().equals(uuid))
                .findFirst()
                .orElseThrow(() -> new Exception("Médico no encontrado."));

        medicoExistente.setActivo(nuevoEstado);
        medicoDAO.actualizarMedico(medicoExistente);
        // Aquí se registraría el cambio de estado en el log[cite: 1].
    }

    private void validarCamposObligatorios(String nombres, String apellidos, String especialidad) {
        if (nombres == null || nombres.trim().isEmpty()) {
            throw new IllegalArgumentException("Los nombres son obligatorios.");
        }
        if (apellidos == null || apellidos.trim().isEmpty()) {
            throw new IllegalArgumentException("Los apellidos son obligatorios.");
        }
        if (especialidad == null || especialidad.trim().isEmpty()) {
            throw new IllegalArgumentException("La especialidad es obligatoria.");
        }
    }
}
