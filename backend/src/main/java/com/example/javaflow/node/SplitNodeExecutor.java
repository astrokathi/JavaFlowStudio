package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.springframework.stereotype.Component;

/**
 * Executor for split nodes (splits input into multiple outputs).
 * This is a placeholder implementation that just wraps the input in a list.
 */
@Component
public class SplitNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        // For now, just return the input as a single-element list (simulating split into one branch)
        executionLog.setStatus("SUCCESS");
        List<Object> result = new ArrayList<>();
        result.add(input);
        return result;
    }
}