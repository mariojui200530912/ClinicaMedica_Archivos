package persistence;

import models.Medico;
import utils.FixedLengthStringUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicoDAO extends AbstractFileDAO{
    private static final String FILE_PATH = "data/medicos.dat";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private static final int IDX_UUID = 0;
    private static final int IDX_NOMBRES = 1;
    private static final int IDX_APELLIDOS = 2;
    private static final int IDX_ESPECIALIDAD = 3;
    private static final int IDX_TELEFONO = 4;
    private static final int IDX_CORREO = 5;
    private static final int IDX_HORARIO_INICIO = 6;
    private static final int IDX_HORARIO_FIN = 7;

    // Longitudes exactas de los campos en caracteres
    private static final int[] LONGITUDES = {
            36, // UUID
            50, // Nombres
            50, // Apellidos
            40, // Especialidad
            15, // Teléfono
            50, // Correo
            5,  // Horario Inicio
            5   // Horario Fin
    };

    private static final int BYTES_EXTRA = 1;

    public MedicoDAO() {
        super(LONGITUDES, BYTES_EXTRA, FILE_PATH);
    }

    public void registrarMedico(Medico medico) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "rw")) {
            raf.seek(raf.length());
            escribirRegistro(raf, medico);
        }
    }

    public List<Medico> consultarTodos() throws IOException {
        List<Medico> medicos = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                medicos.add(leerRegistro(raf));
            }
        }
        return medicos;
    }

    public boolean actualizarMedico(Medico medicoActualizado) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "rw")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();
                String uuidLeido = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_UUID]);

                if (uuidLeido.equals(medicoActualizado.getUuid())) {
                    raf.seek(posicionActual);
                    escribirRegistro(raf, medicoActualizado);
                    return true;
                }
                raf.seek(posicionActual + recordSize);
            }
        }
        return false;
    }

    public boolean cambiarEstadoMedico(String uuid, boolean nuevoEstado) throws IOException {
        int offsetEstado = recordSize - BYTES_EXTRA;

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "rw")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();
                String uuidLeido = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_UUID]);

                if (uuidLeido.equals(uuid)) {
                    raf.seek(posicionActual + offsetEstado);

                    boolean estadoActual = raf.readBoolean();

                    if (estadoActual != nuevoEstado) {
                        raf.seek(posicionActual + offsetEstado); // Retrocedemos el byte
                        raf.writeBoolean(nuevoEstado); // Actualizamos
                        return true;
                    }
                    return false; // El médico ya tenía ese estado
                }
                raf.seek(posicionActual + recordSize);
            }
        }
        return false;
    }

    public Medico buscarPorUuid(String uuidBuscado) throws IOException {
        int longitudUuid = LONGITUDES[IDX_UUID];

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();

                String uuidLeido = FixedLengthStringUtil.readFixedString(raf, longitudUuid);

                if (uuidLeido.equals(uuidBuscado)) {
                    raf.seek(posicionActual);
                    return leerRegistro(raf);
                }

                raf.seek(posicionActual + recordSize);
            }
        }
        return null;
    }

    public List<Medico> buscarGeneral(String criterio) throws IOException {
        List<Medico> resultados = new ArrayList<>();
        String criterioLower = criterio.toLowerCase();

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();

                // Leemos solo lo necesario para la búsqueda
                String uuid = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_UUID]);
                String nombres = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_NOMBRES]);
                String apellidos = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_APELLIDOS]);
                String especialidad = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_ESPECIALIDAD]);

                if (uuid.toLowerCase().contains(criterioLower) ||
                        nombres.toLowerCase().contains(criterioLower) ||
                        apellidos.toLowerCase().contains(criterioLower) ||
                        especialidad.toLowerCase().contains(criterioLower)) {

                    // Si coincide, leemos el registro completo
                    raf.seek(posicionActual);
                    resultados.add(leerRegistro(raf));
                } else {
                    raf.seek(posicionActual + recordSize);
                }
            }
        }
        return resultados;
    }

    public List<Medico> buscarPorEspecialidad(String especialidadBuscada) throws IOException {
        List<Medico> resultados = new ArrayList<>();
        String espLower = especialidadBuscada.toLowerCase();

        int offsetEspecialidad = calcularOffset(IDX_ESPECIALIDAD);
        int longitudEspecialidad = LONGITUDES[IDX_ESPECIALIDAD];

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();

                raf.seek(posicionActual + offsetEspecialidad);
                String especialidadLeida = FixedLengthStringUtil.readFixedString(raf, longitudEspecialidad);

                if (especialidadLeida.toLowerCase().contains(espLower)) {
                    raf.seek(posicionActual);
                    resultados.add(leerRegistro(raf));
                } else {
                    raf.seek(posicionActual + recordSize);
                }
            }
        }
        return resultados;
    }

    public List<Medico> buscarPorEstado(boolean estadoBuscado) throws IOException {
        List<Medico> resultados = new ArrayList<>();

        int offsetEstado = recordSize - BYTES_EXTRA;

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();

                raf.seek(posicionActual + offsetEstado);
                boolean estadoLeido = raf.readBoolean();

                if (estadoLeido == estadoBuscado) {
                    raf.seek(posicionActual);
                    resultados.add(leerRegistro(raf));
                } else {
                    raf.seek(posicionActual + recordSize);
                }
            }
        }
        return resultados;
    }

    public boolean existeMedicoDuplicado(String nombresBuscados, String apellidosBuscados, String especialidadBuscada) throws IOException {
        String nomLower = nombresBuscados.toLowerCase();
        String apeLower = apellidosBuscados.toLowerCase();
        String espLower = especialidadBuscada.toLowerCase();

        int offsetNombres = calcularOffset(IDX_NOMBRES);

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();

                raf.seek(posicionActual + offsetNombres);

                String nombresLeidos = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_NOMBRES]);
                String apellidosLeidos = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_APELLIDOS]);
                String especialidadLeida = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_ESPECIALIDAD]);

                if (nombresLeidos.toLowerCase().equals(nomLower) &&
                        apellidosLeidos.toLowerCase().equals(apeLower) &&
                        especialidadLeida.toLowerCase().equals(espLower)) {
                    return true;
                }

                raf.seek(posicionActual + recordSize);
            }
        }
        return false;
    }

    public Map<String, String> obtenerMapaEspecialidades() throws IOException {
        Map<String, String> mapaEspecialidades = new HashMap<>();
        int offsetEspecialidad = calcularOffset(IDX_ESPECIALIDAD);

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posActual = raf.getFilePointer();

                raf.seek(posActual);
                String uuid = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_UUID]);

                raf.seek(posActual + offsetEspecialidad);
                String especialidad = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_ESPECIALIDAD]);

                mapaEspecialidades.put(uuid, especialidad);

                raf.seek(posActual + recordSize);
            }
        }
        return mapaEspecialidades;
    }

    private void escribirRegistro(RandomAccessFile raf, Medico medico) throws IOException {
        FixedLengthStringUtil.writeFixedString(raf, medico.getUuid(), LONGITUDES[IDX_UUID]);
        FixedLengthStringUtil.writeFixedString(raf, medico.getNombres(), LONGITUDES[IDX_NOMBRES]);
        FixedLengthStringUtil.writeFixedString(raf, medico.getApellidos(), LONGITUDES[IDX_APELLIDOS]);
        FixedLengthStringUtil.writeFixedString(raf, medico.getEspecialidad(), LONGITUDES[IDX_ESPECIALIDAD]);
        FixedLengthStringUtil.writeFixedString(raf, medico.getTelefono(), LONGITUDES[IDX_TELEFONO]);
        FixedLengthStringUtil.writeFixedString(raf, medico.getCorreoElectronico(), LONGITUDES[IDX_CORREO]);
        FixedLengthStringUtil.writeFixedString(raf, medico.getHorarioInicio().format(TIME_FORMATTER), LONGITUDES[IDX_HORARIO_INICIO]);
        FixedLengthStringUtil.writeFixedString(raf, medico.getHorarioFin().format(TIME_FORMATTER), LONGITUDES[IDX_HORARIO_FIN]);
        raf.writeBoolean(medico.isActivo());
    }

    private Medico leerRegistro(RandomAccessFile raf) throws IOException {
        String uuid = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_UUID]);
        String nombres = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_NOMBRES]);
        String apellidos = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_APELLIDOS]);
        String especialidad = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_ESPECIALIDAD]);
        String telefono = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_TELEFONO]);
        String correo = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_CORREO]);
        String hInicioStr = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_HORARIO_INICIO]);
        String hFinStr = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_HORARIO_FIN]);
        boolean activo = raf.readBoolean();

        LocalTime hInicio = LocalTime.parse(hInicioStr, TIME_FORMATTER);
        LocalTime hFin = LocalTime.parse(hFinStr, TIME_FORMATTER);

        return new Medico(uuid, nombres, apellidos, especialidad, telefono, correo, hInicio, hFin, activo);
    }

}
