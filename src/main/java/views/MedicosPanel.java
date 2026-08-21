package views;

import controllers.MedicoController;
import models.Medico;

import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class MedicosPanel extends JPanel {
    private final MedicoController controller;
    private JTable tablaMedicos;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> comparadorFiltro;

    // Componentes formulario
    private JTextField txtNombres, txtApellidos, txtEspecialidad, txtTelefono, txtCorreo, txtHorarioInicio, txtHorarioFin;

    // Componente de busqueda
    private JTextField txtBusqueda;
    private JComboBox<String> comboFiltroEstado;
    private JButton btnBuscar, btnRestablecer;

    // Botones
    private JButton btnRegistrar, btnModificar, btnCambiarEstado, btnLimpiar;

    private String uuidSeleccionado = null;

    public MedicosPanel() {
        this.controller = new MedicoController();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen general

        inicializarComponentes();
        cargarDatosEnTabla();
    }

    private void inicializarComponentes() {
        // Panel
        JPanel panelCentral = new JPanel(new BorderLayout(0, 10));

        String[] columnas = {"UUID", "Nombres", "Apellidos", "Especialidad", "Horario", "Estado", "Teléfono", "Correo"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // Solo permitimos sombrear/copiar el UUID
            }
        };
        tablaMedicos = new JTable(modeloTabla);
        comparadorFiltro = new TableRowSorter<>(modeloTabla);
        tablaMedicos.setRowSorter(comparadorFiltro);

        JPanel panelAccionesTabla = new JPanel(new FlowLayout(FlowLayout.LEFT));

        txtBusqueda = new JTextField(20);
        String[] opcionesEstado = {"Todos los Estados", "Solo Activos", "Solo Inactivos"};
        comboFiltroEstado = new JComboBox<>(opcionesEstado);

        btnBuscar = new JButton("Buscar / Filtrar");
        btnBuscar.addActionListener(e -> buscarYFiltrarMedicos());

        btnRestablecer = new JButton("Mostrar Todos");
        btnRestablecer.addActionListener(e -> {
            txtBusqueda.setText("");
            comboFiltroEstado.setSelectedIndex(0);
            cargarDatosEnTabla();
        });

        panelAccionesTabla.add(new JLabel("Buscar (UUID/Nombre/Especialidad):"));
        panelAccionesTabla.add(txtBusqueda);
        panelAccionesTabla.add(new JLabel("Estado:"));
        panelAccionesTabla.add(comboFiltroEstado);
        panelAccionesTabla.add(btnBuscar);
        panelAccionesTabla.add(btnRestablecer);

        panelCentral.add(panelAccionesTabla, BorderLayout.NORTH);

        JTextField campoCopiable = new JTextField();
        campoCopiable.setEditable(false);
        DefaultCellEditor editorSoloLectura = new DefaultCellEditor(campoCopiable);
        tablaMedicos.getColumnModel().getColumn(0).setCellEditor(editorSoloLectura);

        // Metodo click derecho
        JPopupMenu menuContextual = new JPopupMenu();
        JMenuItem itemCopiarMedico = new JMenuItem("📋 Copiar UUID del Médico");
        itemCopiarMedico.addActionListener(e -> {
            int fila = tablaMedicos.getSelectedRow();
            if(fila != -1) {
                String id = (String) modeloTabla.getValueAt(tablaMedicos.convertRowIndexToModel(fila), 0);
                java.awt.datatransfer.StringSelection sel = new java.awt.datatransfer.StringSelection(id);
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
            }
        });
        menuContextual.add(itemCopiarMedico);
        tablaMedicos.setComponentPopupMenu(menuContextual);

        tablaMedicos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaMedicos.getSelectedRow() != -1) {
                cargarMedicoSeleccionado();
            }
        });

        panelCentral.add(new JScrollPane(tablaMedicos), BorderLayout.CENTER);
        add(panelCentral, BorderLayout.CENTER);

        // Formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Gestión de Médicos"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Nombres (*):"), gbc);
        txtNombres = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.35;
        panelFormulario.add(txtNombres, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Apellidos (*):"), gbc);
        txtApellidos = new JTextField();
        gbc.gridx = 3; gbc.weightx = 0.35;
        panelFormulario.add(txtApellidos, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Especialidad (*):"), gbc);
        txtEspecialidad = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.35;
        panelFormulario.add(txtEspecialidad, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Teléfono:"), gbc);
        txtTelefono = new JTextField();
        gbc.gridx = 3; gbc.weightx = 0.35;
        panelFormulario.add(txtTelefono, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Inicio (HH:mm):"), gbc);
        txtHorarioInicio = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.35;
        panelFormulario.add(txtHorarioInicio, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Fin (HH:mm):"), gbc);
        txtHorarioFin = new JTextField();
        gbc.gridx = 3; gbc.weightx = 0.35;
        panelFormulario.add(txtHorarioFin, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel("Correo:"), gbc);
        txtCorreo = new JTextField();
        gbc.gridx = 1; gbc.weightx = 0.35;
        panelFormulario.add(txtCorreo, gbc);

        gbc.gridx = 2; gbc.weightx = 0.15;
        panelFormulario.add(new JLabel(""), gbc);

        JPanel panelBotonesForm = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

        btnRegistrar = new JButton("Registrar Médico");
        btnRegistrar.addActionListener(e -> registrarMedico());

        btnModificar = new JButton("Guardar Cambios");
        btnModificar.setEnabled(false);
        btnModificar.addActionListener(e -> modificarMedico());

        btnCambiarEstado = new JButton("Activar / Desactivar");
        btnCambiarEstado.setEnabled(false);
        btnCambiarEstado.addActionListener(e -> cambiarEstado());

        btnLimpiar = new JButton("Limpiar / Cancelar");
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        panelBotonesForm.add(btnRegistrar);
        panelBotonesForm.add(btnModificar);
        panelBotonesForm.add(btnCambiarEstado);
        panelBotonesForm.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        gbc.insets = new Insets(15, 5, 5, 5);
        panelFormulario.add(panelBotonesForm, gbc);

        add(panelFormulario, BorderLayout.SOUTH);
    }


    private void poblarTabla(List<Medico> medicos) {
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

    private void cargarDatosEnTabla() {
        try {
            List<Medico> medicos = controller.consultarTodosLosMedicos();
            poblarTabla(medicos);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los datos: " + e.getMessage());
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

            poblarTabla(resultados);

            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron médicos con esos criterios.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error en la búsqueda: " + e.getMessage());
        }
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

    private void modificarMedico() {
        if (uuidSeleccionado == null) return;

        try {
            LocalTime inicio = LocalTime.parse(txtHorarioInicio.getText());
            LocalTime fin = LocalTime.parse(txtHorarioFin.getText());

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
        if (uuidSeleccionado == null) return;

        try {
            int filaVisual = tablaMedicos.getSelectedRow();
            int filaModelo = tablaMedicos.convertRowIndexToModel(filaVisual);
            String estadoStr = (String) modeloTabla.getValueAt(filaModelo, 5);

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

    private void cargarMedicoSeleccionado() {
        int filaVisual = tablaMedicos.getSelectedRow();
        int filaModelo = tablaMedicos.convertRowIndexToModel(filaVisual);

        uuidSeleccionado = (String) modeloTabla.getValueAt(filaModelo, 0);

        txtNombres.setText((String) modeloTabla.getValueAt(filaModelo, 1));
        txtApellidos.setText((String) modeloTabla.getValueAt(filaModelo, 2));
        txtEspecialidad.setText((String) modeloTabla.getValueAt(filaModelo, 3));

        String horarioCompleto = (String) modeloTabla.getValueAt(filaModelo, 4);
        String[] horas = horarioCompleto.split(" - ");
        if (horas.length == 2) {
            txtHorarioInicio.setText(horas[0]);
            txtHorarioFin.setText(horas[1]);
        }

        txtTelefono.setText((String) modeloTabla.getValueAt(filaModelo, 6));
        txtCorreo.setText((String) modeloTabla.getValueAt(filaModelo, 7));

        btnModificar.setEnabled(true);
        btnCambiarEstado.setEnabled(true);
        btnRegistrar.setEnabled(false);
    }

    private void limpiarFormulario() {
        txtNombres.setText("");
        txtApellidos.setText("");
        txtEspecialidad.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtHorarioInicio.setText("");
        txtHorarioFin.setText("");
        txtBusqueda.setText("");
        comboFiltroEstado.setSelectedIndex(0);

        uuidSeleccionado = null;

        btnModificar.setEnabled(false);
        btnCambiarEstado.setEnabled(false);
        btnRegistrar.setEnabled(true);

        tablaMedicos.clearSelection();
    }
}


