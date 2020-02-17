/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 09.02.20, 12:28
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

package org.mcnative.bukkit.plugin.command;

import net.prematic.libraries.command.command.Command;
import net.prematic.libraries.command.command.NotFoundHandler;
import net.prematic.libraries.command.manager.CommandManager;
import net.prematic.libraries.command.sender.CommandSender;
import net.prematic.libraries.utility.interfaces.ObjectOwner;

import java.util.List;

public class BukkitCommandManager implements CommandManager {

    @Override
    public Command getCommand(String name) {
        return null;
    }

    @Override
    public List<Command> getCommands() {
        return null;
    }

    @Override
    public void setNotFoundHandler(NotFoundHandler notFoundHandler) {

    }

    @Override
    public void dispatchCommand(CommandSender commandSender, String s) {

    }

    @Override
    public void registerCommand(Command command) {

    }

    @Override
    public void unregisterCommand(String s) {

    }

    @Override
    public void unregisterCommand(Command command) {

    }

    @Override
    public void unregisterCommand(ObjectOwner objectOwner) {

    }

    @Override
    public void unregisterCommands() {

    }
}