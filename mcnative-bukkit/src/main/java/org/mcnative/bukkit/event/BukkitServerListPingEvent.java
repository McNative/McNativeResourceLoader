/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 17.02.20, 19:57
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

package org.mcnative.bukkit.event;

import org.mcnative.common.event.ServerListPingEvent;
import org.mcnative.common.network.component.server.ServerStatusResponse;

import java.net.InetSocketAddress;

public class BukkitServerListPingEvent implements ServerListPingEvent {
    @Override
    public InetSocketAddress getClientAddress() {
        return null;
    }

    @Override
    public InetSocketAddress getVirtualHost() {
        return null;
    }

    @Override
    public ServerStatusResponse getResponse() {
        return null;
    }

    @Override
    public void setResponse(ServerStatusResponse response) {

    }
}
