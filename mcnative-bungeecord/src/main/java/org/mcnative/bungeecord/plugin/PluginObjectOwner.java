/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 17.11.19, 14:42
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
import net.prematic.libraries.utility.interfaces.ObjectOwner;

public class PluginObjectOwner implements ObjectOwner {

    private final Plugin plugin;

    public PluginObjectOwner(Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public String getName() {
        return plugin.getDescription().getName();
    }

    @Override
    public int hashCode() {
        return plugin.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj instanceof PluginObjectOwner) return plugin.equals(((PluginObjectOwner) obj).getPlugin());
        return plugin.equals(obj);
    }

    @Override
    public String toString() {
        return plugin.toString();
    }
}
