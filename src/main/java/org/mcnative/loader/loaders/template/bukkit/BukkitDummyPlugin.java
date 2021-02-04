/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 03.04.20, 11:45
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

package org.mcnative.loader.loaders.template.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.*;
import org.mcnative.loader.loaders.GuestPluginLoader;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class BukkitDummyPlugin implements Plugin {

    private final GuestPluginLoader loader;
    private final PluginLoader pluginLoader;
    private final PluginDescriptionFile description;
    private boolean enabled;
    public String name;

    protected BukkitDummyPlugin(GuestPluginLoader loader,String name,String version) {
        this.pluginLoader = new DummyClassLoader();
        this.description = new PluginDescriptionFile(name,version,getClass().getName());
        this.name = name;
        this.loader = loader;
        this.enabled = true;
    }

    @Override
    public File getDataFolder() {
        return new File("plugins/"+name);
    }

    @Override
    public PluginDescriptionFile getDescription() {
        return description;
    }

    @Override
    public FileConfiguration getConfig() {
        throw new UnsupportedOperationException("Bukkit dummy plugin is not able to provide a configuration");
    }

    @Override
    public InputStream getResource(String s) {
        return null;//Unused
    }

    @Override
    public void saveConfig() {
        //unused
    }

    @Override
    public void saveDefaultConfig() {
        //Unused
    }

    @Override
    public void saveResource(String s, boolean b) {
        //Unused
    }

    @Override
    public void reloadConfig() {
        //Unused
    }

    @Override
    public PluginLoader getPluginLoader() {
        return pluginLoader;
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        //Unused
    }

    @Override
    public boolean isNaggable() {
        return false;
    }

    @Override
    public void setNaggable(boolean b) {
        //Unused
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String s, String s1) {
        throw new UnsupportedOperationException("McNative dummy plugin is not able to provide a world generator");
    }

    @Override
    public Logger getLogger() {
        return Bukkit.getLogger();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        return false;      //Unused
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;      //Unused
    }

    public class DummyClassLoader implements PluginLoader {

        @Override
        public Plugin loadPlugin(File file) throws UnknownDependencyException {
            throw new IllegalArgumentException("This class loader is only a dummy class loader and can not be used");
        }

        @Override
        public PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException {
            throw new IllegalArgumentException("This class loader is only a dummy class loader and can not be used");
        }

        @Override
        public Pattern[] getPluginFileFilters() {
            throw new IllegalArgumentException("This class loader is only a dummy class loader and can not be used");
        }

        @Override
        public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin) {
            throw new IllegalArgumentException("This class loader is only a dummy class loader and can not be used");
        }

        @Override
        public void enablePlugin(Plugin plugin) {
            //loader.handlePluginEnable();
        }

        @Override
        public void disablePlugin(Plugin plugin) {
            loader.handlePluginDisable();
        }
    }
}
