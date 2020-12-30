/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 25.07.20, 12:24
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

package org.mcnative.loader.rollout;

public class RolloutProfile {

    public static final RolloutProfile DEFAULT = new RolloutProfile("mirror.mcnative.org","RELEASE",true);

    private boolean automatically;
    private String qualifier;
    private String server;

    public RolloutProfile() {}

    public RolloutProfile(String server, String qualifier, boolean automatically) {
        this.server = server;
        this.qualifier = qualifier;
        this.automatically = automatically;
    }

    public String getServer() {
        return server;
    }

    public String getQualifier() {
        return qualifier;
    }

    public boolean isAutomatically() {
        return automatically;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public void setAutomatically(boolean automatically) {
        this.automatically = automatically;
    }
}
