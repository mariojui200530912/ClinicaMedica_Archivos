package utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class FixedLengthStringUtil {
    // Escribe garantizando que ocupe el espacio especificado
    public static void writeFixedString(DataOutput output, String text, int length) throws IOException {
        String formattedText = text;

        if (formattedText == null) {
            formattedText = "";
        }

        if (formattedText.length() > length) {
            formattedText = formattedText.substring(0, length);
        } else {
            formattedText = String.format("%-" + length + "s", formattedText);
        }

        output.writeChars(formattedText);
    }

    // Lee la cadena de texto de longitud fija desde el archivo y elimina los espacios en blanco
    public static String readFixedString(DataInput input, int length) throws IOException {
        char[] chars = new char[length];

        for (int i = 0; i < length; i++) {
            chars[i] = input.readChar();
        }

        return new String(chars).trim();
    }
}
