package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class RoutingNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String path = config != null && config.get("path") != null ? config.get("path").toString() : "/api";
        String method = config != null && config.get("method") != null ? config.get("method").toString() : "GET";
        String handlerMethod = config != null && config.get("handlerMethod") != null ? config.get("handlerMethod").toString() : "handle";
        
        output.put("routingTriggered", true);
        output.put("routePath", path);
        output.put("httpMethod", method);
        output.put("targetHandler", handlerMethod);
        output.put("payloadPassed", sanitizeInput(input));
        output.put("status", "routed-successfully");
        
        return output;
    }
}
