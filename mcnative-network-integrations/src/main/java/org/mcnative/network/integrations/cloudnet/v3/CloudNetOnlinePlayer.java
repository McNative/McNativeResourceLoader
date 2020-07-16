/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 16.07.20, 12:25
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

package org.mcnative.network.integrations.cloudnet.v3;

import de.dytanic.cloudnet.driver.network.HostAndPort;
import de.dytanic.cloudnet.ext.bridge.player.ICloudPlayer;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import org.mcnative.common.McNative;
import org.mcnative.common.network.component.server.MinecraftServer;
import org.mcnative.common.network.component.server.ProxyServer;
import org.mcnative.common.network.component.server.ServerConnectReason;
import org.mcnative.common.network.component.server.ServerConnectResult;
import org.mcnative.common.player.DeviceInfo;
import org.mcnative.common.player.OfflineMinecraftPlayer;
import org.mcnative.common.player.OnlineMinecraftPlayer;
import org.mcnative.common.player.Title;
import org.mcnative.common.player.chat.ChatPosition;
import org.mcnative.common.player.sound.Instrument;
import org.mcnative.common.player.sound.Note;
import org.mcnative.common.player.sound.Sound;
import org.mcnative.common.player.sound.SoundCategory;
import org.mcnative.common.protocol.packet.MinecraftPacket;
import org.mcnative.common.text.components.MessageComponent;
import org.mcnative.network.integrations.McNativePlayerExecutor;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CloudNetOnlinePlayer extends OfflineMinecraftPlayer implements OnlineMinecraftPlayer {

    private final ICloudPlayer player;

    public CloudNetOnlinePlayer(ICloudPlayer player) {
        super(null);
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public long getXBoxId() {
        return Long.parseLong(player.getXBoxId());
    }

    @Override
    public long getFirstPlayed() {
        return player.getFirstLoginTimeMillis();
    }

    @Override
    public long getLastPlayed() {
        return player.getLastLoginTimeMillis();
    }

    @Override
    public InetSocketAddress getAddress() {
        HostAndPort info = player.getLastNetworkConnectionInfo().getAddress();
        return new InetSocketAddress(info.getHost(),info.getPort());
    }

    @Override
    public DeviceInfo getDevice() {
        return DeviceInfo.JAVA;
    }

    @Override
    public boolean isOnlineMode() {
        return player.getLastNetworkConnectionInfo().isOnlineMode();
    }

    @Override
    public int getPing() {
        return McNativePlayerExecutor.getPing(player.getUniqueId());
    }

    @Override
    public CompletableFuture<Integer> getPingAsync() {
        return McNativePlayerExecutor.getPingAsync(player.getUniqueId());
    }

    @Override
    public ProxyServer getProxy() {
        UUID uniqueId = player.getLoginService().getUniqueId();
        return McNative.getInstance().getNetwork().getProxy(uniqueId);
    }

    @Override
    public MinecraftServer getServer() {
        UUID uniqueId = player.getConnectedService().getUniqueId();
        return McNative.getInstance().getNetwork().getServer(uniqueId);
    }

    @Override
    public void connect(MinecraftServer target, ServerConnectReason reason) {
        McNativePlayerExecutor.connect(player.getUniqueId(),target,reason);
    }

    @Override
    public CompletableFuture<ServerConnectResult> connectAsync(MinecraftServer target, ServerConnectReason reason) {
        return McNativePlayerExecutor.connectAsync(player.getUniqueId(),target,reason);
    }

    @Override
    public void kick(MessageComponent<?> message, VariableSet variables) {
        McNativePlayerExecutor.kick(player.getUniqueId(),message,variables);
    }

    @Override
    public void performCommand(String command) {
        McNativePlayerExecutor.performCommand(player.getUniqueId(),command);
    }

    @Override
    public void chat(String message) {
        McNativePlayerExecutor.chat(player.getUniqueId(),message);
    }

    @Override
    public void sendMessage(ChatPosition position, MessageComponent<?> component, VariableSet variables) {
        McNativePlayerExecutor.sendMessage(player.getUniqueId(),position,component,variables);
    }

    @Override
    public void sendActionbar(MessageComponent<?> message, VariableSet variables) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendActionbar(MessageComponent<?> message, VariableSet variables, long staySeconds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendTitle(Title title) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetTitle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendPacket(MinecraftPacket packet) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void playNote(Instrument instrument, Note note) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void playSound(Sound sound, SoundCategory category, float volume, float pitch) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stopSound(Sound sound) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stopSound(String sound, SoundCategory category) {
        throw new UnsupportedOperationException();
    }
}