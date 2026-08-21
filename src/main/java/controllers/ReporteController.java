package controllers;

import models.*;
import persistence.CitaDAO;
import persistence.LogDAO;
import persistence.MedicoDAO;
import persistence.PacienteDAO;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ReporteController {
    private final LogDAO logDAO;
    private final CitaDAO citaDAO;
    private final MedicoDAO medicoDAO;
    private final PacienteDAO pacienteDAO;

    public ReporteController() {
        this.logDAO = new LogDAO();
        this.citaDAO = new CitaDAO();
        this.medicoDAO = new MedicoDAO();
        this.pacienteDAO = new PacienteDAO();
    }

    // -- REPORTE DE LOGS
    public List<Log> obtenerTodosLosLogs() throws IOException {
        return logDAO.consultarTodos();
    }

    public List<Log> filtrarPorModulo(String modulo) throws IOException {
        String moduloLower = modulo.toLowerCase();
        return logDAO.consultarTodos().stream()
                .filter(log -> log.getModulo().toLowerCase().contains(moduloLower))
                .collect(Collectors.toList());
    }

    // -- REPORTES DE PACIENTES
    public List<Paciente> obtenerReporteCompletoPacientes() throws IOException {
        return pacienteDAO.consultarTodos();
    }

    public List<Paciente> obtenerPacientesPorTipoSangre(String tipoSangre) throws IOException {
        String sangreFiltro = tipoSangre.trim().toUpperCase();

        return pacienteDAO.consultarTodos().stream()
                .filter(p -> p.getTipoSangre().trim().toUpperCase().equals(sangreFiltro))
                .collect(Collectors.toList());
    }

    public List<Paciente> obtenerPacientesConMasCitas() throws IOException {
        Map<String, Long> conteoCitas = citaDAO.contarCitasPorPaciente();

        List<String> idsOrdenados = conteoCitas.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Paciente> todosLosPacientes = pacienteDAO.consultarTodos();
        List<Paciente> resultado = new ArrayList<>();

        for (String id : idsOrdenados) {
            todosLosPacientes.stream()
                    .filter(p -> p.getIdentificacion().equals(id))
                    .findFirst()
                    .ifPresent(resultado::add);
        }
        return resultado;
    }

    public List<Paciente> obtenerPacientesSinCitas() throws IOException {
        // pacientes con al menos 1 cita
        Set<String> idsConCitas = citaDAO.contarCitasPorPaciente().keySet();

        return pacienteDAO.consultarTodos().stream()
                .filter(p -> !idsConCitas.contains(p.getIdentificacion()))
                .collect(Collectors.toList());
    }

    // -- REPORTES DE MEDICOS
    public List<Medico> obtenerReporteCompletoMedicos() throws IOException {
        return medicoDAO.consultarTodos();
    }

    public List<Medico> obtenerMedicosPorEspecialidad(String especialidad) throws IOException {
        String espLower = especialidad.trim().toLowerCase();
        return medicoDAO.consultarTodos().stream()
                .filter(m -> m.getEspecialidad().toLowerCase().contains(espLower))
                .collect(Collectors.toList());
    }

    public List<Medico> obtenerMedicosConMasCitas() throws IOException {
        Map<String, Long> conteoCitas = citaDAO.contarCitasPorMedico();

        List<String> uuidsOrdenados = conteoCitas.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Medico> todosLosMedicos = medicoDAO.consultarTodos();
        List<Medico> resultado = new ArrayList<>();

        for (String uuid : uuidsOrdenados) {
            todosLosMedicos.stream()
                    .filter(m -> m.getUuid().equals(uuid))
                    .findFirst()
                    .ifPresent(resultado::add);
        }
        return resultado;
    }

    public List<Medico> obtenerMedicosConCitasEnFecha(LocalDate fecha) throws IOException {
        Set<String> uuidsEnFecha = citaDAO.obtenerUuidsMedicosPorFecha(fecha);

        return medicoDAO.consultarTodos().stream()
                .filter(m -> uuidsEnFecha.contains(m.getUuid()))
                .collect(Collectors.toList());
    }


    // -- REPORTES DE CITAS
    public List<Cita> obtenerReporteCompletoCitas() throws IOException {
        return citaDAO.consultarTodas();
    }

    public List<Cita> obtenerCitasPorRangoFechas(java.time.LocalDate inicio, java.time.LocalDate fin) throws IOException {
        return citaDAO.consultarCitasPorRango(inicio, fin);
    }

    public List<Cita> obtenerCitasPorMedico(String uuidMedico) throws IOException {
        return citaDAO.buscarPorMedico(uuidMedico);
    }

    public List<Cita> obtenerCitasPorPaciente(String idPaciente) throws IOException {
        return citaDAO.buscarPorPaciente(idPaciente);
    }

    public List<Cita> obtenerCitasPorEstado(EstadoCita estado) throws IOException {
        return citaDAO.buscarPorEstado(estado);
    }

    public Map<String, Long> obtenerCantidadCitasPorEspecialidad() throws IOException {
        List<String> uuidsEnCitas = citaDAO.obtenerUuidsMedicosConCitas();

        Map<String, String> mapaEspecialidades = medicoDAO.obtenerMapaEspecialidades();

        return uuidsEnCitas.stream()
                .map(uuid -> mapaEspecialidades.getOrDefault(uuid, "Especialidad Desconocida"))
                .collect(Collectors.groupingBy(especialidad -> especialidad, Collectors.counting()));
    }
}
