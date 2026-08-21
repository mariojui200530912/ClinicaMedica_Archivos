package models;

import utils.UUIDGenerator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class Cita {
    private String uuid;
    private String identificacionPaciente;
    private String uuidMedico;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private String motivoConsulta;
    private EstadoCita estado;
    private String observaciones;
    private boolean eliminado;

    public Cita(String identificacionPaciente, String uuidMedico, LocalDate fecha,
                LocalTime horaInicio, String motivoConsulta, String observaciones) {
        this.uuid = UUIDGenerator.generar();
        this.identificacionPaciente = identificacionPaciente;
        this.uuidMedico = uuidMedico;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.motivoConsulta = motivoConsulta;
        this.estado = EstadoCita.PROGRAMADA;
        this.observaciones = observaciones;
        this.eliminado = false;
    }

    public Cita(String uuid, String identificacionPaciente, String uuidMedico, LocalDate fecha,
                LocalTime horaInicio, String motivoConsulta, EstadoCita estado, String observaciones, boolean eliminado) {
        this.uuid = uuid;
        this.identificacionPaciente = identificacionPaciente;
        this.uuidMedico = uuidMedico;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.motivoConsulta = motivoConsulta;
        this.estado = estado;
        this.observaciones = observaciones;
        this.eliminado = eliminado;
    }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getIdentificacionPaciente() { return identificacionPaciente; }
    public void setIdentificacionPaciente(String identificacionPaciente) { this.identificacionPaciente = identificacionPaciente; }
    public String getUuidMedico() { return uuidMedico; }
    public void setUuidMedico(String uuidMedico) { this.uuidMedico = uuidMedico; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public String getMotivoConsulta() { return motivoConsulta; }
    public void setMotivoConsulta(String motivoConsulta) { this.motivoConsulta = motivoConsulta; }
    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }
}
