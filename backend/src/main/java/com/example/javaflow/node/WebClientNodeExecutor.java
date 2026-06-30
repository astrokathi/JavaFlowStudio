package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.HashMap;
import java.util.Map;

@Component
public class WebClientNodeExecutor implements NodeExecutor {

    private final WebClient webClient;

    public WebClientNodeExecutor() {
        this.webClient = WebClient.builder().build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> config = node.getConfig();
        
        String url = config != null && config.get("url") != null ? config.get("url").toString() : "https://jsonplaceholder.typicode.com/posts/{id}";
        
        // Extract id from input
        String id = "1";
        if (input != null) {
            if (input.get("id") != null) {
                id = input.get("id").toString();
            } else if (input.get("payloadPassed") != null && input.get("payloadPassed") instanceof Map) {
                Map<String, Object> inner = (Map<String, Object>) input.get("payloadPassed");
                if (inner.get("id") != null) {
                    id = inner.get("id").toString();
                }
            } else if (input.get("facadeInput") != null && input.get("facadeInput") instanceof Map) {
                Map<String, Object> inner = (Map<String, Object>) input.get("facadeInput");
                if (inner.get("id") != null) {
                    id = inner.get("id").toString();
                }
            }
        }
        
        String targetUrl = url.replace("{id}", id);
        
        try {
            Map<?, ?> fetchResult = webClient.get()
                .uri(targetUrl)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
                
            output.put("webclientFetched", true);
            output.put("targetUrl", targetUrl);
            output.put("responseBody", fetchResult);
            output.put("id", id);
            output.put("status", "fetched-successfully");
        } catch (Exception e) {
            output.put("webclientFetched", false);
            output.put("targetUrl", targetUrl);
            output.put("error", e.getMessage());
            // Fallback mock payload
            output.put("responseBody", Map.of(
                "id", id,
                "title", "Mock Post title",
                "body", "Mock post body",
                "userId", 1
            ));
            output.put("status", "fetch-failed-mocked");
        }
        
        return output;
    }
}
