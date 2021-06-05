package org.mcnative.loader.loaders.injector.bukkit;

import org.bukkit.plugin.java.JavaPluginLoader;
import org.mcnative.loader.GuestPluginExecutor;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.*;

public class BukkitMiddlewareClassMap implements Map<String, Class<?>> {

    public static BukkitMiddlewareClassMap INSTANCE;

    private final Map<String, Class<?>> original;
    private final List<BukkitSharedUrlClassLoader> classLoaders;

    public BukkitMiddlewareClassMap(Map<String, Class<?>> original) {
        this.original = original;
        this.classLoaders = new ArrayList<>();
    }

    public Map<String, Class<?>> getOriginal() {
        return original;
    }

    public void addLoader(BukkitSharedUrlClassLoader loader){
        this.classLoaders.add(loader);
    }

    public void removeLoader(BukkitSharedUrlClassLoader loader){
        this.classLoaders.remove(loader);
    }

    @Override
    public int size() {
        return original.size();
    }

    @Override
    public boolean isEmpty() {
        return original.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return original.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return original.containsValue(value);
    }

    @Override
    public Class<?> get(Object key) {
        Class<?> clazz = original.get(key);
        if(clazz == null){
            for (BukkitSharedUrlClassLoader loader : classLoaders) {
                try {
                    clazz = loader.findClass((String) key);
                    original.put((String) key,clazz);
                    return clazz;
                } catch (ClassNotFoundException ignored) {}
            }
        }
        return clazz;
    }

    @Override
    public Class<?> put(String key, Class<?> value) {
        return original.put(key, value);
    }

    @Override
    public Class<?> remove(Object key) {
        return original.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Class<?>> m) {
        original.putAll(m);
    }

    @Override
    public void clear() {
        original.clear();
    }

    @Override
    public Set<String> keySet() {
        return original.keySet();
    }

    @Override
    public Collection<Class<?>> values() {
        return original.values();
    }

    @Override
    public Set<Entry<String, Class<?>>> entrySet() {
        return original.entrySet();
    }

    public static BukkitMiddlewareClassMap getInstance() {
        if(INSTANCE == null) INSTANCE = inject(GuestPluginExecutor.class.getClassLoader());
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public static BukkitMiddlewareClassMap inject(ClassLoader loader){
        try {
            Field field = loader.getClass().getDeclaredField("classes");
            field.setAccessible(true);
            Object original = field.get(loader);
            BukkitMiddlewareClassMap middleware = new BukkitMiddlewareClassMap((Map<String, Class<?>>) original);
            setUnsafeObjectFieldValue(loader,field,middleware);
            return middleware;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setUnsafeObjectFieldValue(Object value,Field field, Object newValue){
        try {
            Field uf = Unsafe.class.getDeclaredField("theUnsafe");
            uf.setAccessible(true);
            Unsafe unsafe = (Unsafe) uf.get(null);
            unsafe.putObject(value,unsafe.objectFieldOffset(field),newValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
