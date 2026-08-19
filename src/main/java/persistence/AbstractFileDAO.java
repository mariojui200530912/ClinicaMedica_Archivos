package persistence;

public abstract class AbstractFileDAO {
    private final int[] longitudesCampos;
    protected final int recordSize;

    public AbstractFileDAO(int[] longitudesCampos, int bytesAdicionales) {
        this.longitudesCampos = longitudesCampos;

        int totalCaracteres = 0;
        for (int longitud : longitudesCampos) {
            totalCaracteres += longitud;
        }

        this.recordSize = (totalCaracteres * 2) + bytesAdicionales;
    }

    protected int calcularOffset(int indiceCampo) {
        int offset = 0;
        for (int i = 0; i < indiceCampo; i++) {
            offset += longitudesCampos[i] * 2;
        }
        return offset;
    }
}
