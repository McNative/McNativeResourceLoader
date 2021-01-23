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

package org.mcnative.loader.config;

import net.pretronic.libraries.resourceloader.VersionInfo;

import java.util.UUID;

public class ResourceConfig {

    private transient String name;
    private transient UUID id;
    private String qualifier;
    private String version;

    public ResourceConfig(){}

    public ResourceConfig(String name, UUID id, String qualifier, String version) {
        this.name = name;
        this.id = id;
        this.qualifier = qualifier;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    public String getQualifier() {
        return qualifier;
    }

    public String getVersion() {
        return version;
    }

    public VersionInfo getVersionObject() {
        return VersionInfo.parse(getVersion());
    }

    public boolean isAutomatically(){
        return getVersion().equalsIgnoreCase("LATEST");
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
