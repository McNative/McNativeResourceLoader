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

import net.md_5.bungee.api.plugin.Plugin;
import org.mcnative.loader.*;
import org.mcnative.loader.config.LoaderConfiguration;
import org.mcnative.loader.config.CredentialsConfig;
import org.mcnative.loader.utils.LoaderUtil;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

public class BungeeCordMcNativePluginBootstrap extends Plugin implements PlatformExecutor {

    private static final File LOADER_YML = new File("plugins/McNative/loader.yml");
    private static final File CONFIG_YML = new File("plugins/McNative/config.yml");
    private static final File LOADER_CACHE = new File("plugins/McNative/lib/rollout.dat");

    public static BungeeCordMcNativePluginBootstrap INSTANCE;
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
                getProxy().getPluginManager().unregisterCommands(this);
                getProxy().getPluginManager().unregisterListeners(this);
                return;
            }

            LoaderConfiguration configuration = LoaderConfiguration.load(LOADER_YML);
            configuration.pullProfiles(getLogger(),LOADER_CACHE);

            if(loaderProperties.getProperty("installMcNative").equalsIgnoreCase("true")){
                if(!McNativeLoader.install(getLogger(), EnvironmentNames.BUNGEECORD, configuration)){
                    getProxy().getPluginManager().unregisterCommands(this);
                    getProxy().getPluginManager().unregisterListeners(this);
                    return;
                }
            }

            configuration.save(LOADER_YML);

            if(!McNativeLoader.install(getLogger(), EnvironmentNames.BUNGEECORD, configuration)) return;
            this.executor = new GuestPluginExecutor(this,getDescription().getFile(),getLogger(),EnvironmentNames.BUNGEECORD,loaderProperties,configuration);

            if(!this.executor.install()){
                this.executor = null;
                return;
            }

            this.executor.loadGuestPlugin();

            String version = this.executor.getLoader().getLoadedVersion();
            LoaderUtil.changeFieldValue(getDescription(),"version",version);
        }catch (Exception exception){
            this.executor = null;
            getLogger().log(Level.SEVERE,String.format("Could not bootstrap plugin (%s)",exception.getMessage()));
            exception.printStackTrace();
            getProxy().getPluginManager().unregisterCommands(this);
            getProxy().getPluginManager().unregisterListeners(this);
        }
        CertificateValidation.reset();
    }

    @Override
    public void onEnable() {
        if(this.executor != null) this.executor.enableGuestPlugin();
    }

    @Override
    public void onDisable() {
        if(this.executor != null) this.executor.disableGuestPlugin();
    }

    @Override
    public boolean equals(Object obj) {
        if(executor != null && executor.getLoader() != null && executor.getLoader().isInstanceAvailable()){
            return executor.getLoader().getInstance().equals(obj);
        }
        return super.equals(obj);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void bootstrap() {

    }

    @Override
    public void unload() {

    }
}
