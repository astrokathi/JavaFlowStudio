package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.List;

/**
 * Executor for merge nodes (waits for all incoming branches and combines them).
 * Note: This is a simplified version that assumes the input is already a list of inputs
 * from multiple branches. In a real workflow engine, the merging would be handled
 * by the engine based on the workflow topology.
 */
public class MergeNodeExecutor implements NodeExecutor {

    @Override
    public Mono<Map<String, Object>> execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> config = node.getConfig();
        String mergeMode = (String) config.getOrDefault("mode", "concat"); // concat, merge, etc.
        String outputField = (String) config.getOrDefault("outputField", "merged");

        // For simplicity, we assume the input is a list of objects to merge.
        // In a real scenario, the workflow engine would pass the combined input from all branches.
        // Here we just pass through the input and add a marker.

        Map<String, Object> output = new HashMap<>(input);
        output.put("mergedBy", node.getId());
        output.put("mergeMode", mergeMode);

        executionLog.setStatus("SUCCESS");
        return Mono.just(output);
    }
}
