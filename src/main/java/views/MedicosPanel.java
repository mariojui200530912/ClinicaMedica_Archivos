package views;

import controllers.MedicoController;
import models.Medico;

import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class MedicosPanel extends JPanel {
    private final MedicoController controller;
    private JTable tablaMedicos;
    private DefaultTableModel modeloTabla;

    private JTextField txtNombres, txtApellidos, txtEspecialidad, txtTelefono, txtCorreo, txtHorarioInicio, txtHorarioFin;
    private JButton btnRegistrar, btnModificar, btnCambiarEstado, btnLimpiar;
    private JTextField txtBusqueda;
    private String uuidSeleccionado = null;
    private JComboBox<String> comboFiltroEstado;

    public MedicosPanel() {
        this.controller = new MedicoController();
        setLayout(new BorderLayout(10, 10));

        inicializarComponentes();
        cargarDatosEnTabla();
    }

    private void inicializarComponentes() {
        JPanel panelFormulario = new JPanel(new GridLayout(9, 2, 5, 5));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Médico"));

        panelFormulario.add(new JLabel("Nombres (*):"));
        txtNombres = new JTextField();
        panelFormulario.add(txtNombres);

        panelFormulario.add(new JLabel("Apellidos (*):"));
        txtApellidos = new JTextField();
        panelFormulario.add(txtApellidos);

        panelFormulario.add(new JLabel("Especialidad (*):"));
        txtEspecialidad = new JTextField();
        panelFormulario.add(txtEspecialidad);

        panelFormulario.add(new JLabel("Teléfono:"));
        txtTelefono = new JTextField();
        panelFormulario.add(txtTelefono);

        panelFormulario.add(new JLabel("Correo:"));
        txtCorreo = new JTextField();
        panelFormulario.add(txtCorreo);

        panelFormulario.add(new JLabel("Inicio (HH:mm):"));
        txtHorarioInicio = new JTextField();
        panelFormulario.add(txtHorarioInicio);

        panelFormulario.add(new JLabel("Fin (HH:mm):"));
        txtHorarioFin = new JTextField();
        panelFormulario.add(txtHorarioFin);

        btnRegistrar = new JButton("Registrar");
        btnRegistrar.addActionListener(e -> registrarMedico());
        panelFormulario.add(btnRegistrar);

        btnModificar = new JButton("Modificar");
        btnModificar.setEnabled(false); // Se habilita al seleccionar una fila
        btnModificar.addActionListener(e -> modificarMedico());
        panelFormulario.add(btnModificar);

        add(panelFormulario, BorderLayout.WEST);

        JPanel panelCentral = new JPanel(new BorderLayout());

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtBusqueda = new JTextField(20);

        String[] opcionesEstado = {"Todos los Estados", "Solo Activos", "Solo Inactivos"};
        comboFiltroEstado = new JComboBox<>(opcionesEstado);

        JButton btnBuscar = new JButton("Buscar / Filtrar");
        btnBuscar.addActionListener(e -> buscarYFiltrarMedicos());

        btnCambiarEstado = new JButton("Activar / Desactivar");
        btnCambiarEstado.setEnabled(false);
        btnCambiarEstado.addActionListener(e -> cambiarEstado());

        btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBusqueda.add(new JLabel("Buscar (UUID/Nombre/Especialidad):"));
        panelBusqueda.add(txtBusqueda);
        panelBusqueda.add(new JLabel("Estado:"));
        panelBusqueda.add(comboFiltroEstado);
        panelBusqueda.add(btnBuscar);
        panelBusqueda.add(btnCambiarEstado);
        panelBusqueda.add(btnLimpiar);

        panelCentral.add(panelBusqueda, BorderLayout.NORTH);

        String[] columnas = {"UUID", "Nombres", "Apellidos", "Especialidad", "Horario", "Estado", "Teléfono", "Correo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaMedicos = new JTable(modeloTabla);

        tablaMedicos.getColumnModel().getColumn(6).setMinWidth(0);
        tablaMedicos.getColumnModel().getColumn(6).setMaxWidth(0);
        tablaMedicos.getColumnModel().getColumn(6).setPreferredWidth(0);

        tablaMedicos.getColumnModel().getColumn(7).setMinWidth(0);
        tablaMedicos.getColumnModel().getColumn(7).setMaxWidth(0);
        tablaMedicos.getColumnModel().getColumn(7).setPreferredWidth(0);

        tablaMedicos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaMedicos.getSelectedRow() != -1) {
                cargarMedicoSeleccionado();
            }
        });

        panelCentral.add(new JScrollPane(tablaMedicos), BorderLayout.CENTER);
        add(panelCentral, BorderLayout.CENTER);
    }


    private void registrarMedico() {
        try {
            LocalTime inicio = LocalTime.parse(txtHorarioInicio.getText());
            LocalTime fin = LocalTime.parse(txtHorarioFin.getText());

            controller.registrarMedico(
                    txtNombres.getText(), txtApellidos.getText(), txtEspecialidad.getText(),
                    txtTelefono.getText(), txtCorreo.getText(), inicio, fin
            );

            JOptionPane.showMessageDialog(this, "Médico registrado exitosamente.");
            limpiarFormulario();
            cargarDatosEnTabla();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de hora inválido. Use HH:mm", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatosEnTabla() {
        try {
            List<Medico> medicos = controller.consultarTodosLosMedicos();
            actualizarTabla(medicos);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos: " + e.getMessage());
        }
    }

    private void buscarMedicos() {
        try {
            String criterio = txtBusqueda.getText();
            List<Medico> medicos = controller.buscarMedicos(criterio);
            actualizarTabla(medicos);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error en la búsqueda: " + e.getMessage());
        }
    }

    private void actualizarTabla(List<Medico> medicos) {
        modeloTabla.setRowCount(0);
        for (Medico m : medicos) {
            modeloTabla.addRow(new Object[]{
                    m.getUuid(),
                    m.getNombres(),
                    m.getApellidos(),
                    m.getEspecialidad(),
                    m.getHorarioInicio() + " - " + m.getHorarioFin(),
                    m.isActivo() ? "Activo" : "Inactivo",
                    m.getTelefono(),
                    m.getCorreoElectronico()
            });
        }
    }

    private void cargarMedicoSeleccionado() {
        int fila = tablaMedicos.getSelectedRow();
        uuidSeleccionado = (String) modeloTabla.getValueAt(fila, 0);

        txtNombres.setText((String) modeloTabla.getValueAt(fila, 1));
        txtApellidos.setText((String) modeloTabla.getValueAt(fila, 2));
        txtEspecialidad.setText((String) modeloTabla.getValueAt(fila, 3));

        String horarioCompleto = (String) modeloTabla.getValueAt(fila, 4);
        String[] horas = horarioCompleto.split(" - ");
        if (horas.length == 2) {
            txtHorarioInicio.setText(horas[0]);
            txtHorarioFin.setText(horas[1]);
        }

        txtTelefono.setText((String) modeloTabla.getValueAt(fila, 6));
        txtCorreo.setText((String) modeloTabla.getValueAt(fila, 7));

        btnModificar.setEnabled(true);
        btnCambiarEstado.setEnabled(true);
        btnRegistrar.setEnabled(false);
    }

    private void modificarMedico() {
        if (uuidSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un médico de la tabla para modificarlo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalTime inicio = LocalTime.parse(txtHorarioInicio.getText());
            LocalTime fin = LocalTime.parse(txtHorarioFin.getText());

            // Llamada al controlador para procesar la modificación de la información[cite: 1]
            controller.modificarMedico(
                    uuidSeleccionado, txtNombres.getText(), txtApellidos.getText(),
                    txtEspecialidad.getText(), txtTelefono.getText(),
                    txtCorreo.getText(), inicio, fin
            );

            JOptionPane.showMessageDialog(this, "Médico modificado exitosamente.");
            limpiarFormulario();
            cargarDatosEnTabla();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de hora inválido. Use HH:mm", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cambiarEstado() {
        if (uuidSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un médico de la tabla.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int fila = tablaMedicos.getSelectedRow();
            String estadoStr = (String) modeloTabla.getValueAt(fila, 5);


            boolean nuevoEstado = estadoStr.equals("Inactivo");

            controller.cambiarEstadoMedico(uuidSeleccionado, nuevoEstado);

            String mensaje = nuevoEstado ? "Médico activado exitosamente." : "Médico desactivado exitosamente.";
            JOptionPane.showMessageDialog(this, mensaje);

            limpiarFormulario();
            cargarDatosEnTabla();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cambiar el estado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarYFiltrarMedicos() {
        try {
            String criterio = txtBusqueda.getText().trim();
            int filtroEstado = comboFiltroEstado.getSelectedIndex();

            List<Medico> resultados;

            if (criterio.isEmpty()) {
                resultados = controller.consultarTodosLosMedicos();
            } else {
                resultados = controller.buscarMedicos(criterio);
            }

            if (filtroEstado == 1) { // Solo Activos
                resultados = resultados.stream().filter(Medico::isActivo).collect(java.util.stream.Collectors.toList());
            } else if (filtroEstado == 2) { // Solo Inactivos
                resultados = resultados.stream().filter(m -> !m.isActivo()).collect(java.util.stream.Collectors.toList());
            }

            actualizarTabla(resultados);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error en la búsqueda: " + e.getMessage());
        }
    }

    private void limpiarFormulario() {
        txtNombres.setText("");
        txtApellidos.setText("");
        txtEspecialidad.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtHorarioInicio.setText("");
        txtHorarioFin.setText("");

        uuidSeleccionado = null;

        btnModificar.setEnabled(false);
        btnCambiarEstado.setEnabled(false);
        btnRegistrar.setEnabled(true);

        tablaMedicos.clearSelection();
    }
}


