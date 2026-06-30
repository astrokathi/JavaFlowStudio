package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import java.util.Map;

import java.util.HashMap;

/**
 * Interface for executing a node in the workflow.
 */
public interface NodeExecutor {
    /**
     * Execute the node with the given input and return the output.
     *
     * @param node        The node configuration
     * @param input       Input data from previous node
     * @param executionLog The execution log to update (for logging purposes)
     * @return The output data
     */
    Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog);

    default Map<String, Object> sanitizeInput(Map<String, Object> map) {
        if (map == null) return null;
        Map<String, Object> sanitized = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof Map) {
                sanitized.put(entry.getKey(), val.toString());
            } else {
                sanitized.put(entry.getKey(), val);
            }
        }
        return sanitized;
    }
}