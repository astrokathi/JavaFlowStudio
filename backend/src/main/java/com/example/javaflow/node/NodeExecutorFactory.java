package com.example.javaflow.node;

import com.example.javaflow.model.Node;
import com.example.javaflow.model.ExecutionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Factory to get the appropriate NodeExecutor for a given node type.
 */
@Component
public class NodeExecutorFactory {

    private final HttpRequestNodeExecutor httpRequestNodeExecutor;
    private final MongoDbNodeExecutor mongoDbNodeExecutor;
    private final SplitNodeExecutor splitNodeExecutor;
    private final MergeNodeExecutor mergeNodeExecutor;
    private final DelayNodeExecutor delayNodeExecutor;
    private final FilterNodeExecutor filterNodeExecutor;
    private final EmailNodeExecutor emailNodeExecutor;
    private final WebhookNodeExecutor webhookNodeExecutor;
    private final MapperNodeExecutor mapperNodeExecutor;
    private final ConverterNodeExecutor converterNodeExecutor;
    private final DatabaseAdapterNodeExecutor databaseAdapterNodeExecutor;
    private final ConfigurationProcessorNodeExecutor configurationProcessorNodeExecutor;
    private final SchedulerNodeExecutor schedulerNodeExecutor;
    private final ConsumerNodeExecutor consumerNodeExecutor;
    private final ProducerNodeExecutor producerNodeExecutor;
    private final BusinessLogicNodeExecutor businessLogicNodeExecutor;
    
    // Architectural nodes
    private final RoutingNodeExecutor routingNodeExecutor;
    private final ConfigurationNodeExecutor configurationNodeExecutor;
    private final ServiceNodeExecutor serviceNodeExecutor;
    private final ComponentNodeExecutor componentNodeExecutor;
    
    // New Standard nodes
    private final HandlerNodeExecutor handlerNodeExecutor;
    private final FacadeNodeExecutor facadeNodeExecutor;
    private final EntityNodeExecutor entityNodeExecutor;
    private final PomXmlNodeExecutor pomXmlNodeExecutor;
    private final RepositoryNodeExecutor repositoryNodeExecutor;
    private final WebClientNodeExecutor webClientNodeExecutor;

    @Autowired
    public NodeExecutorFactory(HttpRequestNodeExecutor httpRequestNodeExecutor,
                               MongoDbNodeExecutor mongoDbNodeExecutor,
                               SplitNodeExecutor splitNodeExecutor,
                               MergeNodeExecutor mergeNodeExecutor,
                               DelayNodeExecutor delayNodeExecutor,
                               FilterNodeExecutor filterNodeExecutor,
                               EmailNodeExecutor emailNodeExecutor,
                               WebhookNodeExecutor webhookNodeExecutor,
                               MapperNodeExecutor mapperNodeExecutor,
                               ConverterNodeExecutor converterNodeExecutor,
                               DatabaseAdapterNodeExecutor databaseAdapterNodeExecutor,
                               ConfigurationProcessorNodeExecutor configurationProcessorNodeExecutor,
                               SchedulerNodeExecutor schedulerNodeExecutor,
                               ConsumerNodeExecutor consumerNodeExecutor,
                               ProducerNodeExecutor producerNodeExecutor,
                               BusinessLogicNodeExecutor businessLogicNodeExecutor,
                               RoutingNodeExecutor routingNodeExecutor,
                               ConfigurationNodeExecutor configurationNodeExecutor,
                               ServiceNodeExecutor serviceNodeExecutor,
                               ComponentNodeExecutor componentNodeExecutor,
                               HandlerNodeExecutor handlerNodeExecutor,
                               FacadeNodeExecutor facadeNodeExecutor,
                               EntityNodeExecutor entityNodeExecutor,
                               PomXmlNodeExecutor pomXmlNodeExecutor,
                               RepositoryNodeExecutor repositoryNodeExecutor,
                               WebClientNodeExecutor webClientNodeExecutor) {
        this.httpRequestNodeExecutor = httpRequestNodeExecutor;
        this.mongoDbNodeExecutor = mongoDbNodeExecutor;
        this.splitNodeExecutor = splitNodeExecutor;
        this.mergeNodeExecutor = mergeNodeExecutor;
        this.delayNodeExecutor = delayNodeExecutor;
        this.filterNodeExecutor = filterNodeExecutor;
        this.emailNodeExecutor = emailNodeExecutor;
        this.webhookNodeExecutor = webhookNodeExecutor;
        this.mapperNodeExecutor = mapperNodeExecutor;
        this.converterNodeExecutor = converterNodeExecutor;
        this.databaseAdapterNodeExecutor = databaseAdapterNodeExecutor;
        this.configurationProcessorNodeExecutor = configurationProcessorNodeExecutor;
        this.schedulerNodeExecutor = schedulerNodeExecutor;
        this.consumerNodeExecutor = consumerNodeExecutor;
        this.producerNodeExecutor = producerNodeExecutor;
        this.businessLogicNodeExecutor = businessLogicNodeExecutor;
        this.routingNodeExecutor = routingNodeExecutor;
        this.configurationNodeExecutor = configurationNodeExecutor;
        this.serviceNodeExecutor = serviceNodeExecutor;
        this.componentNodeExecutor = componentNodeExecutor;
        this.handlerNodeExecutor = handlerNodeExecutor;
        this.facadeNodeExecutor = facadeNodeExecutor;
        this.entityNodeExecutor = entityNodeExecutor;
        this.pomXmlNodeExecutor = pomXmlNodeExecutor;
        this.repositoryNodeExecutor = repositoryNodeExecutor;
        this.webClientNodeExecutor = webClientNodeExecutor;
    }

    /**
     * Get the executor for the given node type.
     * 
     * @param nodeType The node type (e.g., "http-request", "mongodb", etc.)
     * @return The NodeExecutor instance
     * @throws IllegalArgumentException if the node type is not supported
     */
    public NodeExecutor getExecutor(String nodeType) {
        switch (nodeType.toLowerCase()) {
            case "http-request":
                return httpRequestNodeExecutor;
            case "mongodb":
                return mongoDbNodeExecutor;
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
            case "mapper":
                return mapperNodeExecutor;
            case "converter":
                return converterNodeExecutor;
            case "database-adapter":
                return databaseAdapterNodeExecutor;
            case "configuration-processor":
                return configurationProcessorNodeExecutor;
            case "scheduler":
                return schedulerNodeExecutor;
            case "consumer":
                return consumerNodeExecutor;
            case "producer":
                return producerNodeExecutor;
            case "business-logic":
                return businessLogicNodeExecutor;
            case "routing-node":
                return routingNodeExecutor;
            case "configuration-node":
                return configurationNodeExecutor;
            case "service-node":
                return serviceNodeExecutor;
            case "component-node":
                return componentNodeExecutor;
            case "handler-node":
                return handlerNodeExecutor;
            case "facade-node":
                return facadeNodeExecutor;
            case "entity-node":
                return entityNodeExecutor;
            case "pom-xml-node":
                return pomXmlNodeExecutor;
            case "repository-node":
                return repositoryNodeExecutor;
            case "webclient-node":
                return webClientNodeExecutor;
            default:
                throw new IllegalArgumentException("Unsupported node type: " + nodeType);
        }
    }
}