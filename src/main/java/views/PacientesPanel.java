package views;

import controllers.PacienteController;
import models.Paciente;

import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class PacientesPanel extends JPanel{
    private final PacienteController controller;
    private JTable tablaPacientes;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> comparadorFiltro;

    // Componentes del formulario
    private JTextField txtIdentificacion, txtNombres, txtApellidos, txtFechaNacimiento, txtTelefono, txtCorreo;
    private JComboBox<String> comboSexo;
    private JComboBox<String> comboTipoSangre;

    // Componentes de busqueda
    private JTextField txtBusqueda;
    private JButton btnBuscar, btnRestablecer;

    private JButton btnRegistrar, btnModificar, btnEliminar, btnLimpiar;

    private String identificacionSeleccionada = null;

    public PacientesPanel() {
        this.controller = new PacienteController();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inicializarComponentes();
        cargarDatosEnTabla();
    }

    private void inicializarComponentes() {

        JPanel panelCentral = new JPanel(new BorderLayout(0, 10));

        String[] columnas = {"ID / Cédula", "Nombres", "Apellidos", "Nacimiento", "Sexo", "Teléfono", "Correo", "Sangre"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // Solo permitimos sombrear/copiar el ID
            }
        };
        tablaPacientes = new JTable(modeloTabla);
        comparadorFiltro = new TableRowSorter<>(modeloTabla);
        tablaPacientes.setRowSorter(comparadorFiltro);

        JPanel panelAccionesTabla = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAccionesTabla.add(new JLabel("Buscar (ID, Nombre, Apellido):"));

        txtBusqueda = new JTextField(20);
        btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscarPacientes());

        btnRestablecer = new JButton("Mostrar Todos");
        btnRestablecer.addActionListener(e -> {
            txtBusqueda.setText("");
            cargarDatosEnTabla();
        });

        panelAccionesTabla.add(txtBusqueda);
        panelAccionesTabla.add(btnBuscar);
        panelAccionesTabla.add(btnRestablecer);

        panelCentral.add(panelAccionesTabla, BorderLayout.NORTH);

        JTextField campoCopiable = new JTextField();
        campoCopiable.setEditable(false);
        DefaultCellEditor editorSoloLectura = new DefaultCellEditor(campoCopiable);
        tablaPacientes.getColumnModel().getColumn(0).setCellEditor(editorSoloLectura);

        // --- Menu click derecho
        JPopupMenu menuContextual = new JPopupMenu();
        JMenuItem itemCopiarPaciente = new JMenuItem("📋 Copiar ID del Paciente");
        itemCopiarPaciente.addActionListener(e -> {
            int fila = tablaPacientes.getSelectedRow();
            if(fila != -1) {
                String id = (String) modeloTabla.getValueAt(tablaPacientes.convertRowIndexToModel(fila), 0);
                java.awt.datatransfer.StringSelection sel = new java.awt.datatransfer.StringSelection(id);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
            }
        });
        menuContextual.add(itemCopiarPaciente);
        tablaPacientes.setComponentPopupMenu(menuContextual);

        tablaPacientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaPacientes.getSelectedRow() != -1) {
                cargarPacienteSeleccionado();
            }
        });

        panelCentral.add(new JScrollPane(tablaPacientes), BorderLayout.CENTER);
        add(panelCentral, BorderLayout.CENTER);

        // Formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Gestión de Pacientes"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Identificación (*):"), gbc);
        txtIdentificacion = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.35;
        panelFormulario.add(txtIdentificacion, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Nombres (*):"), gbc);
        txtNombres = new JTextField();
        gbc.gridx = 3; gbc.weightx = 0.35;
        panelFormulario.add(txtNombres, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Apellidos (*):"), gbc);
        txtApellidos = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.35;
        panelFormulario.add(txtApellidos, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Fecha Nac. (yyyy-MM-dd):"), gbc);
        txtFechaNacimiento = new JTextField();
        gbc.gridx = 3; gbc.weightx = 0.35;
        panelFormulario.add(txtFechaNacimiento, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Sexo:"), gbc);
        String[] opcionesSexo = {"Masculino", "Femenino", "Otro"};
        comboSexo = new JComboBox<>(opcionesSexo);
        gbc.gridx = 1; gbc.weightx = 0.35;
        panelFormulario.add(comboSexo, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Teléfono:"), gbc);
        txtTelefono = new JTextField();
        gbc.gridx = 3; gbc.weightx = 0.35;
        panelFormulario.add(txtTelefono, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Correo (Opcional):"), gbc);
        txtCorreo = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.35;
        panelFormulario.add(txtCorreo, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Tipo Sangre:"), gbc);
        String[] opcionesSangre = {"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"};
        comboTipoSangre = new JComboBox<>(opcionesSangre);
        gbc.gridx = 3; gbc.weightx = 0.35;
        panelFormulario.add(comboTipoSangre, gbc);

        JPanel panelBotonesForm = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

        btnRegistrar = new JButton("Registrar Paciente");
        btnRegistrar.addActionListener(e -> registrarPaciente());

        btnModificar = new JButton("Guardar Cambios");
        btnModificar.setEnabled(false);
        btnModificar.addActionListener(e -> modificarPaciente());

        btnEliminar = new JButton("Eliminar Paciente");
        btnEliminar.setEnabled(false);
        btnEliminar.addActionListener(e -> eliminarPaciente());

        btnLimpiar = new JButton("Limpiar / Cancelar");
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBotonesForm.add(btnRegistrar);
        panelBotonesForm.add(btnModificar);
        panelBotonesForm.add(btnEliminar);
        panelBotonesForm.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        gbc.insets = new Insets(15, 5, 5, 5);
        panelFormulario.add(panelBotonesForm, gbc);

        add(panelFormulario, BorderLayout.SOUTH);
    }

    private void poblarTabla(List<Paciente> pacientes) {
        modeloTabla.setRowCount(0);
        for (Paciente p : pacientes) {
            modeloTabla.addRow(new Object[]{
                    p.getIdentificacion(), p.getNombres(), p.getApellidos(),
                    p.getFechaNacimiento(), p.getSexo(), p.getTelefono(),
                    p.getCorreoElectronico(), p.getTipoSangre()
            });
        }
    }

    private void cargarDatosEnTabla() {
        try {
            List<Paciente> pacientes = controller.consultarTodosLosPacientes();
            poblarTabla(pacientes);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos: " + e.getMessage());
        }
    }

    private void buscarPacientes() {
        String criterio = txtBusqueda.getText().trim();
        if (criterio.isEmpty()) {
            cargarDatosEnTabla(); // Si está vacío, carga todos
            return;
        }

        try {
            List<Paciente> pacientes = controller.buscarPacientes(criterio);
            poblarTabla(pacientes);

            if (pacientes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron pacientes con ese criterio.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error en la búsqueda: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarPaciente() {
        try {
            LocalDate fechaNac = LocalDate.parse(txtFechaNacimiento.getText());

            controller.registrarPaciente(
                    txtIdentificacion.getText(), txtNombres.getText(), txtApellidos.getText(),
                    fechaNac,
                    (String) comboSexo.getSelectedItem(),
                    txtTelefono.getText(), txtCorreo.getText(),
                    (String) comboTipoSangre.getSelectedItem()
            );

            JOptionPane.showMessageDialog(this, "Paciente registrado exitosamente.");
            limpiarFormulario();
            cargarDatosEnTabla();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use yyyy-MM-dd", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificarPaciente() {
        if (identificacionSeleccionada == null) return;

        try {
            LocalDate fechaNac = LocalDate.parse(txtFechaNacimiento.getText());

            controller.modificarPaciente(
                    identificacionSeleccionada, txtNombres.getText(), txtApellidos.getText(),
                    fechaNac, (String) comboSexo.getSelectedItem(), txtTelefono.getText(),
                    txtCorreo.getText(), (String) comboTipoSangre.getSelectedItem()
            );

            JOptionPane.showMessageDialog(this, "Datos del paciente modificados exitosamente.");
            limpiarFormulario();
            cargarDatosEnTabla();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use yyyy-MM-dd", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarPaciente() {
        if (identificacionSeleccionada == null) return;

        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar este paciente de los registros?\nEsta acción es irreversible.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                controller.eliminarPaciente(identificacionSeleccionada);
                JOptionPane.showMessageDialog(this, "Paciente eliminado exitosamente.");
                limpiarFormulario();
                cargarDatosEnTabla();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cargarPacienteSeleccionado() {
        int filaVisual = tablaPacientes.getSelectedRow();
        int filaModelo = tablaPacientes.convertRowIndexToModel(filaVisual);

        identificacionSeleccionada = (String) modeloTabla.getValueAt(filaModelo, 0);

        txtIdentificacion.setText(identificacionSeleccionada);
        txtIdentificacion.setEditable(false); // Evita cambiar la llave primaria

        txtNombres.setText((String) modeloTabla.getValueAt(filaModelo, 1));
        txtApellidos.setText((String) modeloTabla.getValueAt(filaModelo, 2));
        txtFechaNacimiento.setText(modeloTabla.getValueAt(filaModelo, 3).toString());
        comboSexo.setSelectedItem((String) modeloTabla.getValueAt(filaModelo, 4));
        txtTelefono.setText((String) modeloTabla.getValueAt(filaModelo, 5));

        Object correoObj = modeloTabla.getValueAt(filaModelo, 6);
        txtCorreo.setText(correoObj != null ? correoObj.toString() : "");

        comboTipoSangre.setSelectedItem((String) modeloTabla.getValueAt(filaModelo, 7));

        btnModificar.setEnabled(true);
        btnEliminar.setEnabled(true);
        btnRegistrar.setEnabled(false);
    }

    private void limpiarFormulario() {
        txtIdentificacion.setText("");
        txtIdentificacion.setEditable(true);
        txtNombres.setText("");
        txtApellidos.setText("");
        txtFechaNacimiento.setText("");
        comboSexo.setSelectedIndex(0);
        txtTelefono.setText("");
        txtCorreo.setText("");
        comboTipoSangre.setSelectedIndex(0);

        identificacionSeleccionada = null;
        btnModificar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnRegistrar.setEnabled(true);
        tablaPacientes.clearSelection();
    }
}
