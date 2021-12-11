package org.mcnative.loader.loaders.injector.bukkit.modern;

import org.bukkit.plugin.java.JavaPluginLoader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LibraryClassLoaderGroup extends URLClassLoader {

    private final Collection<BukkitModernUrlClassLoader> loaders = ConcurrentHashMap.newKeySet();
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
        Class<?> result = classes.get(name);

        if (result == null) {
            for (ClassLoader loader : loaders) {
                result = loader.loadClass(name);
                if(result != null){
                    classes.put(name, result);
                    return result;
                }
            }
            throw new ClassNotFoundException();
        }
        return result;
    }

    public Class<?> findClassIgnored(String name, ClassLoader ignore) throws ClassNotFoundException {
        Class<?> result = classes.get(name);

        if (result == null) {
            for (BukkitModernUrlClassLoader loader : loaders) {
                if(!loader.equals(ignore)){
                    result = loader.loadClassDirect(name);
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

    public void addLoader(BukkitModernUrlClassLoader loader){
        this.loaders.add(loader);
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

        toAdd.setGroupLoader(group);

        Method method = group.getClass().getDeclaredMethod("addLoader",BukkitModernUrlClassLoader.class);
        method.setAccessible(true);
        method.invoke(group,toAdd);
    }

}
