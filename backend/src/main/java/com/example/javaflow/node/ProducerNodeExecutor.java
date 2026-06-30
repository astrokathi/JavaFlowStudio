package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProducerNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String brokerType = config != null && config.get("brokerType") != null ? config.get("brokerType").toString() : "rabbitmq";
        String destination = config != null && config.get("destination") != null ? config.get("destination").toString() : "";
        String routingKey = config != null && config.get("routingKey") != null ? config.get("routingKey").toString() : "";
        
        output.put("published", true);
        output.put("brokerType", brokerType);
        output.put("destination", destination);
        output.put("routingKey", routingKey);
        output.put("messageBody", sanitizeInput(input));
        output.put("status", "sent");
        
        return output;
    }
}
