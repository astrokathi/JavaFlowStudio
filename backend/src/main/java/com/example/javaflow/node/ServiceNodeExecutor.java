package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class ServiceNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String serviceName = config != null && config.get("serviceName") != null ? config.get("serviceName").toString() : "BusinessService";
        String methods = config != null && config.get("methods") != null ? config.get("methods").toString() : "";
        
        output.put("serviceInvoked", true);
        output.put("serviceName", serviceName);
        output.put("definedMethods", methods);
        output.put("payloadProcessed", sanitizeInput(input));
        output.put("status", "service-execution-completed");
        
        return output;
    }
}
