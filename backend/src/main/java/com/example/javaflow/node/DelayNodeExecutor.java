package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.time.Duration;

/**
 * Executor for delay nodes (waits for a specified duration).
 */
public class DelayNodeExecutor implements NodeExecutor {

    @Override
    public Mono<Map<String, Object>> execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> config = node.getConfig();
        Long delayMs = (Long) config.getOrDefault("delayMs", 0L);
        if (delayMs == null) {
            delayMs = 0L;
        }

        executionLog.setStatus("DELAYING");
        // In a real workflow engine, we would not block the thread here.
        // Instead, we would return a Mono that delays.
        // However, since we are in a reactive context, we can use delayElement.

        return Mono.just(input)
                .delayElement(Duration.ofMillis(delayMs))
                .doOnNext(result -> {
                    executionLog.setStatus("SUCCESS");
                })
                .doOnError(error -> {
                    executionLog.setStatus("ERROR");
                    executionLog.setErrorMessage(error.getMessage());
                });
    }
}
