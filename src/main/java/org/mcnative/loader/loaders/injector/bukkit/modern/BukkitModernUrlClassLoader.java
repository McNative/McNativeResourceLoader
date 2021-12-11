package org.mcnative.loader.loaders.injector.bukkit.modern;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*

loadClass
    check cached class map
    if null
        for loadClass (not global / no libraries) => iterate trough class loaders
        if null
            loadClass => in current library loader

    cache class and return

 */
public class BukkitModernUrlClassLoader extends URLClassLoader {

    private final File file;

    private final Method loadClass;

    private final List<Object> loaders;
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();

    private Object groupLoader;
    private Method librarySearchMethod;

    public BukkitModernUrlClassLoader(File file, URL[] urls, ClassLoader parent, JavaPluginLoader loader) {
        super(urls, Bukkit.class.getClassLoader());
        this.file = file;

        try {
            this.loadClass = parent.getClass().getDeclaredMethod("loadClass0",String.class,boolean.class, boolean.class, boolean.class);
            this.loadClass.setAccessible(true);

            Field loadersField = loader.getClass().getDeclaredField("loaders");
            loadersField.setAccessible(true);
            this.loaders = (List<Object>) loadersField.get(loader);
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setGroupLoader(Object loader) throws NoSuchMethodException {
        groupLoader = loader;
        librarySearchMethod = loader.getClass().getDeclaredMethod("findClassIgnored",String.class,ClassLoader.class);
        librarySearchMethod.setAccessible(true);
    }

    public File getFile() {
        return file;
    }

    public Class<?> loadClassDirect(String name) throws ClassNotFoundException {
        Class<?> result = classes.get(name);

        if (result == null) {
            result = super.findClass(name);
            classes.put(name, result);
        }

        return result;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> result = classes.get(name);

        if (result == null) {
            result = getClassByName(name);
            if (result == null) {
                result = super.findClass(name);
                if(result == null) throw  new ClassNotFoundException();
                classes.put(name, result);
            }
        }
        return result;
    }

    private Class<?> getClassByName(String name){
        try {
            for (Object loader : loaders) {
                try {
                    return(Class<?>) loadClass.invoke(loader,name, true, false, false);
                } catch (InvocationTargetException ignored) {}
            }
            try{
                return (Class<?>) librarySearchMethod.invoke(this.groupLoader,name,this);
            } catch (InvocationTargetException ignored) {}
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public String toString() {
        return "BukkitModernMiddlewareClassMap{"+this.file.getName()+"}";
    }
}

