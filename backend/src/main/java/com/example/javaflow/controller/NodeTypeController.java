package com.example.javaflow.controller;

import com.example.javaflow.dto.NodeTypeDto;
import com.example.javaflow.dto.NodeTypeCreateDto;
import com.example.javaflow.dto.NodeTypeUpdateDto;
import com.example.javaflow.service.NodeTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/node-types")
public class NodeTypeController {

    private final NodeTypeService nodeTypeService;

    public NodeTypeController(NodeTypeService nodeTypeService) {
        this.nodeTypeService = nodeTypeService;
    }

    @GetMapping
    public Flux<NodeTypeDto> getAllNodeTypes() {
        return nodeTypeService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<NodeTypeDto>> getNodeTypeById(@PathVariable String id) {
        return nodeTypeService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public Mono<ResponseEntity<NodeTypeDto>> getNodeTypeByName(@PathVariable String name) {
        return nodeTypeService.findByName(name)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<NodeTypeDto>> createNodeType(@RequestBody NodeTypeCreateDto dto) {
        return nodeTypeService.save(dto)
                .map(saved -> ResponseEntity.status(201).body(saved));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<NodeTypeDto>> updateNodeType(@PathVariable String id, @RequestBody NodeTypeUpdateDto dto) {
        return nodeTypeService.update(id, dto)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteNodeType(@PathVariable String id) {
        return nodeTypeService.findById(id)
                .flatMap(existingNodeType ->
                        nodeTypeService.deleteById(id).then(Mono.just(ResponseEntity.ok().<Void>build())))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().<Void>build()));
    }
}