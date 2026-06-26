package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.Map;

/**
 * Factory to get the appropriate NodeExecutor for a given node type.
 */
@Component
public class NodeExecutorFactory {

    private final HttpRequestNodeExecutor httpRequestNodeExecutor;
    private final MongoDbNodeExecutor mongoDbNodeExecutor;
    private final TransformNodeExecutor transformNodeExecutor;
    private final SplitNodeExecutor splitNodeExecutor;
    private final MergeNodeExecutor mergeNodeExecutor;
    private final DelayNodeExecutor delayNodeExecutor;
    private final FilterNodeExecutor filterNodeExecutor;
    private final EmailNodeExecutor emailNodeExecutor;
    private final WebhookNodeExecutor webhookNodeExecutor;
    private final CustomScriptNodeExecutor customScriptNodeExecutor;

    @Autowired
    public NodeExecutorFactory(HttpRequestNodeExecutor httpRequestNodeExecutor,
                               MongoDbNodeExecutor mongoDbNodeExecutor,
                               TransformNodeExecutor transformNodeExecutor,
                               SplitNodeExecutor splitNodeExecutor,
                               MergeNodeExecutor mergeNodeExecutor,
                               DelayNodeExecutor delayNodeExecutor,
                               FilterNodeExecutor filterNodeExecutor,
                               EmailNodeExecutor emailNodeExecutor,
                               WebhookNodeExecutor webhookNodeExecutor,
                               CustomScriptNodeExecutor customScriptNodeExecutor) {
        this.httpRequestNodeExecutor = httpRequestNodeExecutor;
        this.mongoDbNodeExecutor = mongoDbNodeExecutor;
        this.transformNodeExecutor = transformNodeExecutor;
        this.splitNodeExecutor = splitNodeExecutor;
        this.mergeNodeExecutor = mergeNodeExecutor;
        this.delayNodeExecutor = delayNodeExecutor;
        this.filterNodeExecutor = filterNodeExecutor;
        this.emailNodeExecutor = emailNodeExecutor;
        this.webhookNodeExecutor = webhookNodeExecutor;
        this.customScriptNodeExecutor = customScriptNodeExecutor;
    }

    /**
     * Get the executor for the given node type.
     * 
     * @param nodeType The node type (e.g., "http-request", "mongodb", etc.)
     * @return The NodeExecutor instance
     * @throws IllegalArgumentException if the node type is not supported
     */
    public NodeExecutor getExecutor(String nodeType is not supported
     */
    public NodeExecutor getExecutor(String nodeType) {
        switch (nodeType.toLowerCase()) {
            case "http-request":
                return httpRequestNodeExecutor;
            case "mongodb":
                return mongoDbNodeExecutor;
            case "transform":
                return transformNodeExecutor;
            case "split":
                return splitNodeExecutor;
            case "merge":
                return mergeNodeExecutor;
            case "delay":
                return delayNodeExecutor;
            case "filter":
                return filterNodeExecutor;
            case "email":
                return emailNodeExecutor;
            case "webhook":
                return webhookNodeExecutor;
            case "custom-script":
                return customScriptNodeExecutor;
            default:
                throw new IllegalArgumentException("Unsupported node type: " + nodeType);
        }
    }
}
