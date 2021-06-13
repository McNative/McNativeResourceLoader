package org.mcnative.loader.loaders.injector.bukkit;

import net.pretronic.libraries.utility.reflect.ReflectException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.mcnative.loader.GuestPluginExecutor;
import org.mcnative.loader.loaders.injector.ClassLoaderInjector;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;

public class BukkitLegacyClassLoaderInjector implements ClassLoaderInjector {

    private final Method METHOD_ADD_URL;

    public BukkitLegacyClassLoaderInjector() {
        try {
            METHOD_ADD_URL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            METHOD_ADD_URL.setAccessible(true);
        } catch (NoSuchMethodException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    @Override
    public ClassLoader loadMcNativeClasses(File location) {
        return getClassLoader(null,location);
    }

    @Override
    public ClassLoader getClassLoader(GuestPluginExecutor executor,File location) {
        ClassLoader loader = getClass().getClassLoader();
        try {
            METHOD_ADD_URL.invoke(loader, location.toURI().toURL());
            return loader;
        } catch (IllegalAccessException | InvocationTargetException | MalformedURLException exception) {
            throw new RuntimeException(exception);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleEnable(GuestPluginExecutor executor) {
        try {
            Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
            Field lookupField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
            pluginsField.setAccessible(true);
            lookupField.setAccessible(true);

            BukkitDummyPlugin plugin = new BukkitDummyPlugin(executor.getLoader(),executor.getGuestName(),executor.getGuestVersion());
            List<Plugin> plugins = (List<Plugin>) pluginsField.get(Bukkit.getPluginManager());
            Map<String, Plugin> lookupNames = (Map<String, Plugin>) lookupField.get(Bukkit.getPluginManager());
            plugins.add(plugin);
            lookupNames.put(plugin.getName(),plugin);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleDisable(GuestPluginExecutor executor) {
        ClassLoader loader = executor.getLoader().getClassLoader();
        if(loader instanceof BukkitSharedUrlClassLoader){
            BukkitMiddlewareClassMap.getInstance().removeLoader((BukkitSharedUrlClassLoader) loader);
        }
    }
}
