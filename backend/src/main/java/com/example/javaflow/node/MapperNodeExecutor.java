package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class MapperNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String mappingsStr = "";
        if (config != null && config.get("mappings") != null) {
            mappingsStr = config.get("mappings").toString();
        }

        output.put("mapped", true);
        output.put("status", "mapped-successfully");
        output.put("originalInput", sanitizeInput(input));
        
        if (input != null) {
            input.forEach((k, v) -> {
                output.put("mapped_" + k, v);
            });
        }
        
        return output;
    }
}
