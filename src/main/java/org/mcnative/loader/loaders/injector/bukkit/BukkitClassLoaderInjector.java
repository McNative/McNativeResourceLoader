package org.mcnative.loader.loaders.injector.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.mcnative.loader.GuestPluginExecutor;
import org.mcnative.loader.loaders.injector.ClassLoaderInjector;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class BukkitClassLoaderInjector implements ClassLoaderInjector {

    @Override
    public ClassLoader loadMcNativeClasses(File location) {
        return getClassLoader(null,location);
    }

    @Override
    public ClassLoader getClassLoader(GuestPluginExecutor executor,File location) {
        try {
            Field field = getClass().getClassLoader().getClass().getDeclaredField("loader");
            field.setAccessible(true);

            return new BukkitSharedUrlClassLoader(location,new URL[]{location.toURI().toURL()},getClass().getClassLoader()
                    , (JavaPluginLoader) field.get(getClass().getClassLoader()));
        } catch (MalformedURLException | NoSuchFieldException | IllegalAccessException e) {
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
        if(loader instanceof BukkitSharedUrlClassLoader){
            BukkitMiddlewareClassMap.getInstance().removeLoader((BukkitSharedUrlClassLoader) loader);
        }
    }
}
