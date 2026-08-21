package persistence;

import java.io.File;
import java.io.IOException;

public abstract class AbstractFileDAO {
    private final int[] longitudesCampos;
    protected final int recordSize;

    public AbstractFileDAO(int[] longitudesCampos, int bytesAdicionales, String filePath) {
        this.longitudesCampos = longitudesCampos;

        int totalCaracteres = 0;
        for (int longitud : longitudesCampos) {
            totalCaracteres += longitud;
        }

        this.recordSize = (totalCaracteres * 2) + bytesAdicionales;

        File file = new File(filePath);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Error crítico al inicializar el archivo " + filePath + ": " + e.getMessage());
        }
    }

    protected int calcularOffset(int indiceCampo) {
        int offset = 0;
        for (int i = 0; i < indiceCampo; i++) {
            offset += longitudesCampos[i] * 2;
        }
        return offset;
    }
}
