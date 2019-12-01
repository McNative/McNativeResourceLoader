/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 01.12.19, 16:20
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

package org.mcnative.bungeecord.plugin.command;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class MappedCommandMap implements Multimap<Plugin, net.md_5.bungee.api.plugin.Command>{

    private final Multimap<Plugin, net.md_5.bungee.api.plugin.Command> original;

    @Override
    public int size() {
        return original.size();
    }

    @Override
    public boolean isEmpty() {
        return original.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return original.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return original.containsValue(o);
    }

    @Override
    public boolean containsEntry(Object o, Object o1) {
        return original.containsEntry(o,o1);
    }

    @Override
    public boolean put(Plugin plugin, Command command) {
        return original.put(plugin, command);
    }

    @Override
    public boolean remove(Object o, Object o1) {
        return original.remove(o, o1);
    }

    @Override
    public boolean putAll(Plugin plugin, Iterable<? extends Command> iterable) {
        return original.putAll(plugin,iterable);
    }

    @Override
    public boolean putAll(Multimap<? extends Plugin, ? extends Command> multimap) {
        return original.putAll(multimap);
    }

    @Override
    public Collection<Command> replaceValues(Plugin plugin, Iterable<? extends Command> iterable) {
        return original.replaceValues(plugin, iterable);
    }

    @Override
    public Collection<Command> removeAll(Object o) {
        return original.removeAll(o);
    }

    @Override
    public void clear() {
        original.clear();
    }

    @Override
    public Collection<Command> get(Plugin plugin) {
        return original.get(plugin);
    }

    @Override
    public Set<Plugin> keySet() {
        return original.keySet();
    }

    @Override
    public Multiset<Plugin> keys() {
        return original.keys();
    }

    @Override
    public Collection<Command> values() {
        return original.values();
    }

    @Override
    public Collection<Map.Entry<Plugin, Command>> entries() {
        return original.entries();
    }

    @Override
    public Map<Plugin, Collection<Command>> asMap() {
        return original.asMap();
    }

    @Override
    public boolean equals(Object o) {
        return original.equals(o);
    }

    @Override
    public int hashCode() {
        return original.hashCode();
    }
}
