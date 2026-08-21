package views;

import controllers.PacienteController;
import models.Paciente;

import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class PacientesPanel extends JPanel{
    private final PacienteController controller;
    private JTable tablaPacientes;
    private DefaultTableModel modeloTabla;

    // Componentes del formulario
    private JTextField txtIdentificacion, txtNombres, txtApellidos, txtFechaNacimiento, txtTelefono, txtCorreo;
    private JComboBox<String> comboSexo;
    private JComboBox<String> comboTipoSangre;
    private JButton btnRegistrar, btnModificar, btnEliminar, btnLimpiar;
    private JTextField txtBusqueda;
    private String identificacionSeleccionada = null;

    public PacientesPanel() {
        this.controller = new PacienteController();
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
        cargarDatosEnTabla();
    }

    private void inicializarComponentes() {
        // --- 1. Formulario Lateral (Oeste) ---
        JPanel panelFormulario = new JPanel(new GridLayout(10, 2, 5, 5));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Paciente"));

        panelFormulario.add(new JLabel("Identificación (*):"));
        txtIdentificacion = new JTextField();
        panelFormulario.add(txtIdentificacion);

        panelFormulario.add(new JLabel("Nombres (*):"));
        txtNombres = new JTextField();
        panelFormulario.add(txtNombres);

        panelFormulario.add(new JLabel("Apellidos (*):"));
        txtApellidos = new JTextField();
        panelFormulario.add(txtApellidos);

        panelFormulario.add(new JLabel("Fecha Nac. (yyyy-MM-dd):"));
        txtFechaNacimiento = new JTextField();
        panelFormulario.add(txtFechaNacimiento);

        panelFormulario.add(new JLabel("Sexo:"));
        String[] opcionesSexo = {"Masculino", "Femenino", "Otro"};
        comboSexo = new JComboBox<>(opcionesSexo);
        panelFormulario.add(comboSexo);

        panelFormulario.add(new JLabel("Teléfono:"));
        txtTelefono = new JTextField();
        panelFormulario.add(txtTelefono);

        panelFormulario.add(new JLabel("Correo (Opcional):"));
        txtCorreo = new JTextField();
        panelFormulario.add(txtCorreo);

        panelFormulario.add(new JLabel("Tipo Sangre:"));
        String[] opcionesSangre = {"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"};
        comboTipoSangre = new JComboBox<>(opcionesSangre);
        panelFormulario.add(comboTipoSangre);

        btnRegistrar = new JButton("Registrar");
        btnRegistrar.addActionListener(e -> registrarPaciente());
        panelFormulario.add(btnRegistrar);

        btnModificar = new JButton("Modificar");
        btnModificar.setEnabled(false);
        btnModificar.addActionListener(e -> modificarPaciente());
        panelFormulario.add(btnModificar);

        add(panelFormulario, BorderLayout.WEST);

        // --- 2. Tabla y Búsqueda (Centro) ---
        JPanel panelCentral = new JPanel(new BorderLayout());

        // Barra superior de búsqueda y acciones adicionales
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtBusqueda = new JTextField(20);

        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscarPacientes());

        btnEliminar = new JButton("Eliminar");
        btnEliminar.setEnabled(false);
        btnEliminar.addActionListener(e -> eliminarPaciente());

        btnLimpiar = new JButton("Limpiar Formulario");
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBusqueda.add(new JLabel("Buscar:"));
        panelBusqueda.add(txtBusqueda);
        panelBusqueda.add(btnBuscar);
        panelBusqueda.add(btnEliminar);
        panelBusqueda.add(btnLimpiar);

        panelCentral.add(panelBusqueda, BorderLayout.NORTH);

        String[] columnas = {"ID", "Nombres", "Apellidos", "Nacimiento", "Sexo", "Teléfono", "Sangre"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaPacientes = new JTable(modeloTabla);

        tablaPacientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaPacientes.getSelectedRow() != -1) {
                cargarPacienteSeleccionado();
            }
        });

        panelCentral.add(new JScrollPane(tablaPacientes), BorderLayout.CENTER);
        add(panelCentral, BorderLayout.CENTER);
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

            JOptionPane.showMessageDialog(this, "Paciente modificado exitosamente.");
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
                "¿Está seguro de que desea eliminar este paciente?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

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

    private void cargarDatosEnTabla() {
        try {
            List<Paciente> pacientes = controller.consultarTodosLosPacientes();
            actualizarTabla(pacientes);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos: " + e.getMessage());
        }
    }

    private void buscarPacientes() {
        try {
            String criterio = txtBusqueda.getText();
            List<Paciente> pacientes = controller.buscarPacientes(criterio);
            actualizarTabla(pacientes);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error en la búsqueda: " + e.getMessage());
        }
    }

    private void actualizarTabla(List<Paciente> pacientes) {
        modeloTabla.setRowCount(0);
        for (Paciente p : pacientes) {
            modeloTabla.addRow(new Object[]{
                    p.getIdentificacion(), p.getNombres(), p.getApellidos(),
                    p.getFechaNacimiento(), p.getSexo(), p.getTelefono(), p.getTipoSangre()
            });
        }
    }

    private void cargarPacienteSeleccionado() {
        int fila = tablaPacientes.getSelectedRow();
        identificacionSeleccionada = (String) modeloTabla.getValueAt(fila, 0);

        txtIdentificacion.setText(identificacionSeleccionada);
        txtIdentificacion.setEditable(false); // No permitimos editar la clave primaria

        txtNombres.setText((String) modeloTabla.getValueAt(fila, 1));
        txtApellidos.setText((String) modeloTabla.getValueAt(fila, 2));
        txtFechaNacimiento.setText(modeloTabla.getValueAt(fila, 3).toString());
        comboSexo.setSelectedItem((String) modeloTabla.getValueAt(fila, 4));
        txtTelefono.setText((String) modeloTabla.getValueAt(fila, 5));
        comboTipoSangre.setSelectedItem((String) modeloTabla.getValueAt(fila, 6));

        txtCorreo.setText("");

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
