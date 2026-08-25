package persistence;

import models.Paciente;
import utils.FixedLengthStringUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class PacienteDAO extends AbstractFileDAO{
    private static final String FILE_PATH = "data/pacientes.dat";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final int IDX_ID = 0;
    private static final int IDX_NOMBRES = 1;
    private static final int IDX_APELLIDOS = 2;
    private static final int IDX_FECHA_NAC = 3;
    private static final int IDX_SEXO = 4;
    private static final int IDX_TELEFONO = 5;
    private static final int IDX_CORREO = 6;
    private static final int IDX_TIPO_SANGRE = 7;

    private static final int[] LONGITUDES = {
            20, // Identificación personal
            50, // Nombres
            50, // Apellidos
            10, // Fecha de nacimiento (yyyy-MM-dd)
            15, // Sexo (Ej. "Masculino", "Femenino")
            15, // Teléfono
            50, // Correo electrónico
            5   // Tipo de sangre (Ej. "O+", "AB-")
    };

    private static final int BYTES_EXTRA = 1;

    public PacienteDAO() {
        super(LONGITUDES, BYTES_EXTRA, FILE_PATH);
    }

    public void registrarPaciente(Paciente paciente) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "rw")) {
            raf.seek(raf.length());
            escribirRegistro(raf, paciente);
        }
    }

    public List<Paciente> consultarTodos() throws IOException {
        List<Paciente> pacientes = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                Paciente p = leerRegistro(raf);
                if (!p.isEliminado()) {
                    pacientes.add(p);
                }
            }
        }
        return pacientes;
    }

    public boolean actualizarPaciente(Paciente paciente) throws IOException {
        int offsetEliminado = recordSize - BYTES_EXTRA;

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "rw")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();
                String idLeido = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_ID]);

                if (idLeido.equals(paciente.getIdentificacion())) {
                    raf.seek(posicionActual + offsetEliminado);
                    boolean estaEliminado = raf.readBoolean();

                    if (!estaEliminado) {
                        raf.seek(posicionActual);
                        escribirRegistro(raf, paciente);
                        return true;
                    }
                }
                raf.seek(posicionActual + recordSize);
            }
        }
        return false;
    }

    public boolean eliminarPaciente(String identificacion) throws IOException {
        int offsetEliminado = recordSize - BYTES_EXTRA;

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "rw")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();
                String idLeido = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_ID]);

                if (idLeido.equals(identificacion)) {
                    raf.seek(posicionActual + offsetEliminado);
                    boolean estaEliminado = raf.readBoolean();

                    if (!estaEliminado) {
                        raf.seek(posicionActual + offsetEliminado);
                        raf.writeBoolean(true);
                        return true;
                    }
                }
                raf.seek(posicionActual + recordSize);
            }
        }
        return false;
    }


    public boolean existeIdentificacion(String identificacion) throws IOException {
        int offsetEliminado = recordSize - BYTES_EXTRA;

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();
                String idLeido = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_ID]);

                if (idLeido.equals(identificacion)) {
                    // Verificamos que no esté eliminado
                    raf.seek(posicionActual + offsetEliminado);
                    boolean estaEliminado = raf.readBoolean();
                    if (!estaEliminado) {
                        return true;
                    }
                }
                raf.seek(posicionActual + recordSize);
            }
        }
        return false;
    }

    public List<Paciente> buscarGeneral(String criterio) throws IOException {
        List<Paciente> resultados = new ArrayList<>();
        String criterioLower = criterio.toLowerCase();

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posicionActual = raf.getFilePointer();

                String id = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_ID]);
                String nombres = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_NOMBRES]);
                String apellidos = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_APELLIDOS]);

                // Saltar hasta el final para leer si está eliminado
                raf.seek(posicionActual + (recordSize - BYTES_EXTRA));
                boolean estaEliminado = raf.readBoolean();

                if (!estaEliminado && (id.toLowerCase().contains(criterioLower) ||
                        nombres.toLowerCase().contains(criterioLower) ||
                        apellidos.toLowerCase().contains(criterioLower))) {

                    raf.seek(posicionActual);
                    resultados.add(leerRegistro(raf));
                } else {
                    raf.seek(posicionActual + recordSize);
                }
            }
        }
        return resultados;
    }

    public TreeSet<String> obtenerArbolIdsPacientes() throws IOException {
        TreeSet<String> arbolIds = new TreeSet<>();

        int offsetId = calcularOffset(IDX_ID);
        int offsetEliminado = recordSize - BYTES_EXTRA;

        try (RandomAccessFile raf = new RandomAccessFile(FILE_PATH, "r")) {
            raf.seek(0);
            while (raf.getFilePointer() < raf.length()) {
                long posActual = raf.getFilePointer();

                raf.seek(posActual + offsetEliminado);
                if (!raf.readBoolean()) {
                    raf.seek(posActual + offsetId);
                    String id = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_ID]);
                    arbolIds.add(id);
                }

                raf.seek(posActual + recordSize);
            }
        }
        return arbolIds;
    }

    private void escribirRegistro(RandomAccessFile raf, Paciente p) throws IOException {
        FixedLengthStringUtil.writeFixedString(raf, p.getIdentificacion(), LONGITUDES[IDX_ID]);
        FixedLengthStringUtil.writeFixedString(raf, p.getNombres(), LONGITUDES[IDX_NOMBRES]);
        FixedLengthStringUtil.writeFixedString(raf, p.getApellidos(), LONGITUDES[IDX_APELLIDOS]);
        FixedLengthStringUtil.writeFixedString(raf, p.getFechaNacimiento().format(DATE_FORMATTER), LONGITUDES[IDX_FECHA_NAC]);
        FixedLengthStringUtil.writeFixedString(raf, p.getSexo(), LONGITUDES[IDX_SEXO]);
        FixedLengthStringUtil.writeFixedString(raf, p.getTelefono(), LONGITUDES[IDX_TELEFONO]);
        FixedLengthStringUtil.writeFixedString(raf, p.getCorreoElectronico(), LONGITUDES[IDX_CORREO]);
        FixedLengthStringUtil.writeFixedString(raf, p.getTipoSangre(), LONGITUDES[IDX_TIPO_SANGRE]);
        raf.writeBoolean(p.isEliminado());
    }

    private Paciente leerRegistro(RandomAccessFile raf) throws IOException {
        String id = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_ID]);
        String nombres = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_NOMBRES]);
        String apellidos = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_APELLIDOS]);
        String fechaStr = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_FECHA_NAC]);
        String sexo = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_SEXO]);
        String telefono = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_TELEFONO]);
        String correo = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_CORREO]);
        String tipoSangre = FixedLengthStringUtil.readFixedString(raf, LONGITUDES[IDX_TIPO_SANGRE]);
        boolean eliminado = raf.readBoolean();

        LocalDate fechaNac = LocalDate.parse(fechaStr, DATE_FORMATTER);

        return new Paciente(id, nombres, apellidos, fechaNac, sexo, telefono, correo, tipoSangre, eliminado);
    }
}
