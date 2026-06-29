package com.example.javaflow.service;

import com.example.javaflow.dto.WorkflowDto;
import com.example.javaflow.dto.ExecutionLogDto;
import com.example.javaflow.model.Edge;
import com.example.javaflow.model.ExecutionLog;
import com.example.javaflow.model.Node;
import com.example.javaflow.model.Workflow;
import com.example.javaflow.node.NodeExecutor;
import com.example.javaflow.node.NodeExecutorFactory;
import com.example.javaflow.repository.WorkflowRepository;
import com.example.javaflow.repository.ExecutionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkflowExecutorService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowExecutorService.class);

    private final WorkflowRepository workflowRepository;
    private final ExecutionLogRepository executionLogRepository;
    private final NodeExecutorFactory nodeExecutorFactory;

    public WorkflowExecutorService(WorkflowRepository workflowRepository,
                                   ExecutionLogRepository executionLogRepository,
                                   NodeExecutorFactory nodeExecutorFactory) {
        this.workflowRepository = workflowRepository;
        this.executionLogRepository = executionLogRepository;
        this.nodeExecutorFactory = nodeExecutorFactory;
    }

    /**
     * Execute a workflow by its ID.
     *
     * @param workflowId The ID of the workflow to execute
     * @return Mono containing the list of execution logs for the workflow execution
     */
    public Mono<List<ExecutionLog>> executeWorkflow(String workflowId) {
        return workflowRepository.findById(workflowId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Workflow not found with ID: " + workflowId)))
                .flatMap(this::executeWorkflowInternal);
    }

    /**
     * Execute a workflow (internal method).
     *
     * @param workflow The workflow to execute
     * @return Mono containing the list of execution logs for the workflow execution
     */
    private Mono<List<ExecutionLog>> executeWorkflowInternal(Workflow workflow) {
        log.info("Executing workflow: {}", workflow.getId());

        // Validate workflow
        if (workflow.getNodes() == null || workflow.getNodes().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Workflow has no nodes"));
        }

        // Build a map of node ID to node for easy lookup
        Map<String, Node> nodeMap = workflow.getNodes().stream()
                .collect(Collectors.toMap(Node::getId, node -> node));

        // Validate that all edges refer to existing nodes
        for (Edge edge : workflow.getEdges()) {
            if (!nodeMap.containsKey(edge.getSource()) || !nodeMap.containsKey(edge.getTarget())) {
                return Mono.error(new IllegalArgumentException("Edge references non-existent node: " + edge));
            }
        }

        // Topological sort to determine execution order
        List<Node> sortedNodes = topologicalSort(workflow.getNodes(), workflow.getEdges());

        // Execute nodes in order
        List<ExecutionLog> executionLogs = new ArrayList<>();
        Map<String, Object> dataContext = new HashMap<>(); // Shared data between nodes

        for (Node node : sortedNodes) {
            ExecutionLog executionLog = new ExecutionLog();
            executionLog.setWorkflowId(workflow.getId());
            executionLog.setNodeId(node.getId());
            // executionLog.setNodeType(node.getType()); // Not a field; we can store in output if needed
            executionLog.setStatus("RUNNING");
            executionLog.setStartedAt(LocalDateTime.now());

            try {
                NodeExecutor executor = nodeExecutorFactory.getExecutor(node.getNodeTypeId());
                // Execute the node and get the result
                Object result = executor.execute(node, dataContext, executionLog);
                // For simplicity, we'll store the result in outputData as a map with a single entry
                // In a real system, you might want to store the result more appropriately.
                if (result instanceof Map) {
                    executionLog.setOutputData((Map<String, Object>) result);
                } else {
                    Map<String, Object> outputMap = new HashMap<>();
                    outputMap.put("result", result);
                    executionLog.setOutputData(outputMap);
                }
                executionLog.setStatus("SUCCESS");
                executionLog.setEndedAt(LocalDateTime.now());
                // Store the result in the data context for potential use by subsequent nodes
                dataContext.put(node.getId(), result);
            } catch (Exception e) {
                log.error("Error executing node {}: {}", node.getId(), e.getMessage(), e);
                executionLog.setStatus("FAILED");
                executionLog.setErrorMessage(e.getMessage());
                executionLog.setEndedAt(LocalDateTime.now());
            }

            executionLogs.add(executionLog);
        }

        // Save all execution logs
        return Flux.fromIterable(executionLogs)
                .flatMap(executionLogRepository::save)
                .collectList()
                .doOnNext(savedLogs -> log.info("Workflow execution completed. Logs saved: {}", savedLogs.size()));
    }

    /**
     * Perform a topological sort of the nodes based on the edges.
     *
     * @param nodes The list of nodes in the workflow
     * @param edges The list of edges in the workflow
     * @return A list of nodes in topological order
     */
    private List<Node> topologicalSort(List<Node> nodes, List<Edge> edges) {
        // Create adjacency list and in-degree map
        Map<String, List<String>> adjacencyList = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();

        // Initialize
        for (Node node : nodes) {
            adjacencyList.put(node.getId(), new ArrayList<>());
            inDegree.put(node.getId(), 0);
        }

        // Build the graph
        for (Edge edge : edges) {
            adjacencyList.get(edge.getSource()).add(edge.getTarget());
            inDegree.put(edge.getTarget(), inDegree.get(edge.getTarget()) + 1);
        }

        // Kahn's algorithm
        Queue<String> queue = new LinkedList<>();
        for (String nodeId : inDegree.keySet()) {
            if (inDegree.get(nodeId) == 0) {
                queue.add(nodeId);
            }
        }

        List<Node> sorted = new ArrayList<>();
        Map<String, Node> nodeMap = nodes.stream()
                .collect(Collectors.toMap(Node::getId, node -> node));

        while (!queue.isEmpty()) {
            String nodeId = queue.poll();
            Node node = nodeMap.get(nodeId);
            if (node != null) {
                sorted.add(node);
            }
            for (String neighbor : adjacencyList.get(nodeId)) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        // If there's a cycle, we might not have processed all nodes
        if (sorted.size() < nodes.size()) {
            // Add remaining nodes (those in cycles) in any order
            Set<String> sortedNodeIds = sorted.stream().map(Node::getId).collect(Collectors.toSet());
            for (Node node : nodes) {
                if (!sortedNodeIds.contains(node.getId())) {
                    sorted.add(node);
                }
            }
        }

        return sorted;
    }
}