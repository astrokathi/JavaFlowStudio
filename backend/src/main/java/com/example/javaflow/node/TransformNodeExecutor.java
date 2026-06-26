package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.graaljs.js;
import org.graaljs.polyglot.Context;
import org.graaljs.polyglot.Value;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.HashMap;

/**
 * Executor for transform nodes (using JavaScript via GraalVM).
 */
public class TransformNodeExecutor implements NodeExecutor {

    @Override
    public Mono<Map<String, Object>> execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> config = node.getConfig();
        String script = (String) config.get("script");
        String inputVariable = (String) config.getOrDefault("inputVariable", "input");
        String outputVariable = (String) config.getOrDefault("outputVariable", "output");

        if (script == null || script.isEmpty()) {
            executionLog.setStatus("ERROR");
            executionLog.setErrorMessage("Script is empty");
            return Mono.error(new IllegalArgumentException("Script is empty"));
        }

        try (Context context = Context.newBuilder("js").allowAllAccess(true).build()) {
            // Bind the input as a variable
            context.setBindings(createJsBindings(input), org.graaljs.polyglot.ValueScope.SCOPE_LOCAL);

            // Execute the script
            Value result = context.eval("js", script);

            // Get the output variable if specified, otherwise return the result
            Object outputObj;
            if (outputVariable != null && !outputVariable.isEmpty()) {
                Value outputValue = context.getBindings(org.graaljs.polyglot.ValueScope.SCOPE_LOCAL).getMember(outputVariable);
                outputObj = fromValue(outputValue);
            } else {
                outputObj = fromValue(result);
            }

            Map<String, Object> output = new HashMap<>();
            if (outputObj instanceof Map) {
                output = (Map<String, Object>) outputObj;
            } else {
                output.put("result", outputObj);
            }

            executionLog.setStatus("SUCCESS");
            return Mono.just(output);
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

    private Object fromValue(Value value) {
        if (value.isNull()) {
            return null;
        } else if (value.isBoolean()) {
            return value.asBoolean();
        } else if (value.isNumber()) {
            return value.asDouble();
        } else if (value.isString()) {
            return value.asString();
        } else if (value.isObject()) {
            // Convert to Map
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            value.getMemberKeys().forEach(key -> {
                map.put(key, fromValue(value.getMember(key)));
            });
            return map;
        } else if (value.isArray()) {
            java.util.List<Object> list = new java.util.ArrayList<>();
            for (int i = 0; i < value.getArraySize(); i++) {
                list.add(fromValue(value.getArrayElement(i)));
            }
            return list;
        } else {
            return value.toString();
        }
    }
}
