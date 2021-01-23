/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 26.04.20, 15:49
 * @web %web%
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

package org.mcnative.loader.loaders.mcnative;

import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.logging.PretronicLogger;
import net.pretronic.libraries.plugin.Plugin;
import net.pretronic.libraries.plugin.RuntimeEnvironment;
import net.pretronic.libraries.plugin.description.DefaultPluginDescription;
import net.pretronic.libraries.plugin.description.PluginDescription;
import net.pretronic.libraries.plugin.loader.DefaultPluginLoader;
import net.pretronic.libraries.plugin.loader.classloader.PluginClassLoader;
import net.pretronic.libraries.plugin.manager.PluginManager;
import org.mcnative.loader.PlatformExecutor;
import org.mcnative.loader.utils.LoaderUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class McNativePluginLoader extends DefaultPluginLoader {

    private final PlatformExecutor executor;
    private String pluginType;

    public McNativePluginLoader(PlatformExecutor executor, PluginManager pluginManager, RuntimeEnvironment<?> environment, PretronicLogger logger, PluginClassLoader classLoader, File location, PluginDescription description, boolean lifecycleLogging) {
        super(pluginManager, environment, logger, classLoader, location, description, lifecycleLogging);
        this.executor = executor;
        if(this.pluginType == null) this.pluginType = "mcnative";
    }

    private RuntimeEnvironment<?> getEnvironment(){
        return LoaderUtil.getFieldValue(DefaultPluginLoader.class,this,"environment",RuntimeEnvironment.class);
    }

    @Override
    public boolean isEnabled() {
        return executor.isEnabled();
    }

    @Override
    public void bootstrap() {
        executor.bootstrap();
    }

    public void bootstrapInternal() {
        super.bootstrap();
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    public void shutdownInternal() {
        super.shutdown();
    }

    @Override
    public void unload() {
        if(isEnabled()) shutdown();
        super.unload();
        executor.unload();
    }

    @Override
    public Plugin<?> enable() {
        return super.enable();
    }

    @Override
    public PluginDescription loadDescription() {
        InputStream stream = getClassLoader().getResourceAsStream("mcnative.json");
        if(stream == null) throw new IllegalArgumentException("No mcnative.json available");
        PluginDescription result = DefaultPluginDescription.create(getPluginManager(), DocumentFileType.JSON.getReader().read(stream));
        try { stream.close(); } catch (IOException ignored) {}
        return result;
    }

}

