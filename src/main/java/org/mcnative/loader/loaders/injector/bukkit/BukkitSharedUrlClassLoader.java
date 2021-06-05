package org.mcnative.loader.loaders.injector.bukkit;

import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitSharedUrlClassLoader extends URLClassLoader {

    private final File file;
    private final JavaPluginLoader loader;
    private final Method addClassToCache;

    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();

    public BukkitSharedUrlClassLoader(File file,URL[] urls, ClassLoader parent, JavaPluginLoader loader) {
        super(urls, parent);
        this.file = file;
        this.loader = loader;

        try {
            Method getClassByName = loader.getClass().getDeclaredMethod("getClassByName", String.class);
            this.addClassToCache = loader.getClass().getDeclaredMethod("setClass",String.class,Class.class);

            getClassByName.setAccessible(true);
            this.addClassToCache.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        BukkitMiddlewareClassMap.getInstance(loader).addLoader(this);
    }

    public File getFile() {
        return file;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> result = classes.get(name);

        if (result == null) {
            result = getClassByName(name);
            if (result == null) {
                result = super.findClass(name);
                if(result == null) throw  new ClassNotFoundException();
                addClassToCache(name, result);
                classes.put(name, result);
            }
        }
        return result;
    }

    private Class<?> getClassByName(String name){
        return BukkitMiddlewareClassMap.getInstance(null).getOriginal().get(name);
    }

    private void addClassToCache(String name,Class<?> clazz){
        try {
            addClassToCache.invoke(loader,name,clazz);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}

