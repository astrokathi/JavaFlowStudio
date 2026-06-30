package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class ConfigurationProcessorNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String envVariables = config != null && config.get("envVariables") != null ? config.get("envVariables").toString() : "";
        Boolean injectFromYml = config != null && config.get("injectFromYml") != null ? (Boolean) config.get("injectFromYml") : true;
        
        output.put("processedConfig", true);
        output.put("injectedFromYml", injectFromYml);
        
        Map<String, String> vars = new HashMap<>();
        if (!envVariables.isEmpty()) {
            for (String line : envVariables.split("\n")) {
                if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    vars.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        
        if (injectFromYml) {
            vars.put("spring.main.web-application-type", "reactive");
            vars.put("spring.codec.max-in-memory-size", "262144");
        }
        
        output.put("variables", vars);
        return output;
    }
}
