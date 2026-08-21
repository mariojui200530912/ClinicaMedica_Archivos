package persistence;

import models.Cita;
import models.EstadoCita;
import utils.FixedLengthStringUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CitaDAO extends AbstractFileDAO{
    private static final String FILE_PATH = "data/citas.dat";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private static final int IDX_UUID = 0;
    private static final int IDX_ID_PACIENTE = 1;
    private static final int IDX_UUID_MEDICO = 2;
    private static final int IDX_FECHA = 3;
    private static final int IDX_HORA = 4;
    private static final int IDX_MOTIVO = 5;
    private static final int IDX_ESTADO = 6;
    private static final int IDX_OBSERVACIONES = 7;
    private static final int BYTES_EXTRA = 1;

    private static final int[] LONGITUDES = {
            36,  // UUID
            15,  // Identificación del paciente
            36,  // UUID del médico
            10,  // Fecha (yyyy-MM-dd)
            5,   // Hora (HH:mm)
            100, // Motivo de la consulta
            15,  // Estado (PROGRAMADA, ATENDIDA, CANCELADA)
            150  // Observaciones
    };

    public CitaDAO() {
        super(LONGITUDES, BYTES_EXTRA,  FILE_PATH);
    }

    public void registrarCita(Cita cita) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "rw")) {
            raf.seek(raf.length());
            escribirRegistro(raf, cita);
        }
    }

    public List<Cita> consultarTodas() throws IOException {
        List<Cita> citas = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                Cita cita = leerRegistro(raf);
                if (!cita.isEliminado()) {
                    citas.add(cita);
                }
            }
        }
        return citas;
    }

    public boolean actualizarCita(Cita citaActualizada) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "rw")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();
                String uuidLeido = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_UUID]);

                if (uuidLeido.equals(citaActualizada.getUuid())) {
                    raf.seek(posicionActual);
                    escribirRegistro(raf, citaActualizada);
                    return true;
                }
                raf.seek(posicionActual + recordSize);
            }
        }
        return false;
    }

    public boolean eliminarCita(String uuidBuscado) throws IOException {
        int offsetEliminada = recordSize - BYTES_EXTRA; // Posición exacta del booleano

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "rw")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();
                String uuidLeido = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_UUID]);

                if (uuidLeido.equals(uuidBuscado)) {
                    // Saltamos directamente al byte del booleano
                    raf.seek(posicionActual + offsetEliminada);
                    raf.writeBoolean(true); // Cambiamos la bandera a true
                    return true;
                }
                raf.seek(posicionActual + recordSize);
            }
        }
        return false;
    }

    public Cita buscarPorUuid(String uuidBuscado) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();
                String uuidLeido = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_UUID]);

                if (uuidLeido.equals(uuidBuscado)) {
                    raf.seek(posicionActual);
                    return leerRegistro(raf);
                }
                raf.seek(posicionActual + recordSize);
            }
        }
        return null;
    }

    public List<Cita> buscarPorPaciente(String idPacienteBuscado) throws IOException {
        List<Cita> resultados = new ArrayList<>();
        int offsetId = calcularOffset(IDX_ID_PACIENTE);
        int longitudId = LONGITUDES[IDX_ID_PACIENTE];

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();

                raf.seek(posicionActual + offsetId);
                String idLeido = FixedLengthStringUtil.readFixedString(raf, longitudId);

                if (idLeido.equals(idPacienteBuscado)) {
                    raf.seek(posicionActual);
                    resultados.add(leerRegistro(raf));
                } else {
                    raf.seek(posicionActual + recordSize);
                }
            }
        }
        return resultados;
    }

    public List<Cita> buscarPorMedico(String uuidMedicoBuscado) throws IOException {
        List<Cita> resultados = new ArrayList<>();
        int offsetMedico = calcularOffset(IDX_UUID_MEDICO);
        int longitudMedico = LONGITUDES[IDX_UUID_MEDICO];

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();

                raf.seek(posicionActual + offsetMedico);
                String medicoLeido = FixedLengthStringUtil.readFixedString(raf, longitudMedico);

                if (medicoLeido.equals(uuidMedicoBuscado)) {
                    raf.seek(posicionActual);
                    resultados.add(leerRegistro(raf));
                } else {
                    raf.seek(posicionActual + recordSize);
                }
            }
        }
        return resultados;
    }

    public List<Cita> buscarPorFecha(LocalDate fechaBuscada) throws IOException {
        List<Cita> resultados = new ArrayList<>();

        int offsetFecha = calcularOffset(IDX_FECHA);
        int longitudFecha = LONGITUDES[IDX_FECHA];

        String fechaStrBuscada = fechaBuscada.format(DATE_FORMATTER);

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();

                raf.seek(posicionActual + offsetFecha);
                String fechaLeida = FixedLengthStringUtil.readFixedString(raf, longitudFecha);

                if (fechaLeida.equals(fechaStrBuscada)) {
                    raf.seek(posicionActual);
                    Cita cita = leerRegistro(raf);

                    if (!cita.isEliminado()) {
                        resultados.add(cita);
                    }
                } else {
                    raf.seek(posicionActual + recordSize);
                }
            }
        }
        return resultados;
    }

    public List<Cita> buscarPorEstado(EstadoCita estadoBuscado) throws IOException {
        List<Cita> resultados = new ArrayList<>();
        int offsetEstado = calcularOffset(IDX_ESTADO);
        int longitudEstado = LONGITUDES[IDX_ESTADO];
        String estadoStr = estadoBuscado.name();

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();

                raf.seek(posicionActual + offsetEstado);
                String estadoLeido = FixedLengthStringUtil.readFixedString(raf, longitudEstado);

                if (estadoLeido.equals(estadoStr)) {
                    raf.seek(posicionActual);
                    resultados.add(leerRegistro(raf));
                } else {
                    raf.seek(posicionActual + recordSize);
                }
            }
        }
        return resultados;
    }

    public Map<String, Long> contarCitasPorPaciente() throws IOException {
        Map<String, Long> conteo = new HashMap<>();
        int offsetIdPaciente = calcularOffset(IDX_ID_PACIENTE);
        int offsetEliminado = recordSize - BYTES_EXTRA;

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posActual = raf.getFilePointer();

                raf.seek(posActual + offsetEliminado);
                if (!raf.readBoolean()) {
                    raf.seek(posActual + offsetIdPaciente);
                    String id = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_ID_PACIENTE]);
                    conteo.put(id, conteo.getOrDefault(id, 0L) + 1);
                }
                raf.seek(posActual + recordSize);
            }
        }
        return conteo;
    }

    public Map<String, Long> contarCitasPorMedico() throws IOException {
        Map<String, Long> conteo = new HashMap<>();
        int offsetUuidMedico = calcularOffset(IDX_UUID_MEDICO); // Índice del UUID del médico
        int offsetEliminado = recordSize - BYTES_EXTRA;

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posActual = raf.getFilePointer();

                raf.seek(posActual + offsetEliminado);
                if (!raf.readBoolean()) { // Si no está eliminada
                    raf.seek(posActual + offsetUuidMedico);
                    String uuid = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_UUID_MEDICO]);
                    conteo.put(uuid, conteo.getOrDefault(uuid, 0L) + 1);
                }
                raf.seek(posActual + recordSize);
            }
        }
        return conteo;
    }

    public Set<String> obtenerUuidsMedicosPorFecha(LocalDate fechaBusqueda) throws IOException {
        Set<String> uuidsEnFecha = new HashSet<>();
        int offsetUuidMedico = calcularOffset(IDX_UUID_MEDICO);
        int offsetFecha = calcularOffset(IDX_FECHA);
        int offsetEliminado = recordSize - BYTES_EXTRA;

        String fechaStrBusqueda = fechaBusqueda.toString();

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posActual = raf.getFilePointer();

                raf.seek(posActual + offsetEliminado);
                if (!raf.readBoolean()) {
                    raf.seek(posActual + offsetFecha);
                    String fechaRegistro = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_FECHA]);

                    if (fechaRegistro.equals(fechaStrBusqueda)) {
                        raf.seek(posActual + offsetUuidMedico);
                        String uuid = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_UUID_MEDICO]);
                        uuidsEnFecha.add(uuid);
                    }
                }
                raf.seek(posActual + recordSize);
            }
        }
        return uuidsEnFecha;
    }

    public List<String> obtenerUuidsMedicosConCitas() throws IOException {
        List<String> uuidsMedicos = new java.util.ArrayList<>();
        int offsetUuidMedico = calcularOffset(IDX_UUID_MEDICO);
        int offsetEliminado = recordSize - BYTES_EXTRA;

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posActual = raf.getFilePointer();

                raf.seek(posActual + offsetEliminado);
                if (!raf.readBoolean()) {
                    raf.seek(posActual + offsetUuidMedico);
                    uuidsMedicos.add(FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_UUID_MEDICO]));
                }
                raf.seek(posActual + recordSize);
            }
        }
        return uuidsMedicos;
    }

    public List<Cita> consultarCitasPorRango(LocalDate inicio, LocalDate fin) throws IOException {
        List<Cita> resultados = new ArrayList<>();

        String strInicio = inicio.toString();
        String strFin = fin.toString();

        int offsetFecha = calcularOffset(IDX_FECHA);
        int offsetEliminado = recordSize - BYTES_EXTRA;

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posActual = raf.getFilePointer();

                raf.seek(posActual + offsetEliminado);
                if (!raf.readBoolean()) {
                    raf.seek(posActual + offsetFecha);
                    String fechaRegistro = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_FECHA]);

                    if (fechaRegistro.compareTo(strInicio) >= 0 && fechaRegistro.compareTo(strFin) <= 0) {
                        raf.seek(posActual);
                        resultados.add(leerRegistro(raf));
                    }
                }
                raf.seek(posActual + recordSize);
            }
        }
        return resultados;
    }

    private void escribirRegistro(RandomAccessFile raf, Cita cita) throws IOException {
        FixedLengthStringUtil.writeFixedString(raf, cita.getUuid(), LONGITUDES[IDX_UUID]);
        FixedLengthStringUtil.writeFixedString(raf, cita.getIdentificacionPaciente(), LONGITUDES[IDX_ID_PACIENTE]);
        FixedLengthStringUtil.writeFixedString(raf, cita.getUuidMedico(), LONGITUDES[IDX_UUID_MEDICO]);
        FixedLengthStringUtil.writeFixedString(raf, cita.getFecha().format(DATE_FORMATTER), LONGITUDES[IDX_FECHA]);
        FixedLengthStringUtil.writeFixedString(raf, cita.getHoraInicio().format(TIME_FORMATTER), LONGITUDES[IDX_HORA]);
        FixedLengthStringUtil.writeFixedString(raf, cita.getMotivoConsulta(), LONGITUDES[IDX_MOTIVO]);
        FixedLengthStringUtil.writeFixedString(raf, cita.getEstado().name(), LONGITUDES[IDX_ESTADO]);
        FixedLengthStringUtil.writeFixedString(raf, cita.getObservaciones(), LONGITUDES[IDX_OBSERVACIONES]);
        raf.writeBoolean(cita.isEliminado());
    }

    private Cita leerRegistro(RandomAccessFile raf) throws IOException {
        String uuid = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_UUID]);
        String idPaciente = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_ID_PACIENTE]);
        String uuidMedico = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_UUID_MEDICO]);
        String fechaStr = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_FECHA]);
        String horaStr = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_HORA]);
        String motivo = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_MOTIVO]);
        String estadoStr = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_ESTADO]);
        String observaciones = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_OBSERVACIONES]);
        boolean eliminada = raf.readBoolean();

        LocalDate fecha = LocalDate.parse(fechaStr, DATE_FORMATTER);
        LocalTime hora = LocalTime.parse(horaStr, TIME_FORMATTER);
        EstadoCita estado = EstadoCita.valueOf(estadoStr);

        return new Cita(uuid, idPaciente, uuidMedico, fecha, hora, motivo, estado, observaciones, eliminada);
    }
}
