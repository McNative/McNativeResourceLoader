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
import org.mcnative.loader.rollout.RolloutProfile;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
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
    private final RolloutProfile profile;

    static {
        PLATFORM_IDS.put("bukkit","e5b65750-4dcc-4631-b275-06113b31a416");
        PLATFORM_IDS.put("bungeecord","22ce19f7-9ae8-45ef-9c09-659fc4c9a841");
    }

    public McNativeLoader(Logger logger, String platform, RolloutProfile profile) {
        super(MCNATIVE);
        this.logger = logger;
        this.platform = platform;
        this.profile = profile;

        McNativeConfigAdapter.load();
        if(McNativeConfigAdapter.getId() != null){
            MCNATIVE.setAuthenticator(httpURLConnection -> {
                httpURLConnection.setRequestProperty("serverName","");
                httpURLConnection.setRequestProperty("serverId",McNativeConfigAdapter.getId());
                httpURLConnection.setRequestProperty("serverSecret",McNativeConfigAdapter.getSecret());
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

        MCNATIVE.setVersionUrl(VERSION_URL
                .replace("{profile.server}",profile.getServer())
                .replace("{profile.qualifier}",profile.getQualifier())
                .replace("{resource}",id));

        logger.log(Level.INFO,"(McNative-Loader) Server: "+profile.getServer()+", Qualifier: "+profile.getQualifier());
        try{
            latest = getLatestVersion();
        }catch (Exception exception){
            logger.log(Level.SEVERE,"(McNative-Loader) "+exception.getMessage());
            if(current == null || current.equals(VersionInfo.UNKNOWN)){
                logger.log(Level.SEVERE,"(McNative-Loader) McNative is not available, shutting down");
                return false;
            }
        }

        if(latest != null){
            if(isLatestVersion()){
                logger.info("(McNative-Loader) McNative "+latest.getName()+" (Up to date)");
            }else{
                if(current == null || profile.isAutomatically()){

                    MCNATIVE.setDownloadUrl(DOWNLOAD_URL
                            .replace("{profile.server}",profile.getServer())
                            .replace("{profile.qualifier}",profile.getQualifier())
                            .replace("{resource}",id));

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
                }else{
                    logger.info("(McNative-Loader) automatically updating is disabled");
                    logger.info("(McNative-Loader) Latest Version: "+latest.getName());
                }
            }
        }
        return launch();
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

    public static boolean install(Logger logger,String platform, RolloutProfile profile){
        return new McNativeLoader(logger,platform,profile).install();
    }
}
