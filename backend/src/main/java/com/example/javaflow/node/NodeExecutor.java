package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import reactor.core.publisher.Mono;
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
     * @return Mono containing the output data
     */
    Mono<Map<String, Object>> execute(Node node, Map<String, Object> input, ExecutionLog executionLog);
}
