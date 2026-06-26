package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import reactor.core.publisher.Mono;
import java.util.Map;

/**
 * Executor for filter nodes (passes through if condition is met).
 */
public class FilterNodeExecutor implements NodeExecutor {

    @Override
    public Mono<Map<String, Object>> execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> config = node.getConfig();
        String conditionScript = (String) config.get("condition"); // JavaScript condition that returns boolean
        String inputVariable = (String) config.getOrDefault("inputVariable", "input");

        if (conditionScript == null || conditionScript.isEmpty()) {
            // If no condition, pass through
            executionLog.setStatus("SUCCESS");
            return Mono.just(input);
        }

        try (Context context = Context.newBuilder("js").allowAllAccess(true).build()) {
            // Bind the input as a variable
            context.setBindings(createJsBindings(input), org.graaljs.polyglot.ValueScope.SCOPE_LOCAL);

            // Evaluate the condition
            Value result = context.eval("js", conditionScript);
            boolean passes = result.asBoolean();

            if (passes) {
                executionLog.setStatus("SUCCESS");
                return Mono.just(input);
            } else {
                // Filtered out
                executionLog.setStatus("FILTERED_OUT");
                return Mono.empty(); // Empty Mono signals to not proceed
            }
        } catch (Exception e) {
            executionLog.setStatus("ERROR");
            executionLog.setErrorMessage(e.getMessage());
            return Mono.error(e);
        }
    }

    private org.graaljs.polyglot.Value createJsBindings(Map<String, Object> map) {
        try (Context context = Context.newBuilder("js").allowAllAccess(true).build()) {
            Value value = context.newObject(map);
            return value;
        }
    }
}
