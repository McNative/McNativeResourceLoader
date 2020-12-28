/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 29.12.19, 18:56
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

import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.pretronic.libraries.command.manager.CommandManager;
import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.event.EventBus;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import org.mcnative.bungeecord.player.BungeeCordPlayerManager;
import org.mcnative.bungeecord.server.BungeeCordServerMap;
import org.mcnative.common.LocalService;
import org.mcnative.common.McNative;
import org.mcnative.common.network.NetworkIdentifier;
import org.mcnative.common.network.component.server.MinecraftServer;
import org.mcnative.common.network.component.server.MinecraftServerType;
import org.mcnative.common.network.component.server.ProxyServer;
import org.mcnative.common.network.component.server.ServerStatusResponse;
import org.mcnative.common.network.messaging.Messenger;
import org.mcnative.common.player.ConnectedMinecraftPlayer;
import org.mcnative.common.player.OnlineMinecraftPlayer;
import org.mcnative.common.player.chat.ChatChannel;
import org.mcnative.common.player.tablist.Tablist;
import org.mcnative.common.protocol.packet.MinecraftPacket;
import org.mcnative.common.protocol.packet.PacketManager;
import org.mcnative.common.text.components.MessageComponent;
import org.mcnative.proxy.ProxyService;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/*
@Todo In service
    - Implement default tablist
 */
public class BungeeCordService implements LocalService, ProxyServer, ProxyService {

    private final PacketManager packetManager;
    private final CommandManager commandManager;
    private final BungeeCordPlayerManager playerManager;
    private final EventBus eventBus;
    private final BungeeCordServerMap serverMap;

    private ChatChannel serverChat;
    private Tablist defaultTablist;
    private ServerStatusResponse statusResponse;

    public BungeeCordService(PacketManager packetManager, CommandManager commandManager,BungeeCordPlayerManager playerManager
            ,EventBus eventBus,BungeeCordServerMap serverMap) {
        this.packetManager = packetManager;
        this.commandManager = commandManager;
        this.playerManager = playerManager;
        this.eventBus = eventBus;
        this.serverMap = serverMap;
    }

    @Override
    public Collection<MinecraftServer> getServers() {
        return serverMap.getServers();
    }

    @Override
    public Collection<MinecraftServer> getServers(MinecraftServerType type) {
        return serverMap.getServers(type);
    }

    @Override
    public List<MinecraftServer> getServersByPriority() {
        Collection<MinecraftServer> servers = getServers();
        List<MinecraftServer> result = servers instanceof List ? (List<MinecraftServer>) servers : new ArrayList<>(servers);
        result.sort(Comparator.comparingInt(o -> o.getType().ordinal()));
        return result;
    }

    @Override
    public MinecraftServer getServer(String name) {
        return serverMap.getServer(name);
    }

    @Override
    public MinecraftServer getServer(UUID uniqueId) {
        return serverMap.getServer(uniqueId);
    }

    @Override
    public MinecraftServer getServer(InetSocketAddress address) {
        return serverMap.getServer(address);
    }

    @Override
    public MinecraftServer registerServer(String name, InetSocketAddress address) {
        Validate.notNull(name,address);
        ServerInfo info = net.md_5.bungee.api.ProxyServer.getInstance().constructServerInfo(name,address,"",false);
        this.serverMap.put(info.getName(),info);
        return serverMap.getMappedServer(info);
    }

    @Override
    public Collection<ConnectedMinecraftPlayer> getConnectedPlayers() {
        return playerManager.getConnectedPlayers();
    }

    @Override
    public ConnectedMinecraftPlayer getConnectedPlayer(int id) {
        return playerManager.getConnectedPlayer(id);
    }

    @Override
    public ConnectedMinecraftPlayer getConnectedPlayer(UUID uniqueId) {
        return playerManager.getConnectedPlayer(uniqueId);
    }

    @Override
    public ConnectedMinecraftPlayer getConnectedPlayer(String name) {
        return playerManager.getConnectedPlayer(name);
    }

