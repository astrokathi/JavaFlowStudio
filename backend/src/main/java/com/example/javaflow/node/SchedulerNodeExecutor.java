package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class SchedulerNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String triggerType = config != null && config.get("triggerType") != null ? config.get("triggerType").toString() : "interval";
        String cronExpression = config != null && config.get("cronExpression") != null ? config.get("cronExpression").toString() : "";
        String intervalSeconds = config != null && config.get("intervalSeconds") != null ? config.get("intervalSeconds").toString() : "60";
        
        output.put("schedulerTriggered", true);
        output.put("triggerType", triggerType);
        if (triggerType.equals("cron")) {
            output.put("cronExpression", cronExpression);
        } else {
            output.put("intervalSeconds", intervalSeconds);
        }
        output.put("triggerTime", LocalDateTime.now().toString());
        output.put("status", "active");
        
        return output;
    }
}
