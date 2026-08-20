package persistence;

import models.Log;
import utils.FixedLengthStringUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LogDAO extends AbstractFileDAO{
    private static final String FILE_PATH = "data/logs.dat";
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int IDX_ID = 0;
    private static final int IDX_FECHA = 1;
    private static final int IDX_MODULO = 2;
    private static final int IDX_ACCION = 3;
    private static final int IDX_DETALLE = 4;

    private static final int[] LONGITUDES = {
            36,  // ID (UUID)
            19,  // Fecha y hora
            15,  // Módulo
            20,  // Acción
            100  // Detalle
    };

    public LogDAO() {
        super(LONGITUDES, 0); // 0 bytes extra, puro texto
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();
    }

    public void registrarLog(Log log) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "rw")) {
            raf.seek(raf.length());
            FixedLengthStringUtil.writeFixedString(raf, log.getId(), LONGITUDES[IDX_ID]);
            FixedLengthStringUtil.writeFixedString(raf, log.getFechaHora().format(DATETIME_FORMATTER), LONGITUDES[IDX_FECHA]);
            FixedLengthStringUtil.writeFixedString(raf, log.getModulo(), LONGITUDES[IDX_MODULO]);
            FixedLengthStringUtil.writeFixedString(raf, log.getAccion(), LONGITUDES[IDX_ACCION]);
            FixedLengthStringUtil.writeFixedString(raf, log.getDetalle(), LONGITUDES[IDX_DETALLE]);
        }
    }


    public List<Log> consultarTodos() throws IOException {
        List<Log> logs = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                String id = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_ID]);
                String fechaStr = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_FECHA]);
                String modulo = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_MODULO]);
                String accion = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_ACCION]);
                String detalle = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_DETALLE]);

                LocalDateTime fecha = LocalDateTime.parse(fechaStr, DATETIME_FORMATTER);
                logs.add(new Log(id, fecha, modulo, accion, detalle));
            }
        }
        return logs;
    }
}
