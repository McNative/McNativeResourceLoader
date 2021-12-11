package org.mcnative.loader.loaders;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;
import org.mcnative.loader.bootstrap.standalone.BungeeCordMcNativePluginBootstrap;
import org.mcnative.loader.utils.LoaderUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class BungeeCordGuestPluginLoader implements GuestPluginLoader {

    private final File location;
    private final File loaderLocation;
    private final boolean multiple;
    private Plugin plugin;

    public BungeeCordGuestPluginLoader(File location, File loaderLocation, boolean multiple){
        this.location = location;
        this.loaderLocation = loaderLocation;
        this.multiple = multiple;
    }

    @Override
    public Object getInstance() {
        return plugin;
    }

    @Override
    public ClassLoader getClassLoader() {
        return plugin.getClass().getClassLoader();
    }

    @Override
    public String getLoadedVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public void handlePluginLoad() {
        PluginDescription description = loadPluginDescription(location);
        plugin = loadPlugin(description);
        if(plugin == null) return;
        File folder = new File("plugins/"+description.getName());
        if(!folder.exists()) folder.mkdirs();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handlePluginEnable() {
        Map<String, Plugin> plugins = (Map<String, Plugin>) LoaderUtil.getFieldValue(ProxyServer.getInstance().getPluginManager(),"plugins");
        if(!multiple){
            plugins.remove(BungeeCordMcNativePluginBootstrap.INSTANCE.getDescription().getName());
        }
        plugins.put(plugin.getDescription().getName(),plugin);
        plugin.onEnable();
    }

    @Override
    public void handlePluginDisable() {}

    private PluginDescription loadPluginDescription(File file){
        if ( file.isFile() && file.getName().endsWith( ".jar" ) ) {
            try ( JarFile jar = new JarFile( file ) ) {
                JarEntry pdf = jar.getJarEntry("template/bungee.yml");
                if (pdf == null ) pdf = jar.getJarEntry("template/plugin.yml");
                if(pdf == null) throw new IllegalArgumentException("Plugin must have a plugin.yml or bungee.yml");


                try ( InputStream in = jar.getInputStream( pdf ) )
                {
                    Yaml yaml = LoaderUtil.getFieldValue(ProxyServer.getInstance().getPluginManager(),"yaml",Yaml.class);
                    PluginDescription desc = yaml.loadAs( in, PluginDescription.class );
                    if(desc.getName() == null) throw new IllegalArgumentException("Plugin from "+file.getName()+" has no name");
                    if(desc.getMain() == null) throw new IllegalArgumentException("Plugin from "+file.getName()+" has no main");

                    desc.setFile( file );
                    return desc;
                }
            } catch ( Exception ex )
            {
                ProxyServer.getInstance().getLogger().log( Level.WARNING, "Could not load plugin from file " + file, ex );
            }
        }
        throw new IllegalArgumentException("file not found");
    }

    @SuppressWarnings("unchecked")
    private Plugin loadPlugin(PluginDescription description){
        try
        {
            Class<?> loaderClass = Class.forName("net.md_5.bungee.api.plugin.PluginClassloader");
            Constructor<?> constructor = loaderClass.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            URLClassLoader loader = (URLClassLoader) constructor.newInstance(ProxyServer.getInstance(), description, new URL[]{location.toURI().toURL()});

            //Inject new loader
            Set loaders = LoaderUtil.getFieldValue(loaderClass,"allLoaders",Set.class);
            loaders.add(loader);

            Class<?> main = loader.loadClass( description.getMain() );
            Plugin clazz = (Plugin) main.getDeclaredConstructor().newInstance();

            clazz.onLoad();
            ProxyServer.getInstance().getLogger().log( Level.INFO, "Loaded plugin {0} version {1} by {2}"
                    , new Object[]{description.getName(), description.getVersion(), description.getAuthor()});
            return clazz;
        } catch ( Throwable t ) {
            ProxyServer.getInstance().getLogger().log(Level.WARNING, "Error enabling plugin " + description.getName(),t);
            throw new IllegalArgumentException(t);
        }
    }
}
