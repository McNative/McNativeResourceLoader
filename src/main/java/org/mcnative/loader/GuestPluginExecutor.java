/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 24.11.19, 16:25
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

package org.mcnative.loader;

import net.pretronic.libraries.resourceloader.ResourceException;
import net.pretronic.libraries.resourceloader.ResourceInfo;
import net.pretronic.libraries.resourceloader.ResourceLoader;
import net.pretronic.libraries.resourceloader.VersionInfo;
import org.mcnative.loader.config.LoaderConfiguration;
import org.mcnative.loader.config.McNativeConfig;
import org.mcnative.loader.config.ResourceConfig;
import org.mcnative.loader.loaders.BukkitGuestPluginLoader;
import org.mcnative.loader.loaders.BungeeCordGuestPluginLoader;
import org.mcnative.loader.loaders.GuestPluginLoader;
import org.mcnative.loader.loaders.mcnative.McNativeGuestPluginLoader;
import org.mcnative.loader.utils.LoaderUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GuestPluginExecutor {

    private static final String VERSION_URL = "https://{profile.server}/v1/{resource.id}/versions/latest?plain=true&qualifier={profile.qualifier}";
    private static final String DOWNLOAD_URL = "https://{profile.server}/v1/{resource.id}/versions/{version.build}/download";

    private final PlatformExecutor executor;
    private final File location;
    private final Logger logger;
    private final String runtimeName;
    private final Properties loaderProperties;
    private final LoaderConfiguration configuration;
    private GuestPluginLoader loader;
    private ResourceLoader resourceLoader;

    private boolean multiple;

    public GuestPluginExecutor(PlatformExecutor executor, File location, Logger logger, String runtimeName,Properties loaderProperties, LoaderConfiguration configuration) {
        this.executor = executor;
        this.location = location;
        this.logger = logger;
        this.runtimeName = runtimeName;
        this.loaderProperties = loaderProperties;
        this.configuration = configuration;
        this.multiple = false;
    }

    public boolean installMultiple(){
        this.multiple = true;
        try{
            if(downloadResource(loaderProperties)){
                setupLoader(null);
            }else return false;
        }catch (Exception exception){
            logger.log(Level.SEVERE,String.format("Could not install plugin %s",exception.getMessage()));
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean install(){
        InputStream stream = getClass().getClassLoader().getResourceAsStream("mcnative.json");
        if(stream != null){
            setupLoader(stream);
        }else{
            try{
                if(downloadResource(loaderProperties)){
                    setupLoader(null);
                }else return false;
            }catch (Exception exception){
                logger.log(Level.SEVERE,String.format("Could not install plugin %s",exception.getMessage()));
                exception.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private boolean setupLoader(InputStream descriptionStream){
        if (descriptionStream == null && resourceLoader.getCurrentVersion() == null) {
            throw new ResourceException("No installed version found");
        }
        File location = resourceLoader != null ? resourceLoader.getLocalFile(resourceLoader.getCurrentVersion()) : this.location;
        if(descriptionStream != null || isMcNativePlugin(location)){
            if(descriptionStream == null){
                ClassLoader classLoader = getClass().getClassLoader();
                resourceLoader.loadReflected((URLClassLoader) classLoader);
            }
            this.loader = new McNativeGuestPluginLoader(executor,this.runtimeName,this.logger,location,descriptionStream);
            return true;
        }else if(runtimeName.equalsIgnoreCase(EnvironmentNames.BUKKIT)){
            this.loader = new BukkitGuestPluginLoader(location,this.location,multiple);
            return true;
        }else if(runtimeName.equalsIgnoreCase(EnvironmentNames.BUNGEECORD)){
            this.loader = new BungeeCordGuestPluginLoader(location,this.location,multiple);
            return true;
        }else{
            logger.log(Level.SEVERE,"(Resource-Loader) No valid plugin manifest found");
            return false;
        }
    }

    private boolean downloadResource(Properties loader){
        String name = loader.getProperty("plugin.name");
        String id = loader.getProperty("plugin.id");

        ResourceConfig config = configuration.getResourceConfig(UUID.fromString(id));
        if(config == null) config = configuration.getResourceConfig(name.toLowerCase());

        ResourceInfo info = new ResourceInfo(name,new File("plugins/McNative/lib/resources/"+name.toLowerCase()));
        info.setVersionUrl(replaceLoaderVariables(loader,config,VERSION_URL));
        info.setDownloadUrl(replaceLoaderVariables(loader,config,DOWNLOAD_URL));
        resourceLoader = new ResourceLoader(info);

        if(McNativeConfig.isAvailable()){
            info.setAuthenticator(httpURLConnection -> {
                httpURLConnection.setRequestProperty("networkId",McNativeConfig.getNetworkId());
                httpURLConnection.setRequestProperty("networkSecret",McNativeConfig.getNetworkSecret());
            });
        }else{
            String licenseKey = getLicenseKey(name);
            if(licenseKey != null){
                info.setAuthenticator(httpURLConnection -> {
                    httpURLConnection.setRequestProperty("licenseKey",McNativeConfig.getNetworkId());
                });
            }
        }

        VersionInfo current = resourceLoader.getCurrentVersion();
        VersionInfo latest = VersionInfo.UNKNOWN;

        logger.log(Level.SEVERE,"(Resource-Loader) Server: "+configuration.getEndpoint()+", Qualifier: "+config.getQualifier());
        try{
            latest = resourceLoader.getLatestVersion();
        }catch (Exception exception){
            logger.log(Level.SEVERE,"(McNative-Loader) Could not get latest version");
            logger.log(Level.SEVERE,"(McNative-Loader) Error: "+exception.getMessage());
            if(current == null || current.equals(VersionInfo.UNKNOWN)){
                logger.log(Level.SEVERE,"(Resource-Loader) "+name+" is not available, shutting down");
                return false;
            }
        }

        if(config.isAutomatically()){
            if(latest != null){
                if(current != null && resourceLoader.isLatestVersion()){
                    logger.info("(Resource-Loader) McNative "+latest.getName()+" (Up to date)");
                }else{
                    return download(resourceLoader, current, latest);
                }
            }
        }else{
            VersionInfo version = config.getVersionObject();
            if(current == null || current.equals(VersionInfo.UNKNOWN) || !current.equals(version)){
                if(!download(resourceLoader,current,version)) return false;
            }
            if(resourceLoader.isLatestVersion()) logger.info("(Resource-Loader) McNative "+version.getName()+" (Up to date)");
            else if(latest != null){
                logger.info("(Resource-Loader) automatically updating is disabled");
                logger.info("(Resource-Loader) Latest Version: "+latest.getName());
            }
        }
        return true;
    }

    private String getLicenseKey(String name){
        File location = new File("plugins/"+name+"/license.key");
        return location.exists() ? LoaderUtil.readAllText(location) : null;
    }

    private boolean download(ResourceLoader loader,VersionInfo current,VersionInfo latest){
        logger.info("(McNative-Loader) Downloading McNative "+latest.getName());
        try{
            loader.download(latest);
            logger.info("(McNative-Loader) Successfully downloaded McNative");
        }catch (Exception exception){
            exception.printStackTrace();
            if(current == null || current.equals(VersionInfo.UNKNOWN)){
                logger.info("(McNative-Loader) Download failed, shutting down");
                logger.info("(McNative-Loader) Error: "+exception.getMessage());
                return false;
            }else{
                logger.info("(McNative-Loader) download failed, trying to start an older version");
                logger.info("(McNative-Loader) Error: "+exception.getMessage());
            }
        }
        return true;
    }

    private String replaceLoaderVariables(Properties loaderConfig,ResourceConfig resourceConfig,String input){
        return input
                .replace("{resource.name}",loaderConfig.getProperty("plugin.name"))
                .replace("{resource.id}",loaderConfig.getProperty("plugin.id"))
                .replace("{profile.server}",configuration.getEndpoint())
                .replace("{profile.qualifier}",resourceConfig.getQualifier());
    }

    public String getGuestName(){
        return loaderProperties.getProperty("plugin.name");
    }

    public GuestPluginLoader getLoader() {
        return loader;
    }

    public void loadGuestPlugin(){
        loader.handlePluginLoad();
    }

    public void enableGuestPlugin(){
        loader.handlePluginEnable();
    }

    public void disableGuestPlugin(){
        loader.handlePluginDisable();
    }

    private boolean isMcNativePlugin(File location){
        if(multiple) return false;
        try {
            try (InputStream fileInput = Files.newInputStream(location.toPath()); ZipInputStream input = new ZipInputStream(fileInput)) {
                ZipEntry entry = input.getNextEntry();
                while (entry != null) {
                    if (entry.getName().equals("mcnative.json")) return true;
                    entry = input.getNextEntry();
                }
                return false;
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

}
