/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 24.09.19, 20:24
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

package org.mcnative.common.text.components;

import net.prematic.libraries.document.Document;
import org.mcnative.common.text.Text;
import org.mcnative.common.text.event.ClickAction;
import org.mcnative.common.text.event.HoverAction;
import org.mcnative.common.text.event.TextEvent;
import org.mcnative.common.text.format.TextColor;
import org.mcnative.common.text.format.TextStyle;
import org.mcnative.common.text.variable.VariableSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AbstractChatComponent<T extends AbstractChatComponent<?>> implements ChatComponent<T>{

    private TextColor color;
    private Set<TextStyle> styling;

    private TextEvent<ClickAction> clickEvent;
    private TextEvent<HoverAction> hoverEvent;
    private String insertion;

    private Collection<MessageComponent<?>> extras;

    public AbstractChatComponent(){
        this(null);
    }

    public AbstractChatComponent(TextColor color){
        this(color,new HashSet<>());
    }

    public AbstractChatComponent(TextColor color, Set<TextStyle> styling){
        this.color = color;
        this.styling = styling;
        this.extras = new ArrayList<>();
    }

    @Override
    public TextEvent<ClickAction> getClickEvent() {
        return clickEvent;
    }

    @Override
    public TextEvent<HoverAction> getHoverEvent() {
        return hoverEvent;
    }

    @Override
    public T setClickEvent(TextEvent<ClickAction> event) {
        this.clickEvent = event;
        return (T) this;
    }

    @Override
    public T setHoverEvent(TextEvent<HoverAction> event) {
        this.hoverEvent = event;
        return (T) this;
    }

    @Override
    public String getInsertion() {
        return this.insertion;
    }

    @Override
    public T setInsertion(String insertion) {
        this.insertion = insertion;
        return (T) this;
    }

    @Override
    public Collection<MessageComponent<?>> getExtras() {
        return extras;
    }

    @Override
    public T addExtra(MessageComponent<?> component) {
        extras.add(component);
        return (T) this;
    }

    @Override
    public T removeExtra(MessageComponent<?> component) {
        extras.remove(component);
        return (T) this;
    }

    @Override
    public void toPlainText(StringBuilder builder, VariableSet variables) {
        if(extras != null && !extras.isEmpty()){
            extras.forEach(component -> component.toPlainText(builder, variables));
        }
    }

    @Override
    public TextColor getColor() {
        if(color == null) return TextColor.WHITE;
        return color;
    }

    @Override
    public T setColor(TextColor color) {
        this.color = color;
        return (T) this;
    }

    @Override
    public Set<TextStyle> getStyling() {
        return styling;
    }

    @Override
    public T setStyling(Set<TextStyle> styling) {
        this.styling = styling;
        return (T) this;
    }

    @Override
    public Document compile(String key, VariableSet variables) {
        Document document = Document.newDocument(key);
        if(isBold()) document.add("bold",true);
        if(isItalic()) document.add("italic",true);
        if(isUnderlined()) document.add("underlined",true);
        if(isStrikeThrough()) document.add("strikethrough",true);
        if(isObfuscated()) document.add("obfuscated",true);
        if(this.color != null) document.add("color",color.getName());
        if(insertion != null) document.add("insertion",variables.replace(insertion));
        if(this.clickEvent != null){//@Todo Register temp command
            Document event = Document.newDocument();
            if(clickEvent.getAction().isDirectEvent()){
                event.add("action",clickEvent.getAction().getName().toLowerCase());
                event.add("value",clickEvent.getValue().toString());
            }else{
                event.add("action","run_command");
                event.add("value","mcnOnTextClick");//@Todo add custom event managing
            }
            document.add("clickEvent",event);
        }
        if(this.hoverEvent != null){
            Document event = Document.newDocument();
            event.add("action",hoverEvent.getAction().getName().toLowerCase());

            ChatComponent<?> value;
            if(hoverEvent.getValue() instanceof ChatComponent) value = ((ChatComponent<?>) hoverEvent.getValue());
            else value = new TextComponent(hoverEvent.getValue().toString());
            event.add("value",value.compile(variables));

            document.add("hoverEvent",event);
        }
        if(extras != null && !extras.isEmpty()){
            Document[] extras = new Document[this.extras.size()];
            int index = 0;
            for (MessageComponent<?> extra : this.extras) {
                extras[index] = extra.compile(variables);
                index++;
            }
            document.add("extra",extras);
        }
        return document;
    }

    @Override
    public void decompile(Document data) {
        if(data.getBoolean("bold")) setBold(true);
        if(data.getBoolean("italic")) setItalic(true);
        if(data.getBoolean("underline")) setUnderlined(true);
        if(data.getBoolean("strikeThrough")) setStrikeThrough(true);
        if(data.getBoolean("obfuscated")) setObfuscated(true);

        String color = data.getString("color");
        if(color != null) this.color = TextColor.of(color);

        String insertion = data.getString("insertion");
        if(insertion != null) setInsertion(insertion);

        Document clickEvent = data.getDocument("clickEvent");
        if(clickEvent != null){
            this.clickEvent = new TextEvent<>(ClickAction.of(clickEvent.getString("action")),clickEvent.getString("value"));
        }

        Document hoverEvent = data.getDocument("hoverEvent");
        if(hoverEvent != null){
            this.hoverEvent = new TextEvent<>(HoverAction.of(hoverEvent.getString("action")),clickEvent.getString("value"));
        }
        Document extra = data.getDocument("extra");
        if(extra != null){
            this.extras = Text.decompileArray(extra);
        }
    }

    @Override
    public <N extends ChatComponent<?>> N getAs(Class<N> aClass) {
        return (N) this;
    }

    @Override
    public String toString() {
        return toPlainText();
    }
}