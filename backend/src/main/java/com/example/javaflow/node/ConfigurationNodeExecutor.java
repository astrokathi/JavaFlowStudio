package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class ConfigurationNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String className = config != null && config.get("className") != null ? config.get("className").toString() : "AppConfig";
        String beans = config != null && config.get("beans") != null ? config.get("beans").toString() : "";
        
        output.put("configurationRegistered", true);
        output.put("configClassName", className);
        output.put("registeredBeans", beans);
        output.put("status", "beans-loaded");
        
        return output;
    }
}
