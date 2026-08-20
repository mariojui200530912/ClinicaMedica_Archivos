package utils;

import models.Log;
import persistence.LogDAO;

import java.time.LocalDateTime;


public class LoggerSystem {
    private static final LogDAO logDAO = new LogDAO();

    public static void registrarAccion(String modulo, String accion, String detalle) {
        try {
            String id = UUIDGenerator.generar();
            Log nuevoLog = new Log(id, LocalDateTime.now(), modulo, accion, detalle);

            logDAO.registrarLog(nuevoLog);
        } catch (Exception e) {
            System.err.println("Error crítico: No se pudo registrar el log. " + e.getMessage());
        }
    }
}
