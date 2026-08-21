package utils;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;

public class ExportadorCSV {
    public static void exportarTabla(Component ventanaPadre, TableModel modeloTabla, String nombreArchivo) {
        // Validacion vacios
        if (modeloTabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(ventanaPadre, "No hay datos para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Archivo CSV");
        fileChooser.setSelectedFile(new File(nombreArchivo));

        if (fileChooser.showSaveDialog(ventanaPadre) == JFileChooser.APPROVE_OPTION) {
            try (PrintWriter writer = new PrintWriter(fileChooser.getSelectedFile())) {
                // Escribir Encabezados
                for (int i = 0; i < modeloTabla.getColumnCount(); i++) {
                    writer.print(modeloTabla.getColumnName(i));
                    if (i < modeloTabla.getColumnCount() - 1) writer.print(",");
                }
                writer.println();

                // Escribir Datos de las Filas
                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    for (int j = 0; j < modeloTabla.getColumnCount(); j++) {
                        String valor = modeloTabla.getValueAt(i, j) != null ? modeloTabla.getValueAt(i, j).toString() : "";

                        if (valor.contains(",") || valor.contains("\"")) {
                            valor = "\"" + valor.replace("\"", "\"\"") + "\"";
                        }

                        writer.print(valor);
                        if (j < modeloTabla.getColumnCount() - 1) writer.print(",");
                    }
                    writer.println();
                }
                JOptionPane.showMessageDialog(ventanaPadre, "Datos exportados exitosamente a CSV.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ventanaPadre, "Error al guardar el archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
