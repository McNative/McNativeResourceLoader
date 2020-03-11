/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 09.03.20, 19:14
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

package org.mcnative.common.serviceprovider.placeholder;

import net.pretronic.libraries.utility.Iterators;
import net.pretronic.libraries.utility.Validate;
import net.pretronic.libraries.utility.interfaces.ObjectOwner;
import org.mcnative.common.player.MinecraftPlayer;
import org.mcnative.common.text.components.MessageComponent;
import org.mcnative.common.text.components.TextComponent;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@link McNativePlaceholderProvider} is only a backup provided. If you are using placeholders you
 * should always use a supported plugin like PlaceholderApi.
 *
 * <p>This class contains code from the PlaceholderApi project.</p>
 * <p>https://github.com/PlaceholderAPI/PlaceholderAPI/blob/master/src/main/java/me/clip/placeholderapi/PlaceholderAPI.java</p>
 */
public class McNativePlaceholderProvider implements PlaceholderProvider{

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("[%]([^%]+)[%]");

    private final Collection<Entry> entries;

    public McNativePlaceholderProvider() {
        this.entries = new HashSet<>();
    }

    @Override
    public String getName() {
        return "McNative";
    }

    @Override
    public Collection<String> getIdentifiers() {
        return Iterators.map(this.entries, entry -> entry.identifier);
    }

    @Override
    public boolean hasIdentifier(String identifier) {
        Validate.notNull(identifier);
        return Iterators.findOne(this.entries, entry -> entry.identifier.equalsIgnoreCase(identifier)) != null;
    }

    @Override
    public void registerPlaceHolders(ObjectOwner owner, String identifier, PlaceholderHook hook) {
        Validate.notNull(owner,identifier,hook);
        if(hasIdentifier(identifier)) throw new IllegalArgumentException("The identifier is already registered");
        this.entries.add(new Entry(owner, identifier, hook));
    }

    @Override
    public void unregisterPlaceHolders(String identifier) {
        Validate.notNull(identifier);
        Iterators.removeSilent(this.entries, entry -> entry.identifier.equalsIgnoreCase(identifier));
    }

    @Override
    public void unregisterPlaceHolders(ObjectOwner owner) {
        Validate.notNull(owner);
        Iterators.removeSilent(this.entries, entry -> entry.owner.equals(owner));
    }

    @Override
    public String translate(MinecraftPlayer player, String identifier, String parameter) {
        Entry entry = Iterators.findOne(this.entries, entry1 -> entry1.identifier.equalsIgnoreCase(identifier));
        if(entry != null){
            Object result = entry.hook.onRequest(player,parameter);
            if(result instanceof String){
                return (String) result;
            }else if(result != null){
                return result.toString();
            }else{
                return "NULL";
            }
        }
        return null;
    }


    @Override
    public String replacePlaceholders(MinecraftPlayer player, String input) {
        String output = input;
        Matcher m = PLACEHOLDER_PATTERN.matcher(input);
        while (m.find()) {
            String format = m.group(1);
            int index = format.indexOf("_");

            if (index <= 0 || index >= format.length()) {
                continue;
            }

            String identifier = format.substring(0, index).toLowerCase();
            String params = format.substring(index + 1);

            String value = translate(player,identifier,params);
            output = output.replaceAll(Pattern.quote(m.group()), Matcher.quoteReplacement(value));
        }
        return output;
    }

    @Override
    public void replacePlaceholders(MinecraftPlayer player, MessageComponent<?> rawComponent) {
        if(rawComponent instanceof TextComponent){
            String newTest = replacePlaceholders(player,((TextComponent) rawComponent).getText());
            ((TextComponent) rawComponent).setText(newTest);
        }
        for (MessageComponent<?> extra : rawComponent.getExtras()) {
            replacePlaceholders(player,extra);
        }
    }

    private final static class Entry {

        private final ObjectOwner owner;
        private final String identifier;
        private final PlaceholderHook hook;

        public Entry(ObjectOwner owner, String identifier, PlaceholderHook hook) {
            this.owner = owner;
            this.identifier = identifier;
            this.hook = hook;
        }
    }
}
