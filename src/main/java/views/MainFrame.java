package views;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel panelCentral;

    // Paneles de los módulos
    private PacientesPanel panelPacientes;
    private MedicosPanel panelMedicos;
    private CitasPanel panelCitas;
    private ReportesPanel panelReportes;

    public MainFrame() {
        setTitle("Sistema de Gestión Clínica");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        panelPacientes = new PacientesPanel();
        panelMedicos = new MedicosPanel();
        panelCitas = new CitasPanel();
        panelReportes = new ReportesPanel();

        configurarMenuSuperior();

        cardLayout = new CardLayout();
        panelCentral = new JPanel(cardLayout);

        panelCentral.add(crearPanelBienvenida(), "BIENVENIDA");
        panelCentral.add(panelPacientes, "PACIENTES");
        panelCentral.add(panelMedicos, "MEDICOS");
        panelCentral.add(panelCitas, "CITAS");
        panelCentral.add(panelReportes, "REPORTES");

        add(panelCentral, BorderLayout.CENTER);
    }

    private void configurarMenuSuperior() {
        JMenuBar barraMenu = new JMenuBar();

        JMenu menuModulos = new JMenu("Módulos");

        JMenuItem itemPacientes = new JMenuItem("Pacientes");
        itemPacientes.addActionListener(e -> cardLayout.show(panelCentral, "PACIENTES"));

        JMenuItem itemMedicos = new JMenuItem("Médicos");
        itemMedicos.addActionListener(e -> cardLayout.show(panelCentral, "MEDICOS"));

        JMenuItem itemCitas = new JMenuItem("Citas y Agendas");
        itemCitas.addActionListener(e -> cardLayout.show(panelCentral, "CITAS"));

        menuModulos.add(itemPacientes);
        menuModulos.add(itemMedicos);
        menuModulos.addSeparator();
        menuModulos.add(itemCitas);

        JMenu menuAnalisis = new JMenu("Análisis");

        JMenuItem itemReportes = new JMenuItem("Generador de Reportes");
        itemReportes.addActionListener(e -> cardLayout.show(panelCentral, "REPORTES"));

        menuAnalisis.add(itemReportes);

        JMenu menuSistema = new JMenu("Sistema");

        JMenuItem itemInicio = new JMenuItem("Pantalla de Inicio");
        itemInicio.addActionListener(e -> cardLayout.show(panelCentral, "BIENVENIDA"));

        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> System.exit(0)); // Cierra el programa

        menuSistema.add(itemInicio);
        menuSistema.addSeparator();
        menuSistema.add(itemSalir);

        barraMenu.add(menuSistema);
        barraMenu.add(menuModulos);
        barraMenu.add(menuAnalisis);

        setJMenuBar(barraMenu);
    }

    private JPanel crearPanelBienvenida() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel lblBienvenida = new JLabel("Bienvenido al Sistema de Gestión Clínica", SwingConstants.CENTER);
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblBienvenida.setForeground(Color.GRAY);

        JLabel lblInstrucciones = new JLabel("Utilice el menú superior para navegar entre los módulos.", SwingConstants.CENTER);
        lblInstrucciones.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblInstrucciones.setForeground(Color.LIGHT_GRAY);

        JPanel panelCentro = new JPanel(new GridLayout(2, 1));
        panelCentro.setBackground(Color.WHITE);
        panelCentro.add(lblBienvenida);
        panelCentro.add(lblInstrucciones);

        panel.add(panelCentro, BorderLayout.CENTER);
        return panel;
    }
}
