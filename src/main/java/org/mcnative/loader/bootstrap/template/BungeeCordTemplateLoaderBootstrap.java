package org.mcnative.loader.bootstrap.template;

import net.md_5.bungee.api.plugin.Plugin;
import org.mcnative.loader.*;
import org.mcnative.loader.config.CredentialsConfig;
import org.mcnative.loader.config.LoaderConfiguration;
import org.mcnative.loader.config.Template;
import org.mcnative.loader.loaders.injector.ClassLoaderInjector;
import org.mcnative.loader.loaders.injector.bungeecord.BungeeCordClassLoaderInjector;
import org.mcnative.loader.utils.PrefixLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

public class BungeeCordTemplateLoaderBootstrap extends Plugin implements PlatformExecutor {

    private static final File LOADER_YML = new File("plugins/McNative/loader.yml");
    private static final File CONFIG_YML = new File("plugins/McNative/config.yml");
    private static final File LOADER_CACHE = new File("plugins/McNative/lib/rollout.dat");

    private final List<GuestPluginExecutor> executors = new ArrayList<>();

    @Override
    public void onLoad() {
        try {
            CertificateValidation.disableIllegalAccessWarning();
            CertificateValidation.disable();

            CredentialsConfig.load(CONFIG_YML);;
            LoaderConfiguration config = LoaderConfiguration.load(LOADER_YML);
            config.pullProfiles(getLogger(),LOADER_CACHE);

            Template template = Template.pullTemplate(getLogger(),config);
            if(template == null){
                getProxy().getPluginManager().unregisterCommands(this);
                getProxy().getPluginManager().unregisterListeners(this);
                return;
            }

            ClassLoaderInjector injector = new BungeeCordClassLoaderInjector();

            if(!McNativeLoader.install(getLogger(), EnvironmentNames.BUNGEECORD,injector, config,template.getVariables())){
                getProxy().getPluginManager().unregisterCommands(this);
                getProxy().getPluginManager().unregisterListeners(this);
                return;
            }

            for (Map.Entry<String, String> resource : template.getResources().entrySet()) {
                Properties properties = new Properties();
                properties.setProperty("plugin.name",resource.getKey());
                properties.setProperty("plugin.id",resource.getValue());
                GuestPluginExecutor executor = new GuestPluginExecutor(this,injector,getFile()
                        ,new PrefixLogger(getLogger(),resource.getKey())
                        ,EnvironmentNames.BUNGEECORD,properties,config);
                this.executors.add(executor);
                try {
                    executor.installMultiple();
                }catch (Exception e){
                    getLogger().log(Level.SEVERE,"Could not install plugin "+resource.getKey());
                    e.printStackTrace();
                }
            }

            for (GuestPluginExecutor executor : executors) {
                try {
                    executor.loadGuestPlugin();
                }catch (Exception e){
                    getLogger().log(Level.SEVERE,"Could not load plugin "+executor.getGuestName());
                    e.printStackTrace();
                }
            }
        }catch (Exception exception){
            getLogger().log(Level.SEVERE,"Failed to start McNative template loader "+exception.getMessage());
            exception.printStackTrace();
            getProxy().getPluginManager().unregisterCommands(this);
            getProxy().getPluginManager().unregisterListeners(this);
        }
        CertificateValidation.reset();
    }

    @Override
    public void onEnable() {
        for (GuestPluginExecutor executor : executors) {
            try {
                executor.enableGuestPlugin();
            }catch (Exception e){
                getLogger().log(Level.SEVERE,"Could not enable plugin "+executor.getGuestName());
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("It is not possible to shutdown the McNative template loader");
    }

    @Override
    public void bootstrap() {
        throw new UnsupportedOperationException("It is not possible to bootstrap the McNative template loader");
    }

    @Override
    public void unload() {

    }

}
