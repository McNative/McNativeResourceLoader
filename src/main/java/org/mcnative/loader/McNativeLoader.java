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

import net.pretronic.libraries.resourceloader.ResourceInfo;
import net.pretronic.libraries.resourceloader.ResourceLoader;
import net.pretronic.libraries.resourceloader.VersionInfo;
import org.mcnative.loader.config.LoaderConfiguration;
import org.mcnative.loader.config.McNativeConfig;
import org.mcnative.loader.config.ResourceConfig;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class McNativeLoader extends ResourceLoader {

    public static String RESOURCE_NAME = "McNative";

    private static final Map<String,String> PLATFORM_IDS = new HashMap<>();

    private static final String VERSION_URL = "https://{profile.server}/v1/{resource}/versions/latest?plain=true&qualifier={profile.qualifier}";
    private static final String DOWNLOAD_URL = "https://{profile.server}/v1/{resource}/versions/{version.build}/download";

    private static final ResourceInfo MCNATIVE = new ResourceInfo(RESOURCE_NAME,new File("plugins/McNative/lib/resources/mcnative/"));

    private final Logger logger;
    private final String platform;
    private final LoaderConfiguration configuration;

    static {
        PLATFORM_IDS.put("bukkit","d8b38fc3-189a-488e-aabc-87570e15646c");
        PLATFORM_IDS.put("bungeecord","22ce19f7-9ae8-45ef-9c09-659fc4c9a841");
    }

    public McNativeLoader(Logger logger, String platform, LoaderConfiguration configuration) {
        super(MCNATIVE);
        this.logger = logger;
        this.platform = platform;
        this.configuration = configuration;

        if(McNativeConfig.isAvailable()){
            MCNATIVE.setAuthenticator(httpURLConnection -> {
                httpURLConnection.setRequestProperty("networkId", McNativeConfig.getNetworkId());
                httpURLConnection.setRequestProperty("networkSecret", McNativeConfig.getNetworkSecret());
            });
        }
    }

    public boolean isAvailable(){
        try{
            Method availableMethod = Class.forName("org.mcnative.runtime.api.McNative").getMethod("isAvailable");
            return (boolean) availableMethod.invoke(null);
        }catch (Exception ignored){}
        return false;
    }

    public boolean install(){
        if(isAvailable()) return true;
        VersionInfo current = getCurrentVersion();
        VersionInfo latest = null;

        String id = PLATFORM_IDS.get(platform.toLowerCase());
        if(id == null) throw new UnsupportedOperationException("Platform "+platform+" is not supported");

        ResourceConfig config = configuration.getResourceConfig(UUID.fromString(id));
        if(config == null) config = configuration.getResourceConfig("mcnative");

        MCNATIVE.setVersionUrl(VERSION_URL
                .replace("{profile.server}",configuration.getEndpoint())
                .replace("{profile.qualifier}",config.getQualifier())
                .replace("{resource}",id));

        MCNATIVE.setDownloadUrl(DOWNLOAD_URL
                .replace("{profile.server}",configuration.getEndpoint())
                .replace("{profile.qualifier}",config.getQualifier())
                .replace("{resource}",id));

        logger.log(Level.INFO,"(McNative-Loader) Server: "+configuration.getEndpoint()+", Qualifier: "+config.getQualifier());

        try{
            latest = getLatestVersion();
        }catch (Exception exception){
            logger.log(Level.SEVERE,"(McNative-Loader) Could not get latest version ");
            logger.log(Level.SEVERE,"(McNative-Loader) Error: "+exception.getMessage());
            if(current == null || current.equals(VersionInfo.UNKNOWN)){
                logger.log(Level.SEVERE,"(McNative-Loader) McNative is not available, shutting down");
                return false;
            }
        }

        if(config.isAutomatically()){
            if(latest != null){
                if(current != null && isLatestVersion()){
                    logger.info("(McNative-Loader) McNative "+latest.getName()+" (Up to date)");
                }else if(!download(current,latest)) return false;
            }
        }else{
            VersionInfo version = config.getVersionObject();
            if(current == null || current.equals(VersionInfo.UNKNOWN) || !current.equals(version)){
                if(!download(current,version)) return false;
            }
            if(isLatestVersion()) logger.info("(McNative-Loader) McNative "+version.getName()+" (Up to date)");
            else if(latest != null){
                logger.info("(McNative-Loader) automatically updating is disabled");
                logger.info("(McNative-Loader) Latest Version: "+latest.getName());
            }
        }

        return launch();
    }

    private boolean download(VersionInfo current,VersionInfo latest){
        logger.info("(McNative-Loader) Downloading McNative "+latest.getName());
        try{
            download(latest);
            logger.info("(McNative-Loader) Successfully downloaded McNative");
        }catch (Exception exception){
            exception.printStackTrace();
            if(current == null || current.equals(VersionInfo.UNKNOWN)){
                exception.printStackTrace();
                logger.log(Level.SEVERE,"(McNative-Loader) download failed, shutting down",exception);
                return false;
            }else{
                logger.info("(McNative-Loader) download failed, trying to start an older version");
            }
        }
        return true;
    }

    public boolean launch(){
        try{
            loadReflected((URLClassLoader) getClass().getClassLoader());
            Class<?> mcNativeClass = getClass().getClassLoader().loadClass("org.mcnative.runtime."+this.platform.toLowerCase()+".McNativeLauncher");
            Method launchMethod = mcNativeClass.getMethod("launchMcNative");
            launchMethod.invoke(null);
            return true;
        }catch (Exception exception){
            logger.log(Level.SEVERE,"Could not launch McNative.",exception);
        }
        return false;
    }

    public static boolean install(Logger logger, String platform, LoaderConfiguration configuration){
        return new McNativeLoader(logger,platform,configuration).install();
    }

}
