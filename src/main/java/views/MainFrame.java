package views;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Sistema de Gestión - Clínica Médica");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        inicializarComponentes();
    }

    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());

        JLabel lblTitulo = new JLabel("Administración de Clínica Médica", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Módulo de Médicos", new MedicosPanel());

        tabbedPane.addTab("Módulo de Citas", new JPanel());
        tabbedPane.addTab("Módulo de Pacientes", new JPanel());
        tabbedPane.addTab("Módulo de Reportes", new JPanel());

        panelPrincipal.add(tabbedPane, BorderLayout.CENTER);

        add(panelPrincipal);
    }
}
