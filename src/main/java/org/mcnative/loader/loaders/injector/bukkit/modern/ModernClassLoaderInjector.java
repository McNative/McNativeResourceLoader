package org.mcnative.loader.loaders.injector.bukkit.modern;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.mcnative.loader.GuestPluginExecutor;
import org.mcnative.loader.loaders.injector.ClassLoaderInjector;
import org.mcnative.loader.loaders.injector.bukkit.BukkitDummyPlugin;
import org.mcnative.loader.loaders.injector.bukkit.BukkitMiddlewareClassMap;
import org.mcnative.loader.loaders.injector.bukkit.BukkitSharedUrlClassLoader;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ModernClassLoaderInjector implements ClassLoaderInjector {

    @Override
    public void prepare() {
        try {
            LibraryClassLoaderGroup.injectLoader(getClass().getClassLoader(),null);
        } catch (NoSuchFieldException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ClassLoader loadMcNativeClasses(File location) {
        return getClassLoader(null,location);
    }

    @Override
    public ClassLoader getClassLoader(GuestPluginExecutor executor,File location) {
        try {
            Field field = getClass().getClassLoader().getClass().getDeclaredField("loader");
            field.setAccessible(true);

            BukkitModernUrlClassLoader loader =  new BukkitModernUrlClassLoader(location,new URL[]{location.toURI().toURL()},getClass().getClassLoader()
                    , (JavaPluginLoader) field.get(getClass().getClassLoader()));
            LibraryClassLoaderGroup.injectLoader(getClass().getClassLoader(),loader);
            return loader;
        } catch (MalformedURLException | NoSuchFieldException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
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
        if(loader instanceof BukkitModernUrlClassLoader){
            //remove
        }
    }
}
