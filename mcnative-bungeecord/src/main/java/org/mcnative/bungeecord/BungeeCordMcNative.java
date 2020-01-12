/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 29.12.19, 19:50
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

package org.mcnative.bungeecord;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.concurrent.TaskScheduler;
import net.prematic.libraries.concurrent.simple.SimpleTaskScheduler;
import net.prematic.libraries.dependency.DependencyManager;
import net.prematic.libraries.logging.PrematicLogger;
import net.prematic.libraries.logging.bridge.JdkPrematicLogger;
import net.prematic.libraries.plugin.manager.PluginManager;
import net.prematic.libraries.plugin.service.ServiceRegistry;
import net.prematic.libraries.utility.Validate;
import org.mcnative.bungeecord.plugin.command.McNativeCommand;
import org.mcnative.bungeecord.server.BungeeCordServerStatusResponse;
import org.mcnative.common.LocalService;
import org.mcnative.common.McNative;
import org.mcnative.common.MinecraftPlatform;
import org.mcnative.common.ObjectCreator;
import org.mcnative.common.network.Network;
import org.mcnative.common.network.component.server.ServerStatusResponse;
import org.mcnative.common.player.PlayerManager;
import org.mcnative.common.player.data.DummyDataProvider;
import org.mcnative.common.player.data.PlayerDataProvider;
import org.mcnative.common.plugin.configuration.ConfigurationProvider;
import org.mcnative.common.plugin.configuration.DefaultConfigurationProvider;

import java.io.File;
import java.util.UUID;

public class BungeeCordMcNative implements McNative {

    private final MinecraftPlatform platform;
    private final PrematicLogger logger;
    private final TaskScheduler scheduler;
    private final CommandSender consoleSender;
    private final ObjectCreator creator;

    private final PluginManager pluginManager;
    private final DependencyManager dependencyManager;
    private final PlayerManager playerManager;
    private final LocalService local;

    private Network network;

    public BungeeCordMcNative(PluginManager pluginManager, PlayerManager playerManager, Network network, LocalService local) {
        this.platform = new BungeeCordPlatform();
        this.logger = new JdkPrematicLogger(ProxyServer.getInstance().getLogger());
        this.scheduler = new SimpleTaskScheduler();
        this.consoleSender = new McNativeCommand.MappedCommandSender(ProxyServer.getInstance().getConsole());
        this.dependencyManager = new DependencyManager(logger,new File("plugins/McNative/lib/dependencies"));
        this.creator = new BungeeObjectCreator();

        this.pluginManager = pluginManager;
        this.playerManager = playerManager;
        this.network = network;
        this.local = local;
    }

    @Override
    public String getServiceName() {
        return ProxyServer.getInstance().getName();
    }

    @Override
    public MinecraftPlatform getPlatform() {
        return platform;
    }

    @Override
    public PrematicLogger getLogger() {
        return logger;
    }

    @Override
    public ServiceRegistry getRegistry() {
        return pluginManager;
    }

    @Override
    public TaskScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public CommandSender getConsoleSender() {
        return consoleSender;
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    @Override
    public ObjectCreator getObjectCreator() {
        return creator;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    public boolean isNetworkAvailable() {
        return true;
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(Network network) {
        Validate.notNull(network);
        this.network = network;
    }

    @Override
    public LocalService getLocal() {
        return local;
    }

    @Override
    public void shutdown() {
        ProxyServer.getInstance().stop();
    }

    public void registerDefaultProviders(){
        pluginManager.registerService(this, ConfigurationProvider.class,new DefaultConfigurationProvider());
        pluginManager.registerService(this, PlayerDataProvider.class,new DummyDataProvider());
    }

    private static class BungeeObjectCreator implements ObjectCreator{

        @Override
        public ServerStatusResponse createServerStatusResponse() {
            return new BungeeCordServerStatusResponse(new ServerPing());
        }

        @Override
        public ServerStatusResponse.PlayerInfo createPlayerInfo(String name) {
            return new BungeeCordServerStatusResponse.DefaultPlayerInfo(name);
        }

        @Override
        public ServerStatusResponse.PlayerInfo createPlayerInfo(UUID uniqueId, String name) {
            return new BungeeCordServerStatusResponse.DefaultPlayerInfo(name,uniqueId);
        }
    }
}
