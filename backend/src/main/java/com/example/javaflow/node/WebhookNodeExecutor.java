package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import java.util.Map;

/**
 * Executor for webhook nodes (makes an HTTP call to a URL).
 * This is a placeholder implementation.
 */
public class WebhookNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        // For now, just return the input
        return input;
    }
}