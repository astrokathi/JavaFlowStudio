package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import java.util.Map;

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
}