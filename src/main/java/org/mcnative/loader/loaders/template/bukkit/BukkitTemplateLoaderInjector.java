package org.mcnative.loader.loaders.template.bukkit;

import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.mcnative.loader.GuestPluginExecutor;
import org.mcnative.loader.loaders.template.TemplateLoaderInjector;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class BukkitTemplateLoaderInjector implements TemplateLoaderInjector {

    @Override
    public ClassLoader getClassLoader(GuestPluginExecutor executor,File location) {
        try {
            return new BukkitSharedUrlClassLoader(new URL[]{location.toURI().toURL()},getClass().getClassLoader()
                    , ReflectionUtil.getFieldValue(getClass().getClassLoader(),"loader", JavaPluginLoader.class));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleEnable(GuestPluginExecutor executor) {
        BukkitDummyPlugin plugin = new BukkitDummyPlugin(executor.getLoader(),executor.getGuestName(),executor.getGuestVersion());
        List<Plugin> plugins = (List<Plugin>) ReflectionUtil.getFieldValue(Bukkit.getPluginManager(),"plugins");
        Map<String, Plugin> lookupNames = (Map<String, Plugin>) ReflectionUtil.getFieldValue(Bukkit.getPluginManager(),"lookupNames");
        plugins.add(plugin);
        lookupNames.put(plugin.getName(),plugin);
    }
}
