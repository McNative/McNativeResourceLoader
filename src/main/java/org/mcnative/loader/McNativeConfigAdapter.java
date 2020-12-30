package org.mcnative.loader;

import org.mcnative.loader.rollout.RolloutConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;

public class McNativeConfigAdapter {

    private static String ID = null;
    private static String SECRET = null;

    public static File FILE = new File("plugins/McNative/config.yml");
    public static Yaml YAML;

    static {
        DumperOptions dumper = new DumperOptions();
        dumper.setIndent(2);
        dumper.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumper.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        dumper.setPrettyFlow(true);
        CustomClassLoaderConstructor constructor = new CustomClassLoaderConstructor(RolloutConfiguration.class,RolloutConfiguration.class.getClassLoader());

        TypeDescription configDesc = new TypeDescription(RolloutConfiguration.class);
        constructor.addTypeDescription(configDesc);

        Representer representer = new Representer();
        representer.getPropertyUtils().setAllowReadOnlyProperties(true);
        representer.getPropertyUtils().setSkipMissingProperties(true);

        YAML = new Yaml(constructor,representer,dumper);
    }

    public static void load(){
        if(!FILE.exists()) return;
        try {
            DummyConfig config = YAML.loadAs(new FileInputStream(FILE),DummyConfig.class);
            if(config.server == null) return;
            ID = config.server.id;
            SECRET = config.server.secret;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getId(){
        return ID;
    }

    public static String getSecret(){
        return SECRET;
    }

    public static class DummyConfig {

        public DummyServer server;

        public DummyServer getServer() {
            return server;
        }

        public void setServer(DummyServer server) {
            this.server = server;
        }
    }

    public static class DummyServer {

        public String id;
        public String secret;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

}
