package views;

import controllers.ReporteController;
import models.*;
import utils.ExportadorCSV;

import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ReportesPanel extends JPanel {
    private final ReporteController controller;
    private JTable tablaReportes;
    private DefaultTableModel modeloTabla;

    private JComboBox<String> comboTipoReporte;
    private JLabel lblParametro1, lblParametro2;
    private JTextField txtParametro1, txtParametro2;
    private JButton btnGenerar, btnExportar;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReportesPanel() {
        this.controller = new ReporteController();
        setLayout(new BorderLayout(10, 10));
        inicializarComponentes();
    }

    private void inicializarComponentes() {
        JPanel panelSuperior = new JPanel(new GridBagLayout());
        panelSuperior.setBorder(BorderFactory.createTitledBorder("Generador de Reportes Analíticos"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] tiposReporte = {
                "--- SELECCIONE UN REPORTE ---",
                "[PACIENTES] Reporte completo",
                "[PACIENTES] Por tipo de sangre",
                "[PACIENTES] Con mayor cantidad de citas",
                "[PACIENTES] Sin citas registradas",
                "[MÉDICOS] Reporte completo",
                "[MÉDICOS] Por especialidad",
                "[MÉDICOS] Con mayor cantidad de citas",
                "[MÉDICOS] Con citas en fecha específica",
                "[CITAS] Reporte completo",
                "[CITAS] Por rango de fechas",
                "[CITAS] Por médico (UUID)",
                "[CITAS] Por paciente (ID)",
                "[CITAS] Por estado",
                "[CITAS] Cantidad por especialidad",
                "[AUDITORÍA] Historial de Logs"
        };

        comboTipoReporte = new JComboBox<>(tiposReporte);
        comboTipoReporte.addActionListener(e -> ajustarFiltrosVisuales()); // Escuchador de cambios

        lblParametro1 = new JLabel("Parámetro 1:");
        txtParametro1 = new JTextField(12);
        txtParametro1.setEnabled(false);

        lblParametro2 = new JLabel("Parámetro 2:");
        txtParametro2 = new JTextField(12);
        txtParametro2.setEnabled(false);

        btnGenerar = new JButton("Generar");
        btnGenerar.addActionListener(e -> ejecutarReporte());

        btnExportar = new JButton("Exportar CSV");
        btnExportar.addActionListener(e -> {
            if (comboTipoReporte.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(this, "Seleccione y genere un reporte primero.");
                return;
            }
            String nombreReporte = (String) comboTipoReporte.getSelectedItem();
            String nombreArchivoValido = nombreReporte
                    .replace("[", "")
                    .replace("]", "")
                    .replace(" ", "_")
                    .replace("/", "_")
                    + ".csv";
            utils.ExportadorCSV.exportarTabla(this, modeloTabla, nombreArchivoValido);
        });

        gbc.gridx = 0; gbc.gridy = 0; panelSuperior.add(new JLabel("Reporte:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; panelSuperior.add(comboTipoReporte, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.gridx = 0; panelSuperior.add(lblParametro1, gbc);
        gbc.gridx = 1; panelSuperior.add(txtParametro1, gbc);
        gbc.gridx = 2; panelSuperior.add(lblParametro2, gbc);
        gbc.gridx = 3; panelSuperior.add(txtParametro2, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.add(btnGenerar);
        panelBotones.add(btnExportar);
        gbc.gridx = 4; panelSuperior.add(panelBotones, gbc);

        add(panelSuperior, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel();
        tablaReportes = new JTable(modeloTabla) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        add(new JScrollPane(tablaReportes), BorderLayout.CENTER);
    }

    private void ajustarFiltrosVisuales() {
        int index = comboTipoReporte.getSelectedIndex();
        txtParametro1.setText(""); txtParametro2.setText("");
        txtParametro1.setEnabled(false); txtParametro2.setEnabled(false);
        lblParametro1.setText("Filtro 1:"); lblParametro2.setText("Filtro 2:");

        switch (index) {
            case 2: // Pacientes por sangre
                lblParametro1.setText("Tipo Sangre:"); txtParametro1.setEnabled(true); break;
            case 6: // Medicos por especialidad
                lblParametro1.setText("Especialidad:"); txtParametro1.setEnabled(true); break;
            case 8: // Medicos en fecha
                lblParametro1.setText("Fecha (yyyy-MM-dd):"); txtParametro1.setEnabled(true); break;
            case 10: // Citas rango fechas
                lblParametro1.setText("Inicio (yyyy-MM-dd):"); txtParametro1.setEnabled(true);
                lblParametro2.setText("Fin (yyyy-MM-dd):"); txtParametro2.setEnabled(true); break;
            case 11: // Citas por medico
                lblParametro1.setText("UUID Médico:"); txtParametro1.setEnabled(true); break;
            case 12: // Citas por paciente
                lblParametro1.setText("ID Paciente:"); txtParametro1.setEnabled(true); break;
            case 13: // Citas por estado
                lblParametro1.setText("Estado (PROGRAMADA, etc):"); txtParametro1.setEnabled(true); break;
        }
    }


    private void ejecutarReporte() {
        int index = comboTipoReporte.getSelectedIndex();
        String p1 = txtParametro1.getText().trim();
        String p2 = txtParametro2.getText().trim();

        try {
            switch (index) {
                case 1: mostrarPacientes(controller.obtenerReporteCompletoPacientes()); break;
                case 2: mostrarPacientes(controller.obtenerPacientesPorTipoSangre(p1)); break;
                case 3: mostrarPacientes(controller.obtenerPacientesConMasCitas()); break;
                case 4: mostrarPacientes(controller.obtenerPacientesSinCitas()); break;

                case 5: mostrarMedicos(controller.obtenerReporteCompletoMedicos()); break;
                case 6: mostrarMedicos(controller.obtenerMedicosPorEspecialidad(p1)); break;
                case 7: mostrarMedicos(controller.obtenerMedicosConMasCitas()); break;
                case 8: mostrarMedicos(controller.obtenerMedicosConCitasEnFecha(LocalDate.parse(p1))); break;

                case 9: mostrarCitas(controller.obtenerReporteCompletoCitas()); break;
                case 10: mostrarCitas(controller.obtenerCitasPorRangoFechas(LocalDate.parse(p1), LocalDate.parse(p2))); break;
                case 11: mostrarCitas(controller.obtenerCitasPorMedico(p1)); break;
                case 12: mostrarCitas(controller.obtenerCitasPorPaciente(p1)); break;
                case 13: mostrarCitas(controller.obtenerCitasPorEstado(EstadoCita.valueOf(p1.toUpperCase()))); break;
                case 14: mostrarEstadistica(controller.obtenerCantidadCitasPorEspecialidad(), "Especialidad", "Total de Citas"); break;

                case 15: mostrarLogs(controller.obtenerTodosLosLogs()); break;

                default: JOptionPane.showMessageDialog(this, "Seleccione un reporte válido.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al generar reporte (Verifique parámetros): " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarPacientes(List<Paciente> lista) {
        modeloTabla.setColumnIdentifiers(new String[]{"ID", "Nombres", "Apellidos", "Nacimiento", "Sexo", "Sangre", "Teléfono"});
        modeloTabla.setRowCount(0);
        for (Paciente p : lista) {
            modeloTabla.addRow(new Object[]{p.getIdentificacion(), p.getNombres(), p.getApellidos(), p.getFechaNacimiento(), p.getSexo(), p.getTipoSangre(), p.getTelefono()});
        }
    }

    private void mostrarMedicos(List<Medico> lista) {
        modeloTabla.setColumnIdentifiers(new String[]{"UUID", "Nombres", "Apellidos", "Especialidad", "Horario", "Estado"});
        modeloTabla.setRowCount(0);
        for (Medico m : lista) {
            modeloTabla.addRow(new Object[]{m.getUuid(), m.getNombres(), m.getApellidos(), m.getEspecialidad(), m.getHorarioInicio() + " - " + m.getHorarioFin(), m.isActivo() ? "Activo" : "Inactivo"});
        }
    }

    private void mostrarCitas(List<Cita> lista) {
        modeloTabla.setColumnIdentifiers(new String[]{"UUID", "Paciente ID", "Médico UUID", "Fecha", "Hora", "Motivo", "Estado"});
        modeloTabla.setRowCount(0);
        for (Cita c : lista) {
            modeloTabla.addRow(new Object[]{c.getUuid(), c.getIdentificacionPaciente(), c.getUuidMedico(), c.getFecha(), c.getHoraInicio(), c.getMotivoConsulta(), c.getEstado().name()});
        }
    }

    private void mostrarLogs(List<Log> lista) {
        modeloTabla.setColumnIdentifiers(new String[]{"UUID Transacción", "Fecha/Hora", "Módulo", "Acción", "Detalle"});
        modeloTabla.setRowCount(0);
        for (Log l : lista) {
            modeloTabla.addRow(new Object[]{l.getId(), l.getFechaHora().format(DATETIME_FORMATTER), l.getModulo(), l.getAccion(), l.getDetalle()});
        }
    }

    private void mostrarEstadistica(Map<String, Long> mapa, String col1, String col2) {
        modeloTabla.setColumnIdentifiers(new String[]{col1, col2});
        modeloTabla.setRowCount(0);
        for (Map.Entry<String, Long> entry : mapa.entrySet()) {
            modeloTabla.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }
}
