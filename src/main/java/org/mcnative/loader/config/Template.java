package org.mcnative.loader.config;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Template {

    private final Map<String,String> resources;
    private final Map<String,String> variables;

    public Template() {
        this.resources = new HashMap<>();
        this.variables = new HashMap<>();
    }

    public Map<String, String> getResources() {
        return resources;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public static Template pullTemplate(Logger logger, LoaderConfiguration config){
        logger.log(Level.INFO,"(Template-Loader) Pulling template "+config.getTemplate()+" from "+config.getEndpoint());
        Template template = new Template();
        try{
            HttpURLConnection connection = (HttpURLConnection)new URL(config.getEndpoint("v1/templates/"+config.getTemplate()+"?plain=true")).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("NetworkId",McNativeConfig.getNetworkId());
            connection.setRequestProperty("NetworkSecret",McNativeConfig.getNetworkSecret());

            if(connection.getResponseCode() == 200){
                InputStream input = connection.getInputStream();
                Properties properties = new Properties();
                properties.load(input);

                for (String resources : properties.getProperty("resources").split(";")) {
                    String[] parts = resources.split(":");
                    template.getResources().put(parts[0],parts[1]);
                }

                for (String resources : properties.getProperty("variables").split(";")) {
                    String[] parts = resources.split(":");
                    template.getVariables().put(parts[0],parts[1]);
                }

            }else{
                InputStream response = connection.getErrorStream();
                String content = "";
                if(response != null){
                    try (Scanner scanner = new Scanner(response)) {
                        content = scanner.useDelimiter("\\A").next();
                    }
                }
                logger.log(Level.SEVERE,"(Resource-Loader) Could not load template "+config.getTemplate()+" from remote host ("+connection.getResponseCode()+") "+content);
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.log(Level.SEVERE,"(Resource-Loader) Could not load template "+config.getTemplate());
            logger.log(Level.SEVERE,"(Resource-Loader) Error: "+e.getMessage());
        }
        return template;
    }
}
