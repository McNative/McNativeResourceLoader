package org.mcnative.loader.loaders.template.bukkit;

import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitSharedUrlClassLoader extends URLClassLoader {

    private final JavaPluginLoader loader;
    private final Method getClassByName;
    private final Method addClassToCache;

    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();

    public BukkitSharedUrlClassLoader(URL[] urls, ClassLoader parent, JavaPluginLoader loader) {
        super(urls, parent);
        this.loader = loader;

        this.getClassByName = ReflectionUtil.getMethod(loader.getClass(),"getClassByName",new Class[]{String.class});
        this.addClassToCache = ReflectionUtil.getMethod(loader.getClass(),"setClass",new Class[]{String.class,Class.class});

        this.getClassByName.setAccessible(true);
        this.addClassToCache.setAccessible(true);
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
        try {
            return (Class<?>) getClassByName.invoke(loader,name);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void addClassToCache(String name,Class<?> clazz){
        try {
            addClassToCache.invoke(loader,name,clazz);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}

