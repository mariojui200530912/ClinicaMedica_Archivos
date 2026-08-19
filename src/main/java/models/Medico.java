package models;

import java.time.LocalTime;
import java.util.UUID;

public class Medico {
    private String uuid;
    private String nombres;
    private String apellidos;
    private String especialidad;
    private String telefono;
    private String correoElectronico;
    private LocalTime horarioInicio;
    private LocalTime horarioFin;
    private boolean activo;

    public Medico(String nombres, String apellidos, String especialidad, String telefono,
                  String correoElectronico, LocalTime horarioInicio, LocalTime horarioFin) {
        this.uuid = UUID.randomUUID().toString();
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.especialidad = especialidad;
        this.telefono = telefono;
        this.correoElectronico = correoElectronico;
        this.horarioInicio = horarioInicio;
        this.horarioFin = horarioFin;
        this.activo = true;
    }

    public Medico(String uuid, String nombres, String apellidos, String especialidad, String telefono,
                  String correoElectronico, LocalTime horarioInicio, LocalTime horarioFin, boolean activo) {
        this.uuid = uuid;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.especialidad = especialidad;
        this.telefono = telefono;
        this.correoElectronico = correoElectronico;
        this.horarioInicio = horarioInicio;
        this.horarioFin = horarioFin;
        this.activo = activo;
    }

    public String getUuid() {
        return uuid;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public LocalTime getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(LocalTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public LocalTime getHorarioFin() {
        return horarioFin;
    }

    public void setHorarioFin(LocalTime horarioFin) {
        this.horarioFin = horarioFin;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Medico{" +
                "uuid='" + uuid + '\'' +
                ", nombres='" + nombres + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", especialidad='" + especialidad + '\'' +
                ", estado=" + (activo ? "Activo" : "Inactivo") +
                '}';
    }
}
