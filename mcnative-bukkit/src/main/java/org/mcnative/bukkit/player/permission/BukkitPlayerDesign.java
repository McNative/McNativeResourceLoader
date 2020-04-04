/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 18.03.20, 17:57
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

package org.mcnative.bukkit.player.permission;

import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.message.bml.variable.VariableSet;
import org.bukkit.entity.Player;
import org.mcnative.bukkit.McNativeBukkitConfiguration;
import org.mcnative.common.player.PlayerDesign;

import java.util.Map;

public class BukkitPlayerDesign implements PlayerDesign {

    private final Player player;

    public BukkitPlayerDesign(Player player) {
        this.player = player;
    }

    @Override
    public String getColor() {
        for (Map.Entry<String, String> entry : McNativeBukkitConfiguration.PLAYER_COLORS_COLORS.entrySet()) {
            if(player.hasPermission(entry.getKey())){
                return entry.getValue();
            }
        }
        return McNativeBukkitConfiguration.PLAYER_COLORS_DEFAULT;
    }

    @Override
    public String getPrefix() {
        return "";//Unsupported without exception
    }

    @Override
    public String getSuffix() {
        return "";//Unsupported without exception
    }

    @Override
    public String getChat() {
        return "";//Unsupported without exception
    }

    @Override
    public String getDisplayName() {
        VariableSet variables = VariableSet.create().add("color",getColor());
        return VariableSet.replace(McNativeBukkitConfiguration.PLAYER_DISPLAY_NAME_FORMAT,variables);
    }

    @Override
    public int getPriority() {
        return 0;//Unsupported without exception
    }

    @Override
    public String toJson() {
        Document result = Document.newDocument();
        result.set("color",getColor());
        result.set("displayName",getDisplayName());
        result.set("prefix","").set("suffix","").set("chat","").set("priority",0);
        return DocumentFileType.JSON.getWriter().write(result,false);
    }
}