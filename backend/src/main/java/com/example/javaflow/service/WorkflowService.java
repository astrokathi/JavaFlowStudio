package com.example.javaflow.service;

import com.example.javaflow.dto.WorkflowCreateDto;
import com.example.javaflow.dto.WorkflowDto;
import com.example.javaflow.dto.WorkflowUpdateDto;
import com.example.javaflow.model.Edge;
import com.example.javaflow.model.Node;
import com.example.javaflow.model.Workflow;
import com.example.javaflow.repository.WorkflowRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;

    public WorkflowService(WorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

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
        workflow.setNodes(dto.getNodes().stream()
                .map(this::toNode)
                .collect(Collectors.toList()));
        workflow.setEdges(dto.getEdges().stream()
                .map(this::toEdge)
                .collect(Collectors.toList()));
        return workflowRepository.save(workflow)
                .map(this::toDto);
    }

    public Mono<WorkflowDto> update(String id, WorkflowUpdateDto dto) {
        return workflowRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.getName() != null ? dto.getName() : existing.getName());
                    existing.setDescription(dto.getDescription() != null ? dto.getDescription() : existing.getDescription());
                    if (dto.getNodes() != null) {
                        existing.setNodes(dto.getNodes().stream()
                                .map(this::toNode)
                                .collect(Collectors.toList()));
                    }
                    if (dto.getEdges() != null) {
                        existing.setEdges(dto.getEdges().stream()
                                .map(this::toEdge)
                                .collect(Collectors.toList()));
                    }
                    return existing;
                })
                .flatMap(workflowRepository::save)
                .map(this::toDto);
    }

    public Mono<Void> deleteById(String id) {
        return workflowRepository.deleteById(id);
    }

    public Flux<WorkflowDto> findByActive(boolean active) {
        // We don't have an active field in Workflow, so we return all for now.
        // In a real implementation, we would add an active field to the Workflow model.
        return workflowRepository.findAll()
                .map(this::toDto);
    }

    private Node toNode(com.example.javaflow.dto.NodeDto dto) {
        Node node = new Node();
        node.setId(dto.getId());
        node.setName(dto.getName());
        node.setNodeTypeId(dto.getNodeTypeId());
        // Set position
        Node.Position position = new Node.Position();
        position.setX(dto.getPosition().getX());
        position.setY(dto.getPosition().getY());
        node.setPosition(position);
        node.setConfig(dto.getConfig());
        return node;
    }

    private com.example.javaflow.dto.NodeDto toNodeDto(Node node) {
        com.example.javaflow.dto.NodeDto dto = new com.example.javaflow.dto.NodeDto();
        dto.setId(node.getId());
        dto.setName(node.getName());
        dto.setNodeTypeId(node.getNodeTypeId());
        // Set position
        com.example.javaflow.dto.NodeDto.PositionDto positionDto = new com.example.javaflow.dto.NodeDto.PositionDto();
        positionDto.setX(node.getPosition().getX());
        positionDto.setY(node.getPosition().getY());
        dto.setPosition(positionDto);
        dto.setConfig(node.getConfig());
        return dto;
    }

    private Edge toEdge(com.example.javaflow.dto.EdgeDto dto) {
        Edge edge = new Edge();
        edge.setId(dto.getId());
        edge.setSource(dto.getSource());
        edge.setTarget(dto.getTarget());
        return edge;
    }

    private com.example.javaflow.dto.EdgeDto toEdgeDto(Edge edge) {
        com.example.javaflow.dto.EdgeDto dto = new com.example.javaflow.dto.EdgeDto();
        dto.setId(edge.getId());
        dto.setSource(edge.getSource());
        dto.setTarget(edge.getTarget());
        return dto;
    }

    private WorkflowDto toDto(Workflow workflow) {
        WorkflowDto dto = new WorkflowDto();
        dto.setId(workflow.getId());
        dto.setName(workflow.getName());
        dto.setDescription(workflow.getDescription());
        dto.setNodes(workflow.getNodes().stream()
                .map(this::toNodeDto)
                .collect(Collectors.toList()));
        dto.setEdges(workflow.getEdges().stream()
                .map(this::toEdgeDto)
                .collect(Collectors.toList()));
        return dto;
    }
}