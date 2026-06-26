package com.example.javaflow.repository;

import com.example.javaflow.model.Workflow;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorkflowRepository extends ReactiveMongoRepository<Workflow, String> {
    Flux<Workflow> findByActive(boolean active);
}
