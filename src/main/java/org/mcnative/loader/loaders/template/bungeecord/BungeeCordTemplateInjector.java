package org.mcnative.loader.loaders.template.bungeecord;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.pretronic.libraries.utility.SystemUtil;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import net.pretronic.libraries.utility.reflect.UnsafeInstanceCreator;
import org.mcnative.loader.GuestPluginExecutor;
import org.mcnative.loader.loaders.template.TemplateLoaderInjector;
import org.mcnative.loader.utils.LoaderUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

public class BungeeCordTemplateInjector implements TemplateLoaderInjector {

    private BungeeCordDummyPlugin plugin;

    @Override
    public ClassLoader getClassLoader(GuestPluginExecutor executor,File location) {
        PluginDescription description = new PluginDescription();
        description.setName(executor.getGuestName());
        description.setVersion(executor.getGuestVersion());
        description.setMain("reflected");

        this.plugin = UnsafeInstanceCreator.newInstance(BungeeCordDummyPlugin.class);
        ReflectionUtil.invokeMethod(Plugin.class,plugin,"init"
                ,new Class[]{ProxyServer.class,PluginDescription.class}
                ,new Object[]{ProxyServer.getInstance(),description});

        try{
            //ProxyServer proxy, PluginDescription desc, File file, ClassLoader libraryLoader
            Class<?> loaderClass = Class.forName("net.md_5.bungee.api.plugin.PluginClassloader");
            Constructor<?> constructor = loaderClass.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            URLClassLoader loader;

            //
            if(constructor.getParameterCount() == 4){
                loader = (URLClassLoader) constructor.newInstance(ProxyServer.getInstance(), description, new URL[]{location.toURI().toURL()},ProxyServer.class.getClassLoader());
            }else {
                if(SystemUtil.getJavaBaseVersion() > 16){
                    throw new UnsupportedOperationException("McNative Template loader does not support the current BungeeCord build with Java 16, please update to the latest BungeeCord version.");
                }

                loader = (URLClassLoader) constructor.newInstance(ProxyServer.getInstance(), description, new URL[]{location.toURI().toURL()});
                //Change parent class loader to root loader
                ReflectionUtil.changeFieldValue(ClassLoader.class,loader,"parent",ProxyServer.class.getClassLoader());
            }
            return loader;
        }catch (Exception e){
            throw new UnsupportedOperationException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleEnable(GuestPluginExecutor executor) {
        plugin.setLoader(executor.getLoader());
        Map<String, Plugin> plugins = (Map<String, Plugin>) LoaderUtil.getFieldValue(ProxyServer.getInstance().getPluginManager(),"plugins");
        plugins.put(plugin.getDescription().getName(),plugin);
        plugin.onEnable();
    }
}