    @Override
    public ConnectedMinecraftPlayer getConnectedPlayer(long xBoxId) {
        return playerManager.getConnectedPlayer(xBoxId);
    }

    @Override
    public PacketManager getPacketManager() {
        return packetManager;
    }

    @Override
    public ChatChannel getServerChat() {
        return serverChat;
    }

    @Override
    public void setServerChat(ChatChannel channel) {
        Validate.notNull(channel);
        this.serverChat = channel;
    }

    @Override
    public Tablist getServerTablist() {
        return defaultTablist;
    }

    @Override
    public void setServerTablist(Tablist tablist) {
        Validate.notNull(tablist);
        this.defaultTablist = tablist;
    }


    @Override
    public ServerStatusResponse getStatusResponse() {
        return statusResponse;
    }

    @Override
    public void setStatusResponse(ServerStatusResponse status) {
        Validate.notNull(status);
        this.statusResponse = status;
    }

    @Override
    public InetSocketAddress getAddress() {
        for (ListenerInfo listener : net.md_5.bungee.api.ProxyServer.getInstance().getConfigurationAdapter().getListeners()){
            return (InetSocketAddress) listener.getSocketAddress();
        }
        return null;
    }

    @Override
    public int getOnlineCount() {
        return net.md_5.bungee.api.ProxyServer.getInstance().getOnlineCount();
    }

    @Override
    public Collection<OnlineMinecraftPlayer> getOnlinePlayers() {
        return Iterators.map(playerManager.getConnectedPlayers(), player -> player);
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(UUID uniqueId) {
        return getConnectedPlayer(uniqueId);
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(String name) {
        return getConnectedPlayer(name);
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer(long xBoxId) {
        return getConnectedPlayer(xBoxId);
    }

    @Override
    public void broadcast(MessageComponent<?> component, VariableSet variables) {
        Validate.notNull(component,variables);
        getConnectedPlayers().forEach(player -> player.sendMessage(component,variables));
    }

    @Override
    public void broadcast(String permission, MessageComponent<?> component, VariableSet variables) {
        Validate.notNull(permission,component,variables);
        getConnectedPlayers().forEach(player -> {
            if(player.hasPermission(permission)){
                player.sendMessage(component, variables);
            }
        });
    }

    @Override
    public void broadcastPacket(MinecraftPacket packet) {
        Validate.notNull(packet);
        getConnectedPlayers().forEach(player -> player.sendPacket(packet));
    }

    @Override
    public void broadcastPacket(MinecraftPacket packet, String permission) {
        Validate.notNull(packet,permission);
        getConnectedPlayers().forEach(player -> {
            if(player.hasPermission(permission)){
                player.sendPacket(packet);
            }
        });
    }

    @Override
    public void kickAll(MessageComponent<?> component, VariableSet variables) {
        Validate.notNull(component,variables);
        getConnectedPlayers().forEach(player -> player.disconnect(component, variables));
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public CommandManager getCommandManager() {
        return commandManager;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public String getName() {
        if(McNative.getInstance().isNetworkAvailable()){
            return McNative.getInstance().getNetwork().getLocalIdentifier().getName();
        }
        return net.md_5.bungee.api.ProxyServer.getInstance().getName();
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public int getMaxPlayerCount() {
        ListenerInfo info = net.md_5.bungee.api.ProxyServer.getInstance().getConfigurationAdapter().getListeners().iterator().next();
        return info.getMaxPlayers();
    }

    @Override
    public NetworkIdentifier getIdentifier() {
        return McNative.getInstance().getNetwork().getLocalIdentifier();
    }

    @Override
    public void sendMessage(String channel, Document request) {
        McNative.getInstance().getRegistry().getService(Messenger.class).sendMessage(this,channel,request);
    }

    @Override
    public CompletableFuture<Document> sendQueryMessageAsync(String channel, Document request) {
        return McNative.getInstance().getRegistry().getService(Messenger.class).sendQueryMessageAsync(this,channel,request);
    }
}
