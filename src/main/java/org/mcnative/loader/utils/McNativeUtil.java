package org.mcnative.loader.utils;

import org.mcnative.loader.config.Template;
import org.mcnative.runtime.api.McNative;

import java.util.Map;

public class McNativeUtil {

    public static void registerVariables(Template template){
        for (Map.Entry<String, String> variable : template.getVariables().entrySet()) {
            McNative.getInstance().setVariable(variable.getKey(),variable.getValue());
        }
    }

}
