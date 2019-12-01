/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 26.11.19, 19:46
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

package org.mcnative.bungeecord.plugin;

import net.md_5.bungee.api.plugin.Plugin;
import net.prematic.libraries.document.Document;
import net.prematic.libraries.document.EmptyDocument;
import net.prematic.libraries.logging.bridge.JdkPrematicLogger;
import net.prematic.libraries.plugin.description.PluginDescription;
import net.prematic.libraries.plugin.description.PluginVersion;
import net.prematic.libraries.plugin.description.dependency.Dependency;
import net.prematic.libraries.plugin.description.mainclass.MainClass;
import net.prematic.libraries.plugin.description.mainclass.SingleMainClass;
import org.mcnative.common.McNative;
import org.mcnative.common.plugin.MinecraftPlugin;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class MappedPlugin extends MinecraftPlugin {

    private final Plugin plugin;

    public MappedPlugin(Plugin plugin) {
        this.plugin = plugin;
        initialize(new Description(plugin.getDescription()),null
                ,new JdkPrematicLogger(plugin.getLogger()), McNative.getInstance());
    }

    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public int hashCode() {
        return plugin.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        else if(obj instanceof MappedPlugin) return plugin.equals(((MappedPlugin) obj).getPlugin());
        else return plugin.equals(obj);
    }

    @Override
    public String toString() {
        return plugin.toString();
    }

    public static class Description implements PluginDescription {

        private final net.md_5.bungee.api.plugin.PluginDescription original;

        public Description(net.md_5.bungee.api.plugin.PluginDescription original) {
            this.original = original;
        }

        @Override
        public String getName() {
            return original.getName();
        }

        @Override
        public String getCategory() {
            return "General";
        }

        @Override
        public String getDescription() {
            return original.getDescription();
        }

        @Override
        public String getAuthor() {
            return original.getAuthor();
        }

        @Override
        public String getWebsite() {
            return null;
        }

        @Override
        public UUID getId() {
            return null;
        }

        @Override
        public PluginVersion getVersion() {
            return new PluginVersion(original.getVersion(),1);
        }

        @Override
        public MainClass getMain() {
            return new SingleMainClass(original.getMain());
        }

        @Override
        public Collection<Dependency> getDependencies() {
            return Collections.emptyList();//@Todo implement
        }

        @Override
        public Collection<String> getProviders() {
            return Collections.emptyList();
        }

        @Override
        public Document getProperties() {
            return EmptyDocument.newDocument();
        }
    }
}
