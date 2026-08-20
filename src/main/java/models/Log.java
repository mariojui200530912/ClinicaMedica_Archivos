package models;

import java.time.LocalDateTime;

public class Log {
    private String id;
    private LocalDateTime fechaHora;
    private String modulo;
    private String accion;
    private String detalle;

    public Log(String id, LocalDateTime fechaHora, String modulo, String accion, String detalle) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.modulo = modulo;
        this.accion = accion;
        this.detalle = detalle;
    }

    public String getId() { return id; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getModulo() { return modulo; }
    public String getAccion() { return accion; }
    public String getDetalle() { return detalle; }

}
