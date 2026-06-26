package com.example.javaflow.service;

import com.example.javaflow.dto.WorkflowDto;
import com.example.javaflow.dto.WorkflowCreateDto;
import com.example.javaflow.dto.WorkflowUpdateDto;
import com.example.javaflow.model.Workflow;
import com.example.javaflow.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;

    public Flux<WorkflowDto> findAll() {
        return workflowRepository.findAll()
                .map(this::toDto);
    }

    public Mono<WorkflowDto> findById(String id) {
        return workflowRepository.findById(id)
                .map(this::toDto);
    }

    public Mono<WorkflowDto> save(WorkflowCreateDto dto) {
        Workflow workflow = new Workflow();
        workflow.setName(dto.getName());
        workflow.setDescription(dto.getDescription());
        workflow.setNodes(dto.getNodes());
        workflow.setEdges(dto.getEdges());
        workflow.setActive(true);
        // timestamps will be set by @Document
        return workflowRepository.save(workflow)
                .map(this::toDto);
    }

    public Mono<WorkflowDto> update(String id, WorkflowUpdateDto dto) {
        return workflowRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.getName() != null ? dto.getName() : existing.getName());
                    existing.setDescription(dto.getDescription() != null ? dto.getDescription() : existing.getDescription());
                    existing.setNodes(dto.getNodes() != null ? dto.getNodes() : existing.getNodes());
                    existing.setEdges(dto.getEdges() != null ? dto.getEdges() : existing.getEdges());
                    // updatedAt will be updated by @Version or we can set manually if needed
                    return existing;
                })
                .flatMap(workflowRepository::save)
                .map(this::toDto);
    }

    public Mono<Void> deleteById(String id) {
        return workflowRepository.deleteById(id);
    }

    public Flux<WorkflowDto> findByActive(boolean active) {
        return workflowRepository.findByActive(active)
                .map(this::toDto);
    }

    private WorkflowDto toDto(Workflow workflow) {
        WorkflowDto dto = new WorkflowDto();
        dto.setId(workflow.getId());
        dto.setName(workflow.getName());
        dto.setDescription(workflow.getDescription());
        dto.setNodes(workflow.getNodes());
        dto.setEdges(workflow.getEdges());
        dto.setActive(workflow.isActive());
        dto.setCreatedAt(workflow.getCreatedAt());
        dto.setUpdatedAt(workflow.getUpdatedAt());
        return dto;
    }
}
