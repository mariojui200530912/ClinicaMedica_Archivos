package utils;

public class UUIDGenerator {
    public static String generar() {
        long tiempoMillis = System.currentTimeMillis();
        long tiempoNano = System.nanoTime();

        long aleatorio = (long) (Math.random() * Long.MAX_VALUE);

        // Convertimos los números a representaciones hexadecimales (base 16)
        String hexMillis = Long.toHexString(tiempoMillis);
        String hexNano = Long.toHexString(tiempoNano);
        String hexAleatorio = Long.toHexString(aleatorio);

        String cadenaBase = hexMillis + hexNano + hexAleatorio;

        while (cadenaBase.length() < 32) {
            cadenaBase += "0";
        }

        String raw = cadenaBase.substring(cadenaBase.length() - 32);

        // Insertamos los guiones para lograr el formato de 36 caracteres (8-4-4-4-12)
        return raw.substring(0, 8) + "-" +
                raw.substring(8, 12) + "-" +
                raw.substring(12, 16) + "-" +
                raw.substring(16, 20) + "-" +
                raw.substring(20, 32);
    }
}
