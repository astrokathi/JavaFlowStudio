package com.example.javaflow.service;

import com.example.javaflow.dto.NodeTypeDto;
import com.example.javaflow.dto.NodeTypeCreateDto;
import com.example.javaflow.dto.NodeTypeUpdateDto;
import com.example.javaflow.model.NodeType;
import com.example.javaflow.repository.NodeTypeRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class NodeTypeService {

    private final NodeTypeRepository nodeTypeRepository;

    public NodeTypeService(NodeTypeRepository nodeTypeRepository) {
        this.nodeTypeRepository = nodeTypeRepository;
    }

    public Flux<NodeTypeDto> findAll() {
        return nodeTypeRepository.findAll()
                .map(this::toDto);
    }

    public Mono<NodeTypeDto> findById(String id) {
        return nodeTypeRepository.findById(id)
                .map(this::toDto);
    }

    public Mono<NodeTypeDto> save(NodeTypeCreateDto dto) {
        NodeType nodeType = new NodeType();
        nodeType.setName(dto.getName());
        nodeType.setDescription(dto.getDescription());
        nodeType.setConfigSchema(dto.getConfigSchema());
        return nodeTypeRepository.save(nodeType)
                .map(this::toDto);
    }

    public Mono<NodeTypeDto> update(String id, NodeTypeUpdateDto dto) {
        return nodeTypeRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.getName() != null ? dto.getName() : existing.getName());
                    existing.setDescription(dto.getDescription() != null ? dto.getDescription() : existing.getDescription());
                    existing.setConfigSchema(dto.getConfigSchema() != null ? dto.getConfigSchema() : existing.getConfigSchema());
                    return existing;
                })
                .flatMap(nodeTypeRepository::save)
                .map(this::toDto);
    }

    public Mono<Void> deleteById(String id) {
        return nodeTypeRepository.deleteById(id);
    }

    public Mono<NodeTypeDto> findByName(String name) {
        return nodeTypeRepository.findFirstByName(name)
                .map(this::toDto);
    }

    private NodeTypeDto toDto(NodeType nodeType) {
        NodeTypeDto dto = new NodeTypeDto();
        dto.setId(nodeType.getId());
        dto.setName(nodeType.getName());
        dto.setDescription(nodeType.getDescription());
        dto.setConfigSchema(nodeType.getConfigSchema());
        return dto;
    }
}