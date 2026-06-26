package com.example.javaflow.controller;

import com.example.javaflow.dto.ExecutionLogDto;
import com.example.javaflow.service.ExecutionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/execution-logs")
@RequiredArgsConstructor
public class ExecutionLogController {

    private final ExecutionLogService executionLogService;

    @GetMapping
    public Flux<ExecutionLogDto> getAllExecutionLogs() {
        return executionLogService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ExecutionLogDto>> getExecutionLogById(@PathVariable String id) {
        return executionLogService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/workflow/{workflowId}")
    public Flux<ExecutionLogDto> getExecutionLogsByWorkflowId(@PathVariable String workflowId) {
        return executionLogService.findByWorkflowId(workflowId);
    }

    @GetMapping("/execution/{executionId}")
    public Flux<ExecutionLogDto> getExecutionLogsByExecutionId(@PathVariable String executionId) {
        return executionLogService.findByExecutionId(executionId);
    }

    @GetMapping("/workflow/{workflowId}/execution/{executionId}")
    public Flux<ExecutionLogDto> getExecutionLogsByWorkflowAndExecution(@PathVariable String workflowId, @PathVariable String executionId) {
        return executionLogService.findByWorkflowIdAndExecutionId(workflowId, executionId);
    }
}
