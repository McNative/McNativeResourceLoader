/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 15.09.19, 16:20
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

package org.mcnative.common.protocol.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.mcnative.common.McNative;
import org.mcnative.common.connection.MinecraftConnection;
import org.mcnative.common.protocol.Endpoint;
import org.mcnative.common.protocol.MinecraftProtocolUtil;
import org.mcnative.common.protocol.MinecraftProtocolVersion;
import org.mcnative.common.protocol.packet.*;
import org.mcnative.common.protocol.packet.type.MinecraftChatPacket;

import java.util.List;
import java.util.function.Consumer;

//@Todo implement cancellation
public class MinecraftProtocolEncoder extends MessageToByteEncoder<MinecraftPacket> {

    private final PacketManager packetManager;
    private final Endpoint endpoint;
    private final PacketDirection direction;
    private final MinecraftConnection connection;
    private final MinecraftProtocolVersion version;

    public MinecraftProtocolEncoder(PacketManager packetManager, Endpoint endpoint, PacketDirection direction, MinecraftConnection connection) {
        this.packetManager = packetManager;
        this.endpoint = endpoint;
        this.direction = direction;
        this.connection = connection;
        this.version = connection.getProtocolVersion();
    }

    @Override
    protected void encode(ChannelHandlerContext context, MinecraftPacket packet0, ByteBuf buffer){
        MinecraftPacket packet = packet0;
        List<MinecraftPacketListener> listeners = packetManager.getPacketListeners(endpoint,direction,packet.getClass());
        if(listeners != null && !listeners.isEmpty()){
            MinecraftPacketEvent event = new MinecraftPacketEvent(endpoint,direction,version,packet);
            listeners.forEach(listener -> listener.handle(event));
            packet = event.getPacket();
        }
        MinecraftProtocolUtil.writeVarInt(buffer,packet.getIdentifier().getId(direction,connection.getState(),version));
        packet.write(direction,this.version,buffer);
    }
}