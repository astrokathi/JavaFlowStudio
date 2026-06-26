package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.http.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.HashMap;

/**
 * Executor for webhook nodes (makes an HTTP call to a URL).
 * Similar to HttpRequestNodeExecutor but might have different configuration.
 */
public class WebhookNodeExecutor implements NodeExecutor {

    private final WebClient webClient;

    public WebhookNodeExecutor(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Map<String, Object>> execute(Node node, Map<String, Object> input, ExecutionLog executionLog) {
        Map<String, Object> config = node.getConfig();
        String method = (String) config.getOrDefault("method", "POST");
        String url = (String) config.get("url");
        Map<String, String> headers = (Map<String, String>) config.getOrDefault("headers", new HashMap<>());
        Map<String, Object> queryParams = (Map<String, Object>) config.getOrDefault("queryParams", new HashMap<>());
        Object body = config.getOrDefault("body", null);
        Boolean parseResponseAsJson = (Boolean) config.getOrDefault("parseResponseAsJson", true);

        // Build the request
        WebClient.RequestBodySpec request = webClient.method(HttpMethod.valueOf(method.toUpperCase()))
                .uri(uriBuilder -> {
                    // Replace placeholders in URL with input values? For simplicity, we'll just use the URL as is.
                    // In a more advanced version, we could do templating.
                    return uriBuilder.path(url);
                });

        // Add headers
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request = request.header(entry.getKey(), entry.getValue());
            }
        }

        // Add query parameters
        if (queryParams != null && !queryParams.isEmpty()) {
            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                request = request.queryParam(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        // Add body if present
        WebClient.BodyContentSpec bodySpec;
        if (body != null) {
            bodySpec = request.bodyValue(body);
        } else {
            bodySpec = request;
        }

        // Exchange
        return bodySpec.exchangeToMono(response -> {
            // Update execution log with status code
            executionLog.setStatus("HTTP_" + response.statusCode().value());
            // For simplicity, we'll store the status code in output as well
            Map<String, Object> output = new HashMap<>();
            output.put("statusCode", response.statusCode().value());
            output.put("headers", response.headers().asHttpHeaders());

            if (parseResponseAsJson) {
                return response.bodyToMono(Map.class)
                        .map(bodyMap -> {
                            output.put("body", bodyMap);
                            return output;
                        })
                        .onErrorResume(e -> {
                            // If JSON parsing fails, return as string
                            return response.bodyToMono(String.class)
                                    .map(bodyStr -> {
                                        output.put("body", bodyStr);
                                        return output;
                                    });
                        });
            } else {
                return response.bodyToMono(String.class)
                        .map(bodyStr -> {
                            output.put("body", bodyStr);
                            return output;
                        });
            }
        });
    }
}
