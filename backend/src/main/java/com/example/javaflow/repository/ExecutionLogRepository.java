package com.example.javaflow.repository;

import com.example.javaflow.model.ExecutionLog;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExecutionLogRepository extends ReactiveMongoRepository<ExecutionLog, String> {
    Flux<ExecutionLog> findByWorkflowId(String workflowId);
    Flux<ExecutionLog> findByExecutionId(String executionId);
    Flux<ExecutionLog> findByWorkflowIdAndExecutionId(String workflowId, String executionId);
}
