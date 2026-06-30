package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class PomXmlNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String content = config != null && config.get("content") != null ? config.get("content").toString() : "";
        
        output.put("pomLoaded", true);
        output.put("contentLength", content.length());
        output.put("status", "pom-xml-processed");
        
        return output;
    }
}
