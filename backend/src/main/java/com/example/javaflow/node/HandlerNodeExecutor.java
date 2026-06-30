package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class HandlerNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String handlerName = config != null && config.get("handlerName") != null ? config.get("handlerName").toString() : "PostHandler";
        String methods = config != null && config.get("methods") != null ? config.get("methods").toString() : "";
        
        output.put("handlerInvoked", true);
        output.put("handlerName", handlerName);
        output.put("handlerMethods", methods);
        output.put("status", "request-passed-to-facade");
        
        return output;
    }
}
