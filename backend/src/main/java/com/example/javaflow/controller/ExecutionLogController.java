package com.example.javaflow.controller;

import com.example.javaflow.dto.ExecutionLogDto;
import com.example.javaflow.service.ExecutionLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/execution-logs")
public class ExecutionLogController {

    private final ExecutionLogService executionLogService;

    public ExecutionLogController(ExecutionLogService executionLogService) {
        this.executionLogService = executionLogService;
    }

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

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteExecutionLog(@PathVariable String id) {
        return executionLogService.findById(id)
                .flatMap(existingExecutionLog ->
                        executionLogService.deleteById(id).then(Mono.just(ResponseEntity.ok().<Void>build())))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().<Void>build()));
    }
}