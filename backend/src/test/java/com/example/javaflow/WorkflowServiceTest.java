package com.example.javaflow;

import com.example.javaflow.dto.WorkflowDto;
import com.example.javaflow.dto.WorkflowCreateDto;
import com.example.javaflow.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest
class WorkflowServiceTest {

    @Autowired
    private WorkflowService workflowService;

    @Test
    void testCreateAndFindWorkflow() {
        // Create a workflow DTO
        WorkflowCreateDto createDto = new WorkflowCreateDto();
        createDto.setName("Test Workflow");
        createDto.setDescription("A test workflow");
        createDto.setNodes(List.of());
        createDto.setEdges(List.of());

        // Save the workflow
        workflowService.save(createDto)
                .as(StepVerifier::create)
                .assertNext(saved -> {
                    assertNotNull(saved.getId());
                    assertEquals("Test Workflow", saved.getName());
                    assertEquals("A test workflow", saved.getDescription());
                })
                .verifyComplete();
    }
}
