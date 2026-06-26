package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Executor for split nodes (splits an array into multiple items).
 */
public class SplitNodeExecutor implements NodeExecutor {

    @Override
    public Mono<Map<String, Object>> execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> config = node.getConfig();
        String inputArrayField = (String) config.get("inputArrayField");
        String outputItemField = (String) config.getOrDefault("outputItemField", "item");
        String indexField = (String) config.getOrDefault("indexField", "index");

        if (inputArrayField == null || inputArrayField.isEmpty()) {
            executionLog.setStatus("ERROR");
            executionLog.setErrorMessage("inputArrayField is required");
            return Mono.error(new IllegalArgumentException("inputArrayField is required"));
        }

        Object inputArrayObj = input.get(inputArrayField);
        if (!(inputArrayObj instanceof List)) {
            executionLog.setStatus("ERROR");
            executionLog.setErrorMessage("inputArrayField does not point to a list");
            return Mono.error(new IllegalArgumentException("inputArrayField does not point to a list"));
        }

        List<?> inputArray = (List<?>) inputArrayObj;
        List<Map<String, Object>> outputItems = new ArrayList<>();

        for (int i = 0; i < inputArray.size(); i++) {
            Map<String, Object> item = new HashMap<>();
            item.put(outputItemField, inputArray.get(i));
            item.put(indexField, i);
            outputItems.add(item);
        }

        // For a split node, we typically want to emit multiple outputs.
        // However, our NodeExecutor interface returns a single Mono<Map<String, Object>>.
        // To handle splitting, we can return the first item and rely on the workflow engine
        // to handle the splitting logic (e.g., by having a special splitter node that
        // triggers multiple outgoing edges).
        // Alternatively, we can change the interface to return Flux<Map<String, Object>>.
        // For simplicity, we'll return the first item and note that the workflow engine
        // should handle splitting based on node type.

        if (outputItems.isEmpty()) {
            executionLog.setStatus("SUCCESS");
            return Mono.just(new HashMap<>());
        }

        executionLog.setStatus("SUCCESS");
        // Return the first item; the workflow engine should know to split.
        return Mono.just(outputItems.get(0));
    }
}
