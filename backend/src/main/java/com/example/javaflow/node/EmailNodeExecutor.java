package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import java.util.Map;

/**
 * Executor for email nodes (sends an email).
 * This is a placeholder implementation.
 */
public class EmailNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        // In a real implementation, we would send an email using the node configuration and input data.
        // For now, we just return the input (or we could return a status map).
        // We do not modify the executionLog; the service will handle status and output.
        return input;
    }
}