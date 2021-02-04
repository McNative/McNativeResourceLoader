package org.mcnative.loader.bootstrap.template;

import org.bukkit.plugin.java.JavaPlugin;
import org.mcnative.loader.*;
import org.mcnative.loader.config.LoaderConfiguration;
import org.mcnative.loader.config.McNativeConfig;
import org.mcnative.loader.config.Template;
import org.mcnative.loader.loaders.template.bukkit.BukkitTemplateLoaderInjector;
import org.mcnative.loader.loaders.template.TemplateLoaderInjector;
import org.mcnative.loader.utils.BukkitUtil;
import org.mcnative.loader.utils.McNativeUtil;
import org.mcnative.loader.utils.PrefixLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

public class BukkitTemplateLoaderBootstrap extends JavaPlugin implements PlatformExecutor {

    private static final File LOADER_YML = new File("plugins/McNative/loader.yml");
    private static final File CONFIG_YML = new File("plugins/McNative/config.yml");
    private static final File LOADER_CACHE = new File("plugins/McNative/lib/rollout.dat");

    private final List<GuestPluginExecutor> executors = new ArrayList<>();

    @Override
    public void onLoad() {
        try {
            CertificateValidation.disableIllegalAccessWarning();
            CertificateValidation.disable();

            McNativeConfig.load(CONFIG_YML);;
            LoaderConfiguration config = LoaderConfiguration.load(LOADER_YML);
            config.pullProfiles(getLogger(),LOADER_CACHE);

            Template template = Template.pullTemplate(getLogger(),config);
            if(template == null){
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            if(!McNativeLoader.install(getLogger(), EnvironmentNames.BUKKIT, config)){
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            McNativeUtil.registerVariables(template);

            for (Map.Entry<String, String> resource : template.getResources().entrySet()) {
                Properties properties = new Properties();
                properties.setProperty("plugin.name",resource.getKey());
                properties.setProperty("plugin.id",resource.getValue());
                GuestPluginExecutor executor = new GuestPluginExecutor(this,getFile()
                        ,new PrefixLogger(getLogger(),resource.getKey())
                        ,EnvironmentNames.BUKKIT,properties,config);
                this.executors.add(executor);
                try {
                    executor.installMultiple(new BukkitTemplateLoaderInjector());
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
            getServer().getPluginManager().disablePlugin(this);
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
    public void shutdown() {
        throw new UnsupportedOperationException("It is not possible to shutdown the McNative template loader");
    }

    @Override
    public void bootstrap() {
        throw new UnsupportedOperationException("It is not possible to bootstrap the McNative template loader");
    }

    @Override
    public void unload() {
        ClassLoader classLoader = getClass().getClassLoader();
        BukkitUtil.closeLoader(classLoader);
        System.gc();//Execute garbage collector
    }

}
