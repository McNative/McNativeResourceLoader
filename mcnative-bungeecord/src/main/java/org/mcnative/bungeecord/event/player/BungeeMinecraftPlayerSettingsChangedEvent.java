/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 01.05.20, 09:31
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

package org.mcnative.bungeecord.event.player;

import org.mcnative.common.event.player.settings.MinecraftPlayerSettingsChangedEvent;
import org.mcnative.common.player.ConnectedMinecraftPlayer;
import org.mcnative.common.player.MinecraftPlayer;
import org.mcnative.common.player.OnlineMinecraftPlayer;
import org.mcnative.common.player.PlayerClientSettings;

public class BungeeMinecraftPlayerSettingsChangedEvent implements MinecraftPlayerSettingsChangedEvent {

    private final ConnectedMinecraftPlayer player;
    private final PlayerClientSettings newSettings;

    public BungeeMinecraftPlayerSettingsChangedEvent(ConnectedMinecraftPlayer player, PlayerClientSettings newSettings) {
        this.player = player;
        this.newSettings = newSettings;
    }

    @Override
    public PlayerClientSettings getOldSettings() {
        return player.getClientSettings();
    }

    @Override
    public PlayerClientSettings getNewSettings() {
        return newSettings;
    }

    @Override
    public OnlineMinecraftPlayer getOnlinePlayer() {
        return player;
    }

    @Override
    public MinecraftPlayer getPlayer() {
        return player;
    }
}
