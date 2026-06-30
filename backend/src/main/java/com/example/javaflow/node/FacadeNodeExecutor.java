package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class FacadeNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String facadeName = config != null && config.get("facadeName") != null ? config.get("facadeName").toString() : "PostFacade";
        String methods = config != null && config.get("methods") != null ? config.get("methods").toString() : "";
        
        output.put("facadeInvoked", true);
        output.put("facadeName", facadeName);
        output.put("facadeMethods", methods);
        output.put("status", "facade-delegated-to-service");
        
        return output;
    }
}
