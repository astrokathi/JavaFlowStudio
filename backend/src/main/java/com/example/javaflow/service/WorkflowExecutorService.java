package com.example.javaflow.service;

import com.example.javaflow.dto.WorkflowDto;
import com.example.javaflow.dto.ExecutionLogDto;
import com.example.javaflow.model.ExecutionLog;
import com.example.javaflow.model.Node;
import com.example.javaflow.model.Edge;
import com.example.javaflow.node.NodeExecutor;
import com.example.javaflow.node.NodeExecutorFactory;
import com.example.javaflow.repository.ExecutionLogRepository;
import com.example.javaflow.service.ExecutionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to execute a workflow.
 */
@Service
@RequiredArgsConstructor
public class WorkflowExecutorService {

    private final WorkflowService workflowService;
    private final NodeExecutorFactory nodeExecutorFactory;
    private final ExecutionLogService executionLogService;
    private final ExecutionLogRepository executionLogRepository;

    /**
     * Execute a workflow with the given input.
     * 
     * @param workflowId The ID of the workflow to execute
     * @param input      The initial input data
     * @return Flux of ExecutionLogDto for each step (for monitoring)
     */
    public Flux<ExecutionLogDto> executeWorkflow(String workflowId, Map<String, Object> input) {
        return workflowService.findById(workflowId)
                .flatMapMany(workflowDto -> {
                    // For now, we'll execute nodes in the order they appear
                    // A full implementation would do topological sorting based on edges
                    return Flux.fromIterable(workflowDto.getNodes())
                            .concatMap(node -> executeNodeWithLogging(workflowId, node, input))
                            // Note: This is a simplified version - a real implementation would need to
                            // handle data flow between nodes based on edges
                });
    }

    /**
     * Execute a single node with logging.
     * 
     * @param workflowId The workflow ID
     * @param node       The node to execute
     * @param input      The input data for this node
     * @return Mono of ExecutionLogDto
     */
    private Mono<ExecutionLogDto> executeNodeWithLogging(String workflowId, Node node, Map<String, Object> input) {
        // Create an execution log entry for this node execution
        ExecutionLog executionLog = new ExecutionLog();
        executionLog.setWorkflowId(workflowId);
        executionLog.setNodeId(node.getId());
        executionLog.setInputData(input != null ? input : new HashMap<>());
        executionLog.setStatus("STARTED");
        
        // Save the initial log (with STARTED status)
        return executionLogService.save(executionLog)
                .flatMap(savedLog -> {
                    // Get the appropriate executor for the node type
                    NodeExecutor executor = nodeExecutorFactory.getExecutor(node.getNodeTypeId());
                    // Execute the node
                    return executor.execute(node, executionLog.getInputData(), savedLog)
                            .doOnNext(output -> {
                                // Update the execution log with output and success status
                                savedLog.setOutputData(output);
                                savedLog.setStatus("SUCCESS");
                            })
                            .doOnError(error -> {
                                // Update the execution log with error
                                savedLog.setStatus("ERROR");
                                savedLog.setErrorMessage(error.getMessage());
                            })
                            .then(executionLogService.save(savedLog)) // Save the updated log
                            .map(this::toDto); // Convert to DTO for return
                });
    }

    private ExecutionLogDto toDto(ExecutionLog executionLog) {
        ExecutionLogDto dto = new ExecutionLogDto();
        dto.setId(executionLog.getId());
        dto.setWorkflowId(executionLog.getWorkflowId());
        dto.setExecutionId(executionLog.getExecutionId());
        dto.setNodeId(executionLog.getNodeId());
        dto.setStatus(executionLog.getStatus());
        dto.setStartedAt(executionLog.getStartedAt());
        dto.setEndedAt(executionLog.getEndedAt());
        dto.setInputData(executionLog.getInputData());
        dto.setOutputData(executionLog.getOutputData());
        dto.setErrorMessage(executionLog.getErrorMessage());
        return dto;
    }
}
