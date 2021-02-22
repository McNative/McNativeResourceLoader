/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 24.11.19, 16:30
 *
 * The McNative Project is under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.mcnative.loader.bootstrap.standalone;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcnative.loader.*;
import org.mcnative.loader.config.LoaderConfiguration;
import org.mcnative.loader.config.CredentialsConfig;
import org.mcnative.loader.utils.BukkitUtil;
import org.mcnative.loader.utils.LoaderUtil;

import java.io.File;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

public class BukkitMcNativePluginBootstrap extends JavaPlugin implements Listener, PlatformExecutor {

    private static final File LOADER_YML = new File("plugins/McNative/loader.yml");
    private static final File CONFIG_YML = new File("plugins/McNative/config.yml");
    private static final File LOADER_CACHE = new File("plugins/McNative/lib/rollout.dat");

    public static BukkitMcNativePluginBootstrap INSTANCE;
    private GuestPluginExecutor executor;

    @Override
    public void onLoad() {
        INSTANCE = this;
        try{
            CertificateValidation.disableIllegalAccessWarning();
            CertificateValidation.disable();
            CredentialsConfig.load(CONFIG_YML);

            InputStream loaderConfig = getClass().getClassLoader().getResourceAsStream("mcnative-loader.properties");
            Properties loaderProperties = new Properties();
            loaderProperties.load(loaderConfig);
            if(loaderConfig == null){
                getLogger().log(Level.SEVERE,"Invalid or corrupt McNative plugin (mcnative-loader.json is not available)");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            LoaderConfiguration configuration = LoaderConfiguration.load(LOADER_YML);
            configuration.pullProfiles(getLogger(),LOADER_CACHE);

            if(loaderProperties.getProperty("installMcNative").equalsIgnoreCase("true")){
                if(!McNativeLoader.install(getLogger(), EnvironmentNames.BUKKIT, configuration)){
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                }
            }

            configuration.save(LOADER_YML);

            this.executor = new GuestPluginExecutor(this,getFile(),getLogger(),EnvironmentNames.BUKKIT,loaderProperties,configuration);

            if(!this.executor.install()){
                this.executor = null;
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            this.executor.loadGuestPlugin();

            String version = this.executor.getLoader().getLoadedVersion();
            LoaderUtil.changeFieldValue(getDescription(),"version",version);

        }catch (Exception exception){
            this.executor = null;
            exception.printStackTrace();
            getLogger().log(Level.SEVERE,String.format("Could not load plugin (%s)",exception.getMessage()));
            getServer().getPluginManager().disablePlugin(this);
        }
        CertificateValidation.reset();
    }

    @Override
    public void onEnable() {
        try{
            if(this.executor != null){
                this.executor.enableGuestPlugin();
                if(isEnabled()){
                    Bukkit.getPluginManager().registerEvents(this,this);
                }
            }
        }catch (Exception exception){
            this.executor = null;
            exception.printStackTrace();
            getLogger().log(Level.SEVERE,String.format("Could not enable plugin (%s)",exception.getMessage()));
            getServer().getPluginManager().disablePlugin(this);
            try {
                JavaPlugin.class.getDeclaredField("isEnabled").set(this,false);
            } catch (IllegalAccessException | NoSuchFieldException ignored) { }
        }
    }

    @Override
    public void onDisable() {
        try{
            if(this.executor != null) this.executor.disableGuestPlugin();
        }catch (Exception exception){
            this.executor = null;
            exception.printStackTrace();
            getLogger().log(Level.SEVERE,String.format("Could not disable plugin (%s)",exception.getMessage()));
        }
    }

    @EventHandler
    public void handleMcNativeShutdown(PluginDisableEvent event){
        if(event.getPlugin().getName().equalsIgnoreCase("McNative")){
            getLogger().info("(McNative-Loader) McNative is shutting down, this plugins depends on McNative and is now also shutting down.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void shutdown() {
        Bukkit.getPluginManager().disablePlugin(this);
    }

    @Override
    public void bootstrap() {
        Bukkit.getPluginManager().enablePlugin(this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unload() {
        List<Plugin> plugins = (List<org.bukkit.plugin.Plugin>) LoaderUtil.getFieldValue(Bukkit.getPluginManager(),"plugins");
        Map<String, Plugin> names = (Map<String, org.bukkit.plugin.Plugin>) LoaderUtil.getFieldValue(Bukkit.getPluginManager(),"lookupNames");

        ClassLoader classLoader = getClass().getClassLoader();

        names.remove(this.getName());
        plugins.remove(this);

        if (classLoader instanceof URLClassLoader) {
            LoaderUtil.changeFieldValue(classLoader,"plugin",null);
            LoaderUtil.changeFieldValue(classLoader,"pluginInit",null);

            try {
                if(Class.forName("org.mcnative.runtime.api.McNative").getClassLoader() == classLoader){
                    getLogger().warning("Classes of "+getName()+" could not be unloaded, because this class loader is the host loader of McNative");
                    BukkitUtil.clearCachedClasses(classLoader);
                    return;
                }
            } catch (ClassNotFoundException ignored) {}

            BukkitUtil.closeLoader(classLoader);
        }

        System.gc();//Execute garbage collector
    }

}
