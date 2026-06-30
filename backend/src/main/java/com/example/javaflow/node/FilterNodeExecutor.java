package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Executor for filter nodes (passes through if condition is met).
 * This is a placeholder implementation that always passes through.
 * In a real implementation, this would evaluate a condition (e.g., using a simple expression language).
 */
@Component
public class FilterNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        // For now, we just pass the input through.
        // In the future, we could implement a simple expression language for filtering.
        executionLog.setStatus("SUCCESS");
        // Optionally, we could add a note in the output that filtering was skipped.
        return input;
    }
}