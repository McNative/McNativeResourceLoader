package org.mcnative.loader.loaders.mcnative;

import net.pretronic.libraries.dependency.DependencyGroup;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.logging.bridge.JdkPretronicLogger;
import net.pretronic.libraries.plugin.Plugin;
import net.pretronic.libraries.plugin.RuntimeEnvironment;
import net.pretronic.libraries.plugin.description.DefaultPluginDescription;
import net.pretronic.libraries.plugin.description.PluginDescription;
import net.pretronic.libraries.plugin.loader.classloader.BridgedPluginClassLoader;
import org.mcnative.loader.PlatformExecutor;
import org.mcnative.loader.loaders.GuestPluginLoader;
import org.mcnative.runtime.api.McNative;

import java.io.File;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class McNativeGuestPluginLoader implements GuestPluginLoader {

    private final Logger logger;
    private final McNativePluginLoader loader;

    public McNativeGuestPluginLoader(PlatformExecutor executor, String runtimeName, Logger logger, File location, InputStream descriptionStream) {
        PluginDescription description = DefaultPluginDescription.create(McNative.getInstance().getPluginManager()
                ,DocumentFileType.JSON.getReader().read(descriptionStream));

        this.logger = logger;
        this.loader = new McNativePluginLoader(executor, McNative.getInstance().getPluginManager()
                ,new RuntimeEnvironment<>(runtimeName,McNative.getInstance())
                ,new JdkPretronicLogger(logger),new BridgedPluginClassLoader(getClass().getClassLoader())
                ,location,description,false);
        installDependencies();
    }

    @Override
    public Object getInstance() {
        return loader.getInstance();
    }

    @Override
    public String getLoadedVersion() {
        return loader.getDescription().getVersion().getName();
    }

    @Override
    public void handlePluginLoad() {
        McNative.getInstance().getPluginManager().provideLoader(loader);
        loader.construct();
        loader.initialize();
        loader.load();
    }

    @Override
    public void handlePluginEnable() {
        loader.bootstrapInternal();
    }

    @Override
    public void handlePluginDisable() {
        Plugin<?> owner = loader.getInstance();
        loader.shutdownInternal();
        McNative instance = McNative.getInstance();
        instance.getRegistry().unregisterService(owner);
        instance.getScheduler().unregister(owner);
        instance.getLocal().getEventBus().unsubscribe(owner);
        instance.getLocal().getCommandManager().unregisterCommand(owner);
        if(instance.isNetworkAvailable()){
            instance.getNetwork().getMessenger().unregisterChannels(owner);
        }
    }

    private void installDependencies(){
        InputStream stream = loader.getClassLoader().getResourceAsStream("dependencies.json");
        if(stream == null) return;
        Document data = DocumentFileType.JSON.getReader().read(stream);
        try{
            DependencyGroup dependencies = McNative.getInstance().getDependencyManager().load(data);
            dependencies.install();
            dependencies.loadReflected((URLClassLoader) loader.getClassLoader().asJVMLoader());
        }catch (Exception exception){
            logger.log(Level.SEVERE,String.format("Could not install dependencies %s",exception.getMessage()));
            throw new RuntimeException(exception);
        }
    }

}
