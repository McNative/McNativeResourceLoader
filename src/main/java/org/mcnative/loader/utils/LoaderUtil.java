package org.mcnative.loader.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.function.Predicate;

public class LoaderUtil {

    public static String readAllText(File file) {
        return readAllText(file, StandardCharsets.UTF_8);
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

    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (Exception var3) {
            throw new RuntimeException("Field " + fieldName + " in class " + clazz + " not found.", var3);
        }
    }

    public static Object getFieldValue(Class<?> clazz, String fieldName) {
        return getFieldValue(clazz, fieldName, Object.class);
    }

    public static <R> R getFieldValue(Class<?> clazz, String fieldName, Class<R> value) {
        return getFieldValue(clazz, (Object)null, fieldName, value);
    }

    public static Object getFieldValue(Object object, String fieldName) {
        return getFieldValue(object, fieldName, Object.class);
    }

    public static <R> R getFieldValue(Object object, String fieldName, Class<R> value) {
        return getFieldValue(object.getClass(), object, fieldName, value);
    }

    public static Object getFieldValue(Class<?> clazz, Object object, String fieldName) {
        return getFieldValue(clazz, object, fieldName, Object.class);
    }

    public static <R> R getFieldValue(Class<?> clazz, Object object, String fieldName, Class<R> value) {
        try {
            Field field = getField(clazz, fieldName);
            field.setAccessible(true);
            return value.cast(field.get(object));
        } catch (Exception var5) {
            throw new RuntimeException(var5);
        }
    }

    public static void changeFieldValue(Object object, String fieldName, Object value) {
        changeFieldValue(object.getClass(), object, fieldName, value);
    }

    public static void changeFieldValue(Class<?> clazz, Object object, String fieldName, Object value) {
        try {
            Field field = getField(clazz, fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception var5) {
            throw new RuntimeException("Could not change file " + fieldName + " from class " + clazz, var5);
        }
    }

    public static <U> void removeSilent(Iterable<U> list, Predicate<U> acceptor) {
        Iterator iterator = list.iterator();

        Object result;
        while(iterator.hasNext() && (result = iterator.next()) != null) {
            if (acceptor.test((U) result)) {
                iterator.remove();
            }
        }

    }

}
