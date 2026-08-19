import views.MainFrame;
import views.MedicosPanel;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println("No se pudo aplicar el tema del sistema. Usando el tema por defecto.");
            }

            // Instanciamos y hacemos visible la ventana principal
            MainFrame ventana = new MainFrame();
            ventana.setVisible(true);
        });
    }
}
