package org.mcnative.loader.loaders;

import net.pretronic.libraries.utility.exception.OperationFailedException;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcnative.loader.bootstrap.standalone.BukkitMcNativePluginBootstrap;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class BukkitGuestPluginLoader implements GuestPluginLoader {

    private final File location;
    private final File loaderLocation;
    private Plugin plugin;

    public BukkitGuestPluginLoader(File location,File loaderLocation){
        this.location = location;
        this.loaderLocation = loaderLocation;
    }

    @Override
    public Object getInstance() {
        return plugin;
    }

    @Override
    public String getLoadedVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public void handlePluginLoad() {
        try {
            plugin = Bukkit.getPluginManager().loadPlugin(location);
            if(plugin == null) return;
            File folder = new File("plugins/"+plugin.getName());
            if(!folder.exists()) folder.mkdirs();
            ReflectionUtil.changeFieldValue(JavaPlugin.class,plugin,"dataFolder",folder);
        } catch (InvalidPluginException | InvalidDescriptionException e) {
            throw new OperationFailedException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handlePluginEnable() {
        if(Bukkit.getPluginManager() instanceof SimplePluginManager){
            ReflectionUtil.changeFieldValue(JavaPlugin.class,BukkitMcNativePluginBootstrap.INSTANCE,"isEnabled",false);
            List<Plugin> plugins = (List<Plugin>) ReflectionUtil.getFieldValue(SimplePluginManager.class,Bukkit.getPluginManager(),"plugins");
            Map<String, Plugin> lookupNames = (Map<String, Plugin>) ReflectionUtil.getFieldValue(SimplePluginManager.class,Bukkit.getPluginManager(),"lookupNames");
            plugins.remove(BukkitMcNativePluginBootstrap.INSTANCE);
            lookupNames.remove(BukkitMcNativePluginBootstrap.INSTANCE.getDescription().getName());
        }else{
            BukkitMcNativePluginBootstrap.INSTANCE.getLogger().log(Level.INFO,"Could not unregister loader plugin, this may cause issues");
            Bukkit.getPluginManager().disablePlugin(BukkitMcNativePluginBootstrap.INSTANCE);
        }

        Bukkit.getPluginManager().enablePlugin(plugin);
    }

    @Override
    public void handlePluginDisable() {

    }
}
