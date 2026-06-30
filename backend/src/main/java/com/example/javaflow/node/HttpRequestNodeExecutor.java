package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Executor for HTTP request nodes.
 * This is a placeholder implementation.
 */
@Component
public class HttpRequestNodeExecutor implements NodeExecutor {

    @Override
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        // For now, just return a dummy response
        return "HTTP request executed (placeholder)";
    }
}