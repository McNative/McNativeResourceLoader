package org.mcnative.loader.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoaderConfiguration {

    private static final String LOCAL = "local";
    private static final Yaml YAML;

    static {
        DumperOptions dumper = new DumperOptions();
        dumper.setIndent(2);
        dumper.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumper.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        dumper.setPrettyFlow(true);
        CustomClassLoaderConstructor constructor = new CustomClassLoaderConstructor(LoaderConfiguration.class,LoaderConfiguration.class.getClassLoader());

        TypeDescription configDesc = new TypeDescription(LoaderConfiguration.class);
        configDesc.putMapPropertyType("localProfile",String.class, ResourceConfig.class);//In old yaml versions new method not available
        constructor.addTypeDescription(configDesc);

        Representer representer = new Representer();
        representer.getPropertyUtils().setAllowReadOnlyProperties(true);
        representer.getPropertyUtils().setSkipMissingProperties(true);

        representer.addClassTag(ResourceConfig.class, Tag.MAP);
        representer.addClassTag(DummyResourceSection.class, Tag.MAP);

        YAML = new Yaml(constructor,representer,dumper);
    }

    private String endpoint = "mirror.mcnative.org";
    private String template = "local";
    private String profile = "local";
    private final Collection<ResourceConfig> resourceConfigs;

    public LoaderConfiguration(){
        this.resourceConfigs = new ArrayList<>();
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getEndpoint(String path){
        String endpoint = getEndpoint().replace("http://","https://");
        if(!endpoint.startsWith("https://")) endpoint = "https://"+endpoint;
        if(!endpoint.endsWith("/")) endpoint+="/";
        return "https://"+endpoint+""+path;
    }

    public String getTemplate() {
        return template;
    }

    public String getProfile() {
        return profile;
    }

    public Collection<ResourceConfig> getResourceConfigs(){
        return resourceConfigs;
    }

    public ResourceConfig getResourceConfig(UUID id){
        Objects.requireNonNull(id);
        for (ResourceConfig config : resourceConfigs) {
            if(config.getId().equals(id)) return config;
        }
        return null;
    }

    public ResourceConfig getResourceConfig(String name){
        Objects.requireNonNull(name);
        for (ResourceConfig config : resourceConfigs) {
            if(config.getName().equalsIgnoreCase(name)) return config;
        }
        ResourceConfig config = new ResourceConfig(name.toLowerCase(),null,"RELEASE","LATEST");
        this.resourceConfigs.add(config);
        return config;
    }

    public boolean hasTemplate(){
        return !getTemplate().equalsIgnoreCase(LOCAL);
    }

    public boolean isLocalManaged(){
        return getProfile().equalsIgnoreCase(LOCAL);
    }

    public boolean isRemoteManaged(){
        return !isLocalManaged();
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setLocalProfile(Map<String,ResourceConfig> profile){
        for (Map.Entry<String, ResourceConfig> entry : profile.entrySet()) {
            entry.getValue().setName(entry.getKey());
            this.resourceConfigs.add(entry.getValue());
        }
    }

    public boolean pullProfiles(Logger logger, File cacheLocation){
        if(isRemoteManaged()){
            if(McNativeConfig.isAvailable()){
                boolean available = false;
                if(cacheLocation.exists()){
                    try (Scanner scanner = new Scanner(cacheLocation, StandardCharsets.UTF_8.name())) {
                        long timeout = Long.parseLong(scanner.nextLine());
                        while (scanner.hasNextLine()){
                            readResourceLine(scanner.nextLine());
                        }
                        scanner.close();
                        if(timeout > System.currentTimeMillis()) return true;
                        available = true;
                    }catch (Exception ignored){}
                }

                try {
                    logger.log(Level.INFO,"(McNative-Loader) Pulling rollout configuration from "+endpoint);
                    HttpURLConnection connection = (HttpURLConnection)new URL(getEndpoint("v1/profiles/"+profile+"?plain=true")).openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Accept-Charset", "UTF-8");
                    connection.setRequestProperty("NetworkId",McNativeConfig.getNetworkId());
                    connection.setRequestProperty("NetworkSecret",McNativeConfig.getNetworkSecret());

                    if(connection.getResponseCode() == 200){
                        this.resourceConfigs.clear();
                        InputStream input = connection.getInputStream();
                        Scanner scanner = new Scanner(input, StandardCharsets.UTF_8.name());
                        BufferedWriter writer = new BufferedWriter(new FileWriter(cacheLocation));
                        writer.append(String.valueOf(System.currentTimeMillis()+TimeUnit.MINUTES.toMillis(3)));
                        while (scanner.hasNextLine()){
                            String line = scanner.nextLine();
                            writer.append("\n").append(line);
                            readResourceLine(line);
                        }
                        writer.close();
                    }else{
                        InputStream response = connection.getErrorStream();
                        String content;
                        try (Scanner scanner = new Scanner(response)) {
                            content = scanner.useDelimiter("\\A").next();
                        }
                        logger.log(Level.SEVERE,"Could not load rollout configuration from remote host ("+connection.getResponseCode()+") "+content);
                        return available;
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE,"Could not load rollout configuration from remote host ("+e.getMessage()+") ");
                    return available;
                }
            }else{
                logger.log(Level.SEVERE,"A remote rollout profile requires McNative console credentials.");
                return false;
            }
        }
        return true;
    }

    private void readResourceLine(String line){
        String[] parts = line.split(";");
        resourceConfigs.add(new ResourceConfig(parts[1],UUID.fromString(parts[0]),parts[2],parts[3]));
    }

    public void save(File file) {
        if(isLocalManaged()){
            try{
                file.getParentFile().mkdirs();
                if(!file.exists()) file.createNewFile();

                Map<String,DummyResourceSection> localProfile = new LinkedHashMap<>();
                for (ResourceConfig resourceConfig : getResourceConfigs()) {
                    localProfile.put(resourceConfig.getName(),new DummyResourceSection(resourceConfig.getQualifier(),resourceConfig.getVersion()));
                }

                Map<String,Object> output = new LinkedHashMap<>();
                output.put("endpoint",getEndpoint());
                output.put("template",getTemplate());
                output.put("profile",getProfile());
                output.put("localProfile",localProfile);

                YAML.dump(output,new FileWriter(file));
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
    }

    public static LoaderConfiguration load(File location) throws Exception{
        if(location.exists()) return YAML.loadAs(new FileInputStream(location),LoaderConfiguration.class);
        else return new LoaderConfiguration();
    }

    public static class DummyResourceSection {

        public String qualifier;
        public String version;

        public DummyResourceSection(String qualifier, String version) {
            this.qualifier = qualifier;
            this.version = version;
        }

        public String getQualifier() {
            return qualifier;
        }

        public String getVersion() {
            return version;
        }
    }
}
