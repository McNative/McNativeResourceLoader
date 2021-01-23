package org.mcnative.loader.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;

public class McNativeConfig {

    private static String NETWORK_ID = null;
    private static String NETWORK_SECRET = null;

    public static Yaml YAML;

    static {
        DumperOptions dumper = new DumperOptions();
        dumper.setIndent(2);
        dumper.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumper.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        dumper.setPrettyFlow(true);

        Representer representer = new Representer();
        representer.getPropertyUtils().setAllowReadOnlyProperties(true);
        representer.getPropertyUtils().setSkipMissingProperties(true);

        YAML = new Yaml(representer,dumper);
    }

    public static void load(File location){
        if(!location.exists()) return;
        try {
            DummyConfig config = YAML.loadAs(new FileInputStream(location),DummyConfig.class);
            if(config.console == null) return;
            NETWORK_ID = config.console.networkId;
            NETWORK_SECRET = config.console.secret;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String getNetworkId(){
        return NETWORK_ID;
    }

    public static String getNetworkSecret(){
        return NETWORK_SECRET;
    }

    public static boolean isAvailable(){
        return NETWORK_ID != null && !NETWORK_ID.equalsIgnoreCase("00000-00000-00000");
    }

    public static class DummyConfig {

        public DummyConsoleSection console;

        public DummyConsoleSection getConsole() {
            return console;
        }

        public void setConsole(DummyConsoleSection console) {
            this.console = console;
        }
    }

    public static class DummyConsoleSection {

        public String networkId;
        public String secret;

        public String getNetworkId() {
            return networkId;
        }

        public void setNetworkId(String networkId) {
            this.networkId = networkId;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

}
