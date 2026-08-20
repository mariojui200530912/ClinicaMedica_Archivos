package controllers;

import models.Paciente;
import persistence.PacienteDAO;
import utils.LoggerSystem;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class PacienteController {
    private final PacienteDAO pacienteDAO;

    public PacienteController() {
        this.pacienteDAO = new PacienteDAO();
    }

    public void registrarPaciente(String identificacion, String nombres, String apellidos,
                                  LocalDate fechaNacimiento, String sexo, String telefono,
                                  String correoElectronico, String tipoSangre) throws Exception {

        validarCamposObligatorios(identificacion, nombres, apellidos);

        if (pacienteDAO.existeIdentificacion(identificacion)) {
            throw new Exception("El número de identificación ingresado ya se encuentra registrado.");
        }

        Paciente nuevoPaciente = new Paciente(identificacion, nombres, apellidos, fechaNacimiento,
                sexo, telefono, correoElectronico, tipoSangre);

        pacienteDAO.registrarPaciente(nuevoPaciente);
        LoggerSystem.registrarAccion("PACIENTES", "CREACIÓN", "Se registró un nuevo paciente con ID: " + identificacion);
    }

    public List<Paciente> consultarTodosLosPacientes() throws IOException {
        return pacienteDAO.consultarTodos();
    }

    public List<Paciente> buscarPacientes(String criterio) throws IOException {
        return pacienteDAO.buscarGeneral(criterio);
    }

    public void modificarPaciente(String identificacionOriginal, String nombres, String apellidos,
                                  LocalDate fechaNacimiento, String sexo, String telefono,
                                  String correoElectronico, String tipoSangre) throws Exception {

        validarCamposObligatorios(identificacionOriginal, nombres, apellidos);

        if (!pacienteDAO.existeIdentificacion(identificacionOriginal)) {
            throw new Exception("El paciente que intenta modificar no existe o ha sido eliminado.");
        }

        Paciente pacienteActualizado = new Paciente(identificacionOriginal, nombres, apellidos,
                fechaNacimiento, sexo, telefono,
                correoElectronico, tipoSangre);

        boolean actualizado = pacienteDAO.actualizarPaciente(pacienteActualizado);
        if (!actualizado) {
            throw new Exception("Error al actualizar la información del paciente en el archivo.");
        }
        LoggerSystem.registrarAccion("PACIENTES", "ACTUALIZACIÓN", "Se actualizaron los datos del paciente con ID: " + identificacionOriginal);
    }

    public void eliminarPaciente(String identificacion) throws Exception {
        boolean eliminado = pacienteDAO.eliminarPaciente(identificacion);
        if (!eliminado) {
            throw new Exception("No se encontró el paciente para eliminar.");
        }
        LoggerSystem.registrarAccion("PACIENTES", "ELIMINACIÓN", "Se eliminó lógicamente al paciente con ID: " + identificacion);
    }

    private void validarCamposObligatorios(String identificacion, String nombres, String apellidos) {
        if (identificacion == null || identificacion.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de identificación es obligatorio.");
        }
        if (nombres == null || nombres.trim().isEmpty()) {
            throw new IllegalArgumentException("Los nombres son obligatorios.");
        }
        if (apellidos == null || apellidos.trim().isEmpty()) {
            throw new IllegalArgumentException("Los apellidos son obligatorios.");
        }
    }
}
