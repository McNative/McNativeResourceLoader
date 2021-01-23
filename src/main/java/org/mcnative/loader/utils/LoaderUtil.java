package org.mcnative.loader.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class LoaderUtil {

    public static String readAllText(File file) {
        return readAllText(file, Charset.defaultCharset());
    }

    public static String readAllText(File file, Charset charset) {
        try {
            return readAllText(Files.newInputStream(file.toPath()), charset);
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }

    public static String readAllText(InputStream stream, Charset charset) {
        if (!Charset.isSupported(charset.name())) {
            throw new UnsupportedOperationException("Charset " + charset.name() + " is not supported.");
        } else {
            try {
                byte[] content = new byte[stream.available()];
                stream.read(content);
                return new String(content, charset);
            } catch (IOException var3) {
                throw new RuntimeException(var3);
            }
        }
    }

}
