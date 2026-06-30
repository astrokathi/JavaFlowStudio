package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class ConverterNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String fromFormat = config != null && config.get("fromFormat") != null ? config.get("fromFormat").toString() : "json";
        String toFormat = config != null && config.get("toFormat") != null ? config.get("toFormat").toString() : "json";
        
        output.put("converted", true);
        output.put("from", fromFormat);
        output.put("to", toFormat);
        output.put("payloadSize", input != null ? input.size() : 0);
        
        if (toFormat.equals("xml")) {
            output.put("result", "<data>" + (input != null ? input.toString() : "") + "</data>");
        } else if (toFormat.equals("csv")) {
            output.put("result", "key,value\n" + (input != null ? input.entrySet().stream()
                .map(e -> e.getKey() + "," + e.getValue())
                .reduce("", (a, b) -> a + "\n" + b) : ""));
        } else {
            output.put("result", sanitizeInput(input));
        }
        
        return output;
    }
}
