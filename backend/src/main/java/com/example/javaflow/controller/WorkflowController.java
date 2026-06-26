package com.example.javaflow.controller;

import com.example.javaflow.dto.WorkflowDto;
import com.example.javaflow.dto.WorkflowCreateDto;
import com.example.javaflow.dto.WorkflowUpdateDto;
import com.example.javaflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping
    public Flux<WorkflowDto> getAllWorkflows() {
        return workflowService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<WorkflowDto>> getWorkflowById(@PathVariable String id) {
        return workflowService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<WorkflowDto>> createWorkflow(@RequestBody WorkflowCreateDto dto) {
        return workflowService.save(dto)
                .map(saved -> ResponseEntity.status(201).body(saved));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<WorkflowDto>> updateWorkflow(@PathVariable String id, @RequestBody WorkflowUpdateDto dto) {
        return workflowService.update(id, dto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteWorkflow(@PathVariable String id) {
        return workflowService.deleteById(id)
                .map(v -> ResponseEntity.ok().<Void>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public Flux<WorkflowDto> getActiveWorkflows() {
        return workflowService.findByActive(true);
    }
}
