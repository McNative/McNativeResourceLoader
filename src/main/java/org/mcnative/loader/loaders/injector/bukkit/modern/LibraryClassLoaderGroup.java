package org.mcnative.loader.loaders.injector.bukkit.modern;

import org.bukkit.plugin.java.JavaPluginLoader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LibraryClassLoaderGroup extends URLClassLoader {

    private final Map<ClassLoader,Method> loaders = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();

    public LibraryClassLoaderGroup() {
        super(new URL[]{});
    }

    @Override
    public URL getResource(String name) {
        throw new UnsupportedOperationException("Group loader bridge does not support resource loading");
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        System.out.println(" => SEARCHING CLASS IN GROUP LOADER ("+System.identityHashCode(this)+") "+name);
        Class<?> result = classes.get(name);

        if (result == null) {
            for (Map.Entry<ClassLoader, Method> loader : loaders.entrySet()) {
                System.out.println(" - in "+loader.getKey().getClass());
                result = loader.getKey().loadClass(name);
                if(result != null){
                    classes.put(name, result);
                    return result;
                }
            }
            throw new ClassNotFoundException();
        }else{
            System.out.println(" - cached");
        }
        System.out.println("----");
        return result;
    }

    public Class<?> findClassIgnored(String name, ClassLoader ignore) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        Class<?> result = classes.get(name);

        if (result == null) {
            for (Map.Entry<ClassLoader, Method> loader : loaders.entrySet()) {
                if(!loader.equals(ignore)){
                    result = (Class<?>) loader.getValue().invoke(loader.getKey(),name);
                    if(result != null){
                        classes.put(name, result);
                        return result;
                    }
                }
            }
            throw new ClassNotFoundException();
        }
        return result;
    }

    public void addLoader(ClassLoader loader) throws NoSuchMethodException {
        System.out.println("REGISTERED IN GROUP LOADER ("+System.identityHashCode(this)+") "+loader.getClass());
        this.loaders.put(loader,loader.getClass().getDeclaredMethod("loadClassDirect",String.class));
    }

    public static void injectLoader(ClassLoader loader, BukkitModernUrlClassLoader toAdd) throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Field loaderField = loader.getClass().getDeclaredField("loader");
        loaderField.setAccessible(true);
        JavaPluginLoader javaLoader = (JavaPluginLoader) loaderField.get(loader);

        Field loaderListField = javaLoader.getClass().getDeclaredField("loaders");
        loaderListField.setAccessible(true);
        List<Object> loaders = (List<Object>) loaderListField.get(javaLoader);

        Field field = loader.getClass().getDeclaredField("libraryLoader");
        field.setAccessible(true);

        Object group = null;
        for (Object pluginLoader : loaders) {
            Object loaderResult = field.get(pluginLoader);
            if(loaderResult != null && loaderResult.getClass().getSimpleName().equalsIgnoreCase("LibraryClassLoaderGroup")){
                group = loaderResult;
            }
        }

        if(group == null){
            group = new LibraryClassLoaderGroup();
            field.set(loader,group);
        }

        if(toAdd == null) return;

        toAdd.setGroupLoader(group);

        Method method = group.getClass().getDeclaredMethod("addLoader",ClassLoader.class);
        method.setAccessible(true);
        method.invoke(group,toAdd);
    }

}
