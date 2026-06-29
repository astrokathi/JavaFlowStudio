package com.example.javaflow.repository;

import com.example.javaflow.model.NodeType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface NodeTypeRepository extends ReactiveMongoRepository<NodeType, String> {
    Mono<NodeType> findFirstByName(String name);
}