package com.example.javaflow.service;

import com.example.javaflow.dto.ExecutionLogDto;
import com.example.javaflow.model.ExecutionLog;
import com.example.javaflow.repository.ExecutionLogRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ExecutionLogService {

    private final ExecutionLogRepository executionLogRepository;

    public ExecutionLogService(ExecutionLogRepository executionLogRepository) {
        this.executionLogRepository = executionLogRepository;
    }

    public Flux<ExecutionLogDto> findAll() {
        return executionLogRepository.findAll()
                .map(this::toDto);
    }

    public Mono<ExecutionLogDto> findById(String id) {
        return executionLogRepository.findById(id)
                .map(this::toDto);
    }

    public Mono<ExecutionLogDto> save(ExecutionLog executionLog) {
        return executionLogRepository.save(executionLog)
                .map(this::toDto);
    }

    public Flux<ExecutionLogDto> findByWorkflowId(String workflowId) {
        return executionLogRepository.findByWorkflowId(workflowId)
                .map(this::toDto);
    }

    public Flux<ExecutionLogDto> findByExecutionId(String executionId) {
        return executionLogRepository.findByExecutionId(executionId)
                .map(this::toDto);
    }

    public Flux<ExecutionLogDto> findByWorkflowIdAndExecutionId(String workflowId, String executionId) {
        return executionLogRepository.findByWorkflowIdAndExecutionId(workflowId, executionId)
                .map(this::toDto);
    }

    public Mono<Void> deleteById(String id) {
        return executionLogRepository.deleteById(id);
    }

    private ExecutionLogDto toDto(ExecutionLog executionLog) {
        ExecutionLogDto dto = new ExecutionLogDto();
        dto.setId(executionLog.getId());
        dto.setWorkflowId(executionLog.getWorkflowId());
        dto.setExecutionId(executionLog.getExecutionId());
        dto.setStatus(executionLog.getStatus());
        dto.setStartedAt(executionLog.getStartedAt());
        dto.setEndedAt(executionLog.getEndedAt());
        dto.setInputData(executionLog.getInputData());
        dto.setOutputData(executionLog.getOutputData());
        dto.setErrorMessage(executionLog.getErrorMessage());
        return dto;
    }
}