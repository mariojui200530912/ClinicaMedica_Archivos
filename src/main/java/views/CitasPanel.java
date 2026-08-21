package views;

import controllers.CitaController;
import controllers.MedicoController;
import controllers.PacienteController;
import models.Cita;
import models.EstadoCita;
import models.Medico;
import models.Paciente;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class CitasPanel extends JPanel{
    private final CitaController controller;
    private final PacienteController pacienteController;
    private final MedicoController medicoController;
    private JTable tablaCitas;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> comparadorFiltro;

    private JComboBox<String> comboCriterioBusqueda;
    private JTextField txtValorBusqueda;
    private JButton btnBuscarTabla, btnRestablecerTabla;

    private JTextField txtInfoPaciente, txtInfoMedico, txtFecha, txtHora, txtMotivo, txtObservaciones;
    private JButton btnBuscarPaciente, btnBuscarMedico, btnProgramar, btnModificar, btnCambiarEstado, btnEliminar, btnLimpiar;

    private String idPacienteSeleccionado = null;
    private String uuidMedicoSeleccionado = null;
    private String uuidCitaSeleccionada = null;

    public CitasPanel() {
        this.controller = new CitaController();
        this.medicoController = new MedicoController();
        this.pacienteController = new PacienteController();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inicializarComponentes();
        cargarDatosEnTabla();
    }

    private void inicializarComponentes() {

        JPanel panelCentral = new JPanel(new BorderLayout(0, 10));

        String[] columnas = {"UUID Cita", "ID Paciente", "UUID Médico", "Fecha", "Hora", "Motivo", "Estado", "Observaciones"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 1 || column == 2;
            }
        };
        tablaCitas = new JTable(modeloTabla);
        comparadorFiltro = new TableRowSorter<>(modeloTabla);
        tablaCitas.setRowSorter(comparadorFiltro);

        JPanel panelAccionesTabla = new JPanel(new FlowLayout(FlowLayout.LEFT));

        comboCriterioBusqueda = new JComboBox<>(new String[]{
                "UUID Cita",
                "ID Paciente",
                "UUID Médico",
                "Fecha (yyyy-MM-dd)",
                "Estado (PROGRAMADA/ATENDIDA...)"
        });

        txtValorBusqueda = new JTextField(15);
        btnBuscarTabla = new JButton("Buscar");
        btnBuscarTabla.addActionListener(e -> realizarBusquedaBackend());

        btnRestablecerTabla = new JButton("Mostrar Todas");
        btnRestablecerTabla.addActionListener(e -> {
            txtValorBusqueda.setText("");
            cargarDatosEnTabla();
        });

        panelAccionesTabla.add(new JLabel("Buscar por:"));
        panelAccionesTabla.add(comboCriterioBusqueda);
        panelAccionesTabla.add(txtValorBusqueda);
        panelAccionesTabla.add(btnBuscarTabla);
        panelAccionesTabla.add(btnRestablecerTabla);

        panelCentral.add(panelAccionesTabla, BorderLayout.NORTH);

        JTextField campoCopiable = new JTextField();
        campoCopiable.setEditable(false);
        DefaultCellEditor editorSoloLectura = new DefaultCellEditor(campoCopiable);
        tablaCitas.getColumnModel().getColumn(0).setCellEditor(editorSoloLectura);
        tablaCitas.getColumnModel().getColumn(1).setCellEditor(editorSoloLectura);
        tablaCitas.getColumnModel().getColumn(2).setCellEditor(editorSoloLectura);

        JPopupMenu menuContextual = new JPopupMenu();
        JMenuItem itemVerPaciente = new JMenuItem("👁️ Ver Info del Paciente");
        itemVerPaciente.addActionListener(e -> {
            int fila = tablaCitas.getSelectedRow();
            if(fila != -1) mostrarInformacionPaciente((String) modeloTabla.getValueAt(tablaCitas.convertRowIndexToModel(fila), 1));
        });
        JMenuItem itemVerMedico = new JMenuItem("⚕️ Ver Info del Médico");
        itemVerMedico.addActionListener(e -> {
            int fila = tablaCitas.getSelectedRow();
            if(fila != -1) mostrarInformacionMedico((String) modeloTabla.getValueAt(tablaCitas.convertRowIndexToModel(fila), 2));
        });
        JMenuItem itemCopiarPaciente = new JMenuItem("📋 Copiar ID Paciente");
        itemCopiarPaciente.addActionListener(e -> {
            int fila = tablaCitas.getSelectedRow();
            if(fila != -1) {
                java.awt.datatransfer.StringSelection sel = new java.awt.datatransfer.StringSelection((String) modeloTabla.getValueAt(tablaCitas.convertRowIndexToModel(fila), 1));
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
            }
        });
        JMenuItem itemCopiarMedico = new JMenuItem("📋 Copiar UUID Médico");
        itemCopiarMedico.addActionListener(e -> {
            int fila = tablaCitas.getSelectedRow();
            if(fila != -1) {
                java.awt.datatransfer.StringSelection sel = new java.awt.datatransfer.StringSelection((String) modeloTabla.getValueAt(tablaCitas.convertRowIndexToModel(fila), 2));
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
            }
        });

        menuContextual.add(itemVerPaciente);
        menuContextual.add(itemCopiarPaciente);
        menuContextual.addSeparator();
        menuContextual.add(itemVerMedico);
        menuContextual.add(itemCopiarMedico);
        tablaCitas.setComponentPopupMenu(menuContextual);

        tablaCitas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaCitas.getSelectedRow() != -1) {
                cargarCitaSeleccionada();
            }
        });

        panelCentral.add(new JScrollPane(tablaCitas), BorderLayout.CENTER);
        add(panelCentral, BorderLayout.CENTER);

        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos de la Cita y Acciones"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Paciente:"), gbc);
        JPanel panelPaciente = new JPanel(new BorderLayout(5, 0));
        txtInfoPaciente = new JTextField("Ninguno seleccionado");
        txtInfoPaciente.setEditable(false); txtInfoPaciente.setBackground(Color.WHITE);
        btnBuscarPaciente = new JButton("Buscar...");
        btnBuscarPaciente.addActionListener(e -> abrirBuscadorPaciente());
        panelPaciente.add(txtInfoPaciente, BorderLayout.CENTER);
        panelPaciente.add(btnBuscarPaciente, BorderLayout.EAST);
        gbc.gridx = 1; gbc.weightx = 0.35;
        panelFormulario.add(panelPaciente, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Fecha (yyyy-MM-dd):"), gbc);
        txtFecha = new JTextField();
        gbc.gridx = 3; gbc.weightx = 0.35;
        panelFormulario.add(txtFecha, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Médico:"), gbc);
        JPanel panelMedico = new JPanel(new BorderLayout(5, 0));
        txtInfoMedico = new JTextField("Ninguno seleccionado");
        txtInfoMedico.setEditable(false); txtInfoMedico.setBackground(Color.WHITE);
        btnBuscarMedico = new JButton("Buscar...");
        btnBuscarMedico.addActionListener(e -> abrirBuscadorMedico());
        panelMedico.add(txtInfoMedico, BorderLayout.CENTER);
        panelMedico.add(btnBuscarMedico, BorderLayout.EAST);
        gbc.gridx = 1; gbc.weightx = 0.35;
        panelFormulario.add(panelMedico, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Hora (HH:mm):"), gbc);
        txtHora = new JTextField();
        gbc.gridx = 3; gbc.weightx = 0.35;
        panelFormulario.add(txtHora, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Motivo:"), gbc);
        txtMotivo = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.35;
        panelFormulario.add(txtMotivo, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Observaciones:"), gbc);
        txtObservaciones = new JTextField();
        gbc.gridx = 3; gbc.weightx = 0.35;
        panelFormulario.add(txtObservaciones, gbc);

        JPanel panelBotonesForm = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

        btnProgramar = new JButton("Programar Cita");
        btnProgramar.addActionListener(e -> programarCita());

        btnModificar = new JButton("Modificar Textos");
        btnModificar.setEnabled(false);
        btnModificar.addActionListener(e -> modificarCita());

        btnCambiarEstado = new JButton("Cambiar Estado");
        btnCambiarEstado.setEnabled(false);
        btnCambiarEstado.addActionListener(e -> cambiarEstadoCita());

        btnEliminar = new JButton("Eliminar Cita");
        btnEliminar.setEnabled(false);
        btnEliminar.addActionListener(e -> eliminarCita());

        btnLimpiar = new JButton("Limpiar / Cancelar");
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBotonesForm.add(btnProgramar);
        panelBotonesForm.add(btnModificar);
        panelBotonesForm.add(btnCambiarEstado);
        panelBotonesForm.add(btnEliminar);
        panelBotonesForm.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        gbc.insets = new Insets(15, 5, 5, 5);
        panelFormulario.add(panelBotonesForm, gbc);

        add(panelFormulario, BorderLayout.SOUTH);
    }

    private void realizarBusquedaBackend() {
        String valor = txtValorBusqueda.getText().trim();

        if (valor.isEmpty()) {
            cargarDatosEnTabla();
            return;
        }

        int criterio = comboCriterioBusqueda.getSelectedIndex();
        List<Cita> resultados = new ArrayList<>();

        try {
            switch (criterio) {
                case 0: // UUID
                    Cita c = controller.buscarCitaPorUuid(valor);
                    if (c != null) resultados.add(c);
                    break;
                case 1: // ID Paciente
                    resultados = controller.consultarCitasPorPaciente(valor);
                    break;
                case 2: // UUID Médico
                    resultados = controller.consultarCitasPorMedico(valor);
                    break;
                case 3: // Fecha
                    resultados = controller.consultarCitasPorFecha(LocalDate.parse(valor));
                    break;
                case 4: // Estado
                    resultados = controller.consultarCitasPorEstado(EstadoCita.valueOf(valor.toUpperCase()));
                    break;
            }

            poblarTabla(resultados);

            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron citas con ese criterio de búsqueda.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Utilice el formato: yyyy-MM-dd", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Estado inválido. Ingrese PROGRAMADA, ATENDIDA o CANCELADA.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al realizar la búsqueda: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void poblarTabla(List<Cita> listaCitas) {
        modeloTabla.setRowCount(0); // Limpiar la tabla actual
        for (Cita c : listaCitas) {
            modeloTabla.addRow(new Object[]{
                    c.getUuid(), c.getIdentificacionPaciente(), c.getUuidMedico(),
                    c.getFecha(), c.getHoraInicio(), c.getMotivoConsulta(), c.getEstado().name(), c.getObservaciones()
            });
        }
    }

    private void cargarDatosEnTabla() {
        try {
            List<Cita> citas = controller.consultarTodasLasCitas();
            poblarTabla(citas);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos: " + e.getMessage());
        }
    }

    private void abrirBuscadorPaciente() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        BuscadorPacienteDialog dialog = new BuscadorPacienteDialog(parentWindow);
        dialog.setVisible(true);

        Paciente p = dialog.getPacienteSeleccionado();
        if (p != null) {
            idPacienteSeleccionado = p.getIdentificacion();
            txtInfoPaciente.setText(p.getNombres() + " " + p.getApellidos());
        }
    }

    private void abrirBuscadorMedico() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        BuscadorMedicoDialog dialog = new BuscadorMedicoDialog(parentWindow);
        dialog.setVisible(true);

        Medico m = dialog.getMedicoSeleccionado();
        if (m != null) {
            uuidMedicoSeleccionado = m.getUuid();
            txtInfoMedico.setText("Dr. " + m.getNombres() + " " + m.getApellidos());
        }
    }

    private void programarCita() {
        if (idPacienteSeleccionado == null || uuidMedicoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un paciente y un médico mediante los botones de búsqueda.");
            return;
        }
        try {
            LocalDate fecha = LocalDate.parse(txtFecha.getText());
            LocalTime hora = LocalTime.parse(txtHora.getText());
            controller.programarCita(idPacienteSeleccionado, uuidMedicoSeleccionado, fecha, hora, txtMotivo.getText(), txtObservaciones.getText());
            JOptionPane.showMessageDialog(this, "Cita programada exitosamente.");
            limpiarFormulario();
            cargarDatosEnTabla();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha u hora inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificarCita() {
        if (uuidCitaSeleccionada == null) return;
        String nuevoMotivo = txtMotivo.getText().trim();
        String nuevasObservaciones = txtObservaciones.getText().trim();
        if (nuevoMotivo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El motivo no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            controller.modificarCita(uuidCitaSeleccionada, nuevoMotivo, nuevasObservaciones);
            JOptionPane.showMessageDialog(this, "Textos de la cita modificados exitosamente.");
            limpiarFormulario();
            cargarDatosEnTabla();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al modificar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cambiarEstadoCita() {
        if (uuidCitaSeleccionada == null) return;
        EstadoCita[] opciones = EstadoCita.values();
        int seleccion = JOptionPane.showOptionDialog(this, "Seleccione el nuevo estado para esta cita:",
                "Cambiar Estado", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (seleccion != -1) {
            try {
                EstadoCita nuevoEstado = opciones[seleccion];
                controller.cambiarEstadoCita(uuidCitaSeleccionada, nuevoEstado);
                JOptionPane.showMessageDialog(this, "Estado actualizado a " + nuevoEstado.name() + ".");
                limpiarFormulario();
                cargarDatosEnTabla();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void eliminarCita() {
        if (uuidCitaSeleccionada == null) return;
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro que desea cancelar y eliminar esta cita?\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                controller.eliminarCita(uuidCitaSeleccionada);
                JOptionPane.showMessageDialog(this, "Cita eliminada correctamente.");
                limpiarFormulario();
                cargarDatosEnTabla();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cargarCitaSeleccionada() {
        int filaVisual = tablaCitas.getSelectedRow();
        int filaModelo = tablaCitas.convertRowIndexToModel(filaVisual);
        uuidCitaSeleccionada = (String) modeloTabla.getValueAt(filaModelo, 0);

        txtMotivo.setText((String) modeloTabla.getValueAt(filaModelo, 5));
        Object obs = modeloTabla.getValueAt(filaModelo, 7);
        txtObservaciones.setText(obs != null ? obs.toString() : "");

        btnModificar.setEnabled(true);
        btnCambiarEstado.setEnabled(true);
        btnEliminar.setEnabled(true);
        btnProgramar.setEnabled(false);
        btnBuscarPaciente.setEnabled(false);
        btnBuscarMedico.setEnabled(false);
    }

    private void limpiarFormulario() {
        txtInfoPaciente.setText("Ninguno seleccionado");
        idPacienteSeleccionado = null;
        txtInfoMedico.setText("Ninguno seleccionado");
        uuidMedicoSeleccionado = null;
        txtFecha.setText("");
        txtHora.setText("");
        txtMotivo.setText("");
        txtObservaciones.setText("");

        uuidCitaSeleccionada = null;
        btnModificar.setEnabled(false);
        btnCambiarEstado.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnProgramar.setEnabled(true);
        btnBuscarPaciente.setEnabled(true);
        btnBuscarMedico.setEnabled(true);
        tablaCitas.clearSelection();
    }

    private void mostrarInformacionPaciente(String idPaciente) {
        try {
            List<Paciente> coincidencias = pacienteController.buscarPacientes(idPaciente);
            if (!coincidencias.isEmpty()) {
                Paciente p = coincidencias.get(0);
                String mensaje = "👤 DATOS DEL PACIENTE\n\nIdentificación: " + p.getIdentificacion() +
                        "\nNombre: " + p.getNombres() + " " + p.getApellidos() + "\nNacimiento: " + p.getFechaNacimiento() +
                        "\nSexo: " + p.getSexo() + "\nTipo de Sangre: " + p.getTipoSangre() +
                        "\nTeléfono: " + p.getTelefono() + "\nCorreo: " + p.getCorreoElectronico();
                JOptionPane.showMessageDialog(this, mensaje, "Ficha del Paciente", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la información del paciente.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarInformacionMedico(String uuidMedico) {
        try {
            List<Medico> coincidencias = medicoController.buscarMedicos(uuidMedico);
            if (!coincidencias.isEmpty()) {
                Medico m = coincidencias.get(0);
                String estadoStr = m.isActivo() ? "🟢 ACTIVO" : "🔴 INACTIVO";
                String mensaje = "⚕️ DATOS DEL MÉDICO\n\nEspecialidad: " + m.getEspecialidad() +
                        "\nNombre: Dr. " + m.getNombres() + " " + m.getApellidos() +
                        "\nHorario de Atención: " + m.getHorarioInicio() + " a " + m.getHorarioFin() +
                        "\nTeléfono: " + m.getTelefono() + "\nEstado en Clínica: " + estadoStr;
                JOptionPane.showMessageDialog(this, mensaje, "Ficha del Médico", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la información del médico.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}