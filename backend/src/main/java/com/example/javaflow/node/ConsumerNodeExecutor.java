package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class ConsumerNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String brokerType = config != null && config.get("brokerType") != null ? config.get("brokerType").toString() : "rabbitmq";
        String queueName = config != null && config.get("queueName") != null ? config.get("queueName").toString() : "";
        
        output.put("brokerType", brokerType);
        output.put("queueName", queueName);
        output.put("status", "listening");
        
        Map<String, Object> messagePayload = new HashMap<>();
        messagePayload.put("eventId", "evt-991");
        messagePayload.put("type", "ORDER_CREATED");
        messagePayload.put("amount", 250.0);
        output.put("receivedMessage", messagePayload);
        
        return output;
    }
}
