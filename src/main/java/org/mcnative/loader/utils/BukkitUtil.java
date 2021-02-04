package org.mcnative.loader.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.regex.Pattern;

public class BukkitUtil {

    public static void closeLoader(ClassLoader classLoader){
        Map<String, Class<?>> classes = (Map<String, Class<?>>) LoaderUtil.getFieldValue(classLoader,"classes");
        classes.clear();
        BukkitUtil.clearCachedClasses(classLoader);

        try {
            ((URLClassLoader) classLoader).close();
        } catch (IOException ignored) {}
    }

    @SuppressWarnings("unchecked")
    public static void clearCachedClasses(ClassLoader classLoader){
        Map<Pattern, PluginLoader> loaders = (Map<Pattern, PluginLoader>) LoaderUtil.getFieldValue(Bukkit.getPluginManager(),"fileAssociations");
        for (Map.Entry<Pattern, PluginLoader> loader : loaders.entrySet()) {
            if(loader.getValue() instanceof JavaPluginLoader){
                Map<String, Class<?>> classes = (Map<String, Class<?>>) LoaderUtil.getFieldValue(loader.getValue(),"classes");
                LoaderUtil.removeSilent(classes.entrySet(), entry -> entry.getValue().getClassLoader().equals(classLoader));
            }
        }
    }

}
