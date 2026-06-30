package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Executor for delay nodes (waits for a specified duration).
 * This is a placeholder implementation that doesn't actually delay.
 */
@Component
public class DelayNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        // For now, just return the input after a simulated delay (but we don't actually delay in this placeholder)
        return input;
    }
}