package views;

import controllers.PacienteController;
import models.Paciente;

import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BuscadorPacienteDialog extends JDialog {
    private PacienteController controller;
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JTextField txtBusqueda;
    private Paciente pacienteSeleccionado = null;

    public BuscadorPacienteDialog(Window owner) {
        super(owner, "Buscar Paciente", ModalityType.APPLICATION_MODAL);
        setSize(600, 400);
        setLocationRelativeTo(owner);
        controller = new PacienteController();

        inicializarComponentes();
        cargarDatos("");
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));

        // Panel de búsqueda superior
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtBusqueda = new JTextField(20);
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> cargarDatos(txtBusqueda.getText()));

        panelBusqueda.add(new JLabel("Nombre o ID:"));
        panelBusqueda.add(txtBusqueda);
        panelBusqueda.add(btnBuscar);
        add(panelBusqueda, BorderLayout.NORTH);

        // Tabla central
        String[] columnas = {"ID", "Nombres", "Apellidos"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modeloTabla);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel();
        JButton btnSeleccionar = new JButton("Seleccionar Paciente");
        btnSeleccionar.addActionListener(e -> seleccionarYSalir());
        panelInferior.add(btnSeleccionar);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void cargarDatos(String criterio) {
        try {
            List<Paciente> pacientes = criterio.isEmpty() ?
                    controller.consultarTodosLosPacientes() : controller.buscarPacientes(criterio);

            modeloTabla.setRowCount(0);
            for (Paciente p : pacientes) {
                modeloTabla.addRow(new Object[]{p.getIdentificacion(), p.getNombres(), p.getApellidos()});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar: " + e.getMessage());
        }
    }

    private void seleccionarYSalir() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un paciente de la lista.");
            return;
        }

        String id = (String) modeloTabla.getValueAt(fila, 0);
        String nombres = (String) modeloTabla.getValueAt(fila, 1);
        String apellidos = (String) modeloTabla.getValueAt(fila, 2);

        pacienteSeleccionado = new Paciente(id, nombres, apellidos, null, "", "", "", "");
        dispose();
    }

    public Paciente getPacienteSeleccionado() {
        return pacienteSeleccionado;
    }
}
