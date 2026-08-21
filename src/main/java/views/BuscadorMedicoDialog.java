package views;

import controllers.MedicoController;
import models.Medico;

import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BuscadorMedicoDialog extends JDialog {
    private MedicoController controller;
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JTextField txtBusqueda;
    private Medico medicoSeleccionado = null;

    public BuscadorMedicoDialog(Window owner) {
        super(owner, "Buscar Médico", ModalityType.APPLICATION_MODAL);
        setSize(600, 400);
        setLocationRelativeTo(owner);
        controller = new MedicoController();

        inicializarComponentes();
        cargarDatos("");
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtBusqueda = new JTextField(20);
        JButton btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> cargarDatos(txtBusqueda.getText()));

        panelBusqueda.add(new JLabel("Nombre o Especialidad:"));
        panelBusqueda.add(txtBusqueda);
        panelBusqueda.add(btnBuscar);
        add(panelBusqueda, BorderLayout.NORTH);

        String[] columnas = {"UUID", "Nombres", "Apellidos", "Especialidad"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modeloTabla);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelInferior = new JPanel();
        JButton btnSeleccionar = new JButton("Seleccionar Médico");
        btnSeleccionar.addActionListener(e -> seleccionarYSalir());
        panelInferior.add(btnSeleccionar);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void cargarDatos(String criterio) {
        try {
            List<Medico> medicos = criterio.isEmpty() ?
                    controller.consultarTodosLosMedicos() : controller.buscarMedicos(criterio);

            modeloTabla.setRowCount(0);
            for (Medico m : medicos) {
                if (m.isActivo()) { // Solo mostramos médicos activos para citas
                    modeloTabla.addRow(new Object[]{m.getUuid(), m.getNombres(), m.getApellidos(), m.getEspecialidad()});
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar: " + e.getMessage());
        }
    }

    private void seleccionarYSalir() {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un médico de la lista.");
            return;
        }

        String uuid = (String) modeloTabla.getValueAt(fila, 0);
        String nombres = (String) modeloTabla.getValueAt(fila, 1);
        String apellidos = (String) modeloTabla.getValueAt(fila, 2);

        medicoSeleccionado = new Medico(nombres, apellidos, "", "", "", null, null);
        medicoSeleccionado.setUuid(uuid); // Asignamos el UUID para poder usarlo
        dispose();
    }

    public Medico getMedicoSeleccionado() {
        return medicoSeleccionado;
    }
}
