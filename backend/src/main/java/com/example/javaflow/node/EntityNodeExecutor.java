package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class EntityNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String entityName = config != null && config.get("entityName") != null ? config.get("entityName").toString() : "Post";
        String attributes = config != null && config.get("attributes") != null ? config.get("attributes").toString() : "";
        String lombokAnnotations = config != null && config.get("lombokAnnotations") != null ? config.get("lombokAnnotations").toString() : "@Data";
        
        output.put("entityRegistered", true);
        output.put("entityName", entityName);
        output.put("attributes", attributes);
        output.put("lombokAnnotations", lombokAnnotations);
        output.put("status", "entity-definition-loaded");
        
        return output;
    }
}
