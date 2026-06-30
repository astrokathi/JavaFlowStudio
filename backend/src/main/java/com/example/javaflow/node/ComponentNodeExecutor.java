package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class ComponentNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String componentName = config != null && config.get("componentName") != null ? config.get("componentName").toString() : "AppUtility";
        String methods = config != null && config.get("methods") != null ? config.get("methods").toString() : "";
        
        output.put("componentInvoked", true);
        output.put("componentName", componentName);
        output.put("utilityMethods", methods);
        output.put("inputPassed", sanitizeInput(input));
        output.put("status", "utility-method-executed");
        
        return output;
    }
}
