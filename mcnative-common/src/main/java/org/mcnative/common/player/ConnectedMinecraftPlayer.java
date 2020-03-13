/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 28.12.19, 22:26
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

package org.mcnative.common.player;

import net.pretronic.libraries.command.sender.CommandSender;
import org.mcnative.common.connection.MinecraftConnection;
import org.mcnative.common.player.bossbar.BossBar;
import org.mcnative.common.player.scoreboard.BelowNameInfo;
import org.mcnative.common.player.scoreboard.Tablist;
import org.mcnative.common.player.scoreboard.sidebar.Sidebar;

import java.util.Collection;

public interface ConnectedMinecraftPlayer extends OnlineMinecraftPlayer, MinecraftConnection, CommandSender {

    Sidebar getSidebar();

    void setSidebar(Sidebar sidebar);


    Tablist getTablist();

    void setTablist(Tablist tablist);


    BelowNameInfo getBelowNameInfo();

    void setBelowNameInfo(BelowNameInfo info);


    Collection<BossBar> getActiveBossBars();

    void addBossBar(BossBar bossBar);

    void removeBossBar(BossBar bossBar);


    void setResourcePack(String url);

    void setResourcePack(String url, String hash);

}
