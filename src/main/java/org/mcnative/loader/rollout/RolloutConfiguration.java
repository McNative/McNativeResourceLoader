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

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;

public class RolloutConfiguration {

    public static File FILE = new File("plugins/McNative/update.yml");
    public static Yaml YAML;

    static {
        DumperOptions dumper = new DumperOptions();
        dumper.setIndent(2);
        dumper.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumper.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        dumper.setPrettyFlow(true);
        CustomClassLoaderConstructor constructor = new CustomClassLoaderConstructor(RolloutConfiguration.class,RolloutConfiguration.class.getClassLoader());

        TypeDescription configDesc = new TypeDescription(RolloutConfiguration.class);
        configDesc.putMapPropertyType("profiles",String.class,RolloutProfile.class);//In old yaml versions new method not available
        configDesc.putListPropertyType("plugins",PluginEntry.class);
        constructor.addTypeDescription(configDesc);

        Representer representer = new Representer();
        representer.getPropertyUtils().setAllowReadOnlyProperties(true);
        representer.getPropertyUtils().setSkipMissingProperties(true);

        representer.addClassTag(RolloutProfile.class, Tag.MAP);
        representer.addClassTag(PluginEntry.class, Tag.MAP);

        YAML = new Yaml(constructor,representer,dumper);
    }

    private Map<String,RolloutProfile> profiles;
    private Collection<PluginEntry> plugins;

    public RolloutConfiguration() {
        profiles = new HashMap<>();
        plugins = new ArrayList<>();
    }

    public Map<String,RolloutProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Map<String, RolloutProfile> profiles) {
        this.profiles = profiles;
    }

    public Collection<PluginEntry> getPlugins() {
        return plugins;
    }

    public void setPlugins(Collection<PluginEntry> plugins) {
        this.plugins = plugins;
    }

    public RolloutProfile getProfile(String name){
        for (PluginEntry plugin : plugins) {
            if(plugin.pluginName.equalsIgnoreCase(name)){
                RolloutProfile profile = profiles.get(plugin.profile);
                if(profile != null) return profile;
                break;
            }
        }

        if(profiles.isEmpty()) profiles.put("production",RolloutProfile.DEFAULT);
        plugins.add(new PluginEntry(name,"production"));

        return RolloutProfile.DEFAULT;
    }

    public static RolloutConfiguration load() throws Exception{
        if(FILE.exists()) return YAML.loadAs(new FileInputStream(FILE),RolloutConfiguration.class);
        else return new RolloutConfiguration();
    }

    public static void save(RolloutConfiguration configuration) {
        try{
            FILE.getParentFile().mkdirs();
            if(!FILE.exists()) FILE.createNewFile();

            Map<String,Object> output = new LinkedHashMap<>();
            output.put("profiles",configuration.getProfiles());
            output.put("plugins",configuration.getPlugins());

            YAML.dump(output,new FileWriter(FILE));
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    public static class PluginEntry {

        private String pluginName;
        private String profile;

        public PluginEntry() {}

        public PluginEntry(String pluginName, String profile) {
            this.pluginName = pluginName;
            this.profile = profile;
        }

        public String getPluginName() {
            return pluginName;
        }

        public String getProfile() {
            return profile;
        }

        public void setPluginName(String pluginName) {
            this.pluginName = pluginName;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }
    }
}
