package org.mcnative.loader.config;

import java.util.HashMap;
import java.util.Map;
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

        //For Testing
        template.resources.put("DKBans","ea6d0f4d-1a8f-4f9b-835a-c17b9169df17");
        template.resources.put("DKCoins","0249f842-de95-42df-b611-7ad390d90086");

        return template;
    }
}
