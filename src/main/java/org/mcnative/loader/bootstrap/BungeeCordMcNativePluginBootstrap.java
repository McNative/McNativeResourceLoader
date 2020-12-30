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

package org.mcnative.loader.bootstrap;

import net.md_5.bungee.api.plugin.Plugin;
import net.pretronic.libraries.utility.reflect.ReflectionUtil;
import org.mcnative.loader.*;
import org.mcnative.loader.rollout.RolloutConfiguration;
import org.mcnative.loader.rollout.RolloutProfile;

import java.util.logging.Level;

public class BungeeCordMcNativePluginBootstrap extends Plugin implements PlatformExecutor {

    private GuestPluginExecutor executor;

    @Override
    public void onLoad() {
        try{
            RolloutConfiguration configuration = RolloutConfiguration.load();

            RolloutProfile mcnative = configuration.getProfile(McNativeLoader.RESOURCE_NAME);
            RolloutProfile resource = configuration.getProfile(getDescription().getName());

            CertificateValidation.disable();

            if(!McNativeLoader.install(getLogger(), EnvironmentNames.BUNGEECORD, mcnative)) return;
            this.executor = new GuestPluginExecutor(this,getDescription().getFile(),getLogger(),EnvironmentNames.BUNGEECORD,resource);

            if(!this.executor.install()){
                this.executor = null;
                return;
            }

            CertificateValidation.reset();

            this.executor.loadGuestPlugin();

            String version = this.executor.getLoader().getLoadedVersion();
            ReflectionUtil.changeFieldValue(getDescription(),"version",version);

            RolloutConfiguration.save(configuration);
        }catch (Exception exception){
            this.executor = null;
            getLogger().log(Level.SEVERE,String.format("Could not bootstrap plugin (%s)",exception.getMessage()));
            exception.printStackTrace();
            getProxy().getPluginManager().unregisterCommands(this);
            getProxy().getPluginManager().unregisterListeners(this);
        }
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
        return false;
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
