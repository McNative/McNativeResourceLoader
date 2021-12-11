package org.mcnative.loader.loaders.injector.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.mcnative.loader.loaders.injector.ClassLoaderInjector;
import org.mcnative.loader.loaders.injector.bukkit.modern.ModernClassLoaderInjector;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BukkitInjectorGetter {

    public static ClassLoaderInjector get(Logger logger){
        if(getJavaBaseVersion() < 16){
            if(isMinecraft16()) {
                logger.log(Level.WARNING,"[McNative] Using legacy class loader injector because of 1.16 compatibility issues");
                return new BukkitLegacyClassLoaderInjector();
            }
            return new BukkitClassLoaderInjector();
        }else{
            if(isOldVersion()){
                logger.log(Level.WARNING,"[McNative] ------------------------");
                logger.log(Level.WARNING,"[McNative] Versions above Java 15 are not officially supported for "+ getVersion()+", it might cause issues on your server");
                logger.log(Level.WARNING,"[McNative] ------------------------");
            }

            try {
                Field field = BukkitInjectorGetter.class.getClassLoader().getClass().getDeclaredField("loader");
                field.setAccessible(true);
                JavaPluginLoader  loader = (JavaPluginLoader) field.get(BukkitInjectorGetter.class.getClassLoader());
                Field hasField = loader.getClass().getDeclaredField("classes");
            } catch (IllegalAccessException | NoSuchFieldException e) {
                logger.log(Level.INFO,"[McNative] Using modern class loader implementation");
                return new ModernClassLoaderInjector();
            }
            logger.log(Level.INFO,"[McNative] Using standard class loader implementation");
            return new BukkitClassLoaderInjector();
        }
    }

    private static boolean isOldVersion(){
        try {
            Class.forName("org.bukkit.plugin.java.LibraryLoader");
            return false;
        } catch (ClassNotFoundException e) {
            return true;
        }
    }

    private static boolean isMinecraft16(){
        return getVersion().toLowerCase().contains("1_16");
    }

    private static String getVersion() {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

    public static int getJavaBaseVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }
}
