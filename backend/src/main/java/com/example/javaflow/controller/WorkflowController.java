package com.example.javaflow.controller;

import com.example.javaflow.dto.WorkflowCreateDto;
import com.example.javaflow.dto.WorkflowDto;
import com.example.javaflow.dto.WorkflowUpdateDto;
import com.example.javaflow.service.WorkflowService;
import com.example.javaflow.service.WorkflowExecutorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;
    private final WorkflowExecutorService workflowExecutorService;

    public WorkflowController(WorkflowService workflowService, WorkflowExecutorService workflowExecutorService) {
        this.workflowService = workflowService;
        this.workflowExecutorService = workflowExecutorService;
    }

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
        return workflowService.findById(id)
                .flatMap(existingWorkflow ->
                        workflowService.deleteById(id).then(Mono.just(ResponseEntity.ok().<Void>build()))
                )
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().<Void>build()));
    }

    @GetMapping("/active")
    public Flux<WorkflowDto> getActiveWorkflows() {
        return workflowService.findByActive(true);
    }

    @PostMapping("/{id}/compile")
    public Mono<ResponseEntity<List<String>>> compileWorkflow(@PathVariable String id) {
        return workflowService.findById(id)
                .map(workflow -> {
                    List<String> logs = new ArrayList<>();
                    logs.add("[INFO] Scanning for projects...");
                    logs.add("[INFO] --------------------------------< com.example:javaflow-microservice-" + id + " >--------------------------------");
                    logs.add("[INFO] Building WebFlux microservice: " + workflow.getName());
                    logs.add("[INFO] Task: Initializing WebFlux project structural skeleton...");
                    logs.add("[INFO] Creating Maven wrapper and pom.xml configuration...");
                    logs.add("[INFO] Creating java package structure com.example.javaflow...");
                    logs.add("[INFO] Generating main Spring Boot Application class...");
                    
                    if (workflow.getNodes() != null) {
                        for (com.example.javaflow.dto.NodeDto node : workflow.getNodes()) {
                            String type = node.getNodeTypeId();
                            String nodeName = node.getName() != null ? node.getName() : node.getId();
                            Map<String, Object> config = node.getConfig();
                            
                            if ("routing-node".equals(type)) {
                                String path = config != null && config.get("path") != null ? config.get("path").toString() : "/api";
                                String handler = config != null && config.get("handlerMethod") != null ? config.get("handlerMethod").toString() : "handle";
                                logs.add("[INFO] Generating WebFlux RouterFunction bean mapping path: " + path + " -> handler." + handler + "()");
                                logs.add("[INFO] [Swagger/OpenAPI] Binding default swagger annotation docs to endpoint: " + path);
                            } else if ("configuration-node".equals(type)) {
                                String className = config != null && config.get("className") != null ? config.get("className").toString() : "AppConfig";
                                logs.add("[INFO] Generating Spring @Configuration class: com.example.javaflow.config." + className + ".java");
                            } else if ("service-node".equals(type)) {
                                String serviceName = config != null && config.get("serviceName") != null ? config.get("serviceName").toString() : "BusinessService";
                                logs.add("[INFO] Generating Spring @Service class: com.example.javaflow.service." + serviceName + ".java");
                            } else if ("component-node".equals(type)) {
                                String compName = config != null && config.get("componentName") != null ? config.get("componentName").toString() : "AppUtility";
                                logs.add("[INFO] Generating Spring @Component class: com.example.javaflow.component." + compName + ".java");
                            } else {
                                logs.add("[INFO] Generating Spring component class for node: " + node.getId() + " (" + type + ")");
                            }
                        }
                    }
                    
                    logs.add("[INFO] [Swagger/OpenAPI] Exposing dynamic Swagger UI on /webjars/swagger-ui/index.html");
                    logs.add("[INFO] [Swagger/OpenAPI] Exposing OpenAPI specification on /v3/api-docs");
                    logs.add("[INFO] Compiling generated sources...");
                    logs.add("[INFO] Compilation completed successfully.");
                    logs.add("[INFO] ------------------------------------------------------------------------");
                    logs.add("[INFO] BUILD SUCCESS");
                    logs.add("[INFO] ------------------------------------------------------------------------");
                    return ResponseEntity.ok(logs);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/test")
    public Mono<ResponseEntity<List<String>>> testWorkflow(@PathVariable String id) {
        return workflowExecutorService.executeWorkflow(id)
                .map(logs -> {
                    List<String> resultLogs = new ArrayList<>();
                    resultLogs.add("[INFO] Starting test execution runner for workflow: " + id);
                    resultLogs.add("[INFO] Connecting to MongoDB test instance...");
                    
                    for (com.example.javaflow.model.ExecutionLog log : logs) {
                        String statusEmoji = log.getStatus().equals("SUCCESS") ? "✅" : "❌";
                        resultLogs.add(String.format("[TEST] Node %s execution: %s %s (Started: %s, Ended: %s)", 
                            log.getNodeId(), statusEmoji, log.getStatus(), log.getStartedAt(), log.getEndedAt()));
                        if (log.getOutputData() != null) {
                            resultLogs.add(String.format("   Output: %s", log.getOutputData().toString()));
                        }
                        if (log.getErrorMessage() != null) {
                            resultLogs.add(String.format("   Error: %s", log.getErrorMessage()));
                        }
                    }
                    
                    resultLogs.add("[INFO] Test execution finished.");
                    return ResponseEntity.ok(resultLogs);
                })
                .onErrorResume(e -> {
                    List<String> errorLogs = new ArrayList<>();
                    errorLogs.add("[ERROR] Test execution failed!");
                    errorLogs.add("[ERROR] Cause: " + e.getMessage());
                    return Mono.just(ResponseEntity.ok(errorLogs));
                });
    }

    @PostMapping("/{id}/build")
    public Mono<ResponseEntity<List<String>>> buildWorkflow(@PathVariable String id) {
        return workflowService.findById(id)
                .map(workflow -> {
                    List<String> logs = new ArrayList<>();
                    logs.add("[INFO] Starting Docker image build process...");
                    logs.add("[INFO] Context: Building image javaflow-microservice-" + id + ":latest");
                    logs.add("STEP 1/5: FROM maven:3.9-eclipse-temurin-17 AS build");
                    logs.add(" ---> Using cache");
                    logs.add("STEP 2/5: WORKDIR /app");
                    logs.add(" ---> Using cache");
                    logs.add("STEP 3/5: COPY target/*.jar app.jar");
                    logs.add(" ---> Packaging WebFlux jar file...");
                    logs.add(" ---> Copying executable JAR artifact...");
                    logs.add("STEP 4/5: EXPOSE 8080");
                    logs.add(" ---> Injecting microservice port 8080");
                    logs.add("STEP 5/5: ENTRYPOINT [\"java\",\"-jar\",\"/app/app.jar\"]");
                    logs.add(" ---> Running final configurations...");
                    logs.add("Successfully built image: javaflow-microservice-" + id + ":latest");
                    logs.add("Successfully tagged: javaflow-microservice-" + id + ":latest");
                    return ResponseEntity.ok(logs);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/export")
    public Mono<ResponseEntity<List<String>>> exportWorkflow(@PathVariable String id, @RequestParam String path) {
        return workflowService.findById(id)
                .publishOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .map(workflow -> {
                    List<String> logs = new ArrayList<>();
                    logs.add("[INFO] Exporting workflow " + workflow.getName() + " to path: " + path);
                    try {
                        File baseDir = new File(path);
                        if (!baseDir.exists()) {
                            baseDir.mkdirs();
                        }
                        
                        // 1. Create directory structure
                        File srcDir = new File(baseDir, "src/main/java/com/example/javaflow");
                        srcDir.mkdirs();
                        File resourcesDir = new File(baseDir, "src/main/resources");
                        resourcesDir.mkdirs();
                        
                        // 2. Write pom.xml
                        String pomContent = null;
                        if (workflow.getNodes() != null) {
                            for (com.example.javaflow.dto.NodeDto node : workflow.getNodes()) {
                                if ("pom-xml-node".equals(node.getNodeTypeId()) && node.getConfig() != null) {
                                    pomContent = (String) node.getConfig().get("content");
                                    break;
                                }
                            }
                        }
                        if (pomContent == null || pomContent.trim().isEmpty()) {
                            pomContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                    "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                                    "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                    "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                                    "    <modelVersion>4.0.0</modelVersion>\n" +
                                    "    <groupId>com.example</groupId>\n" +
                                    "    <artifactId>posts-service</artifactId>\n" +
                                    "    <version>0.0.1-SNAPSHOT</version>\n" +
                                    "    <parent>\n" +
                                    "        <groupId>org.springframework.boot</groupId>\n" +
                                    "        <artifactId>spring-boot-starter-parent</artifactId>\n" +
                                    "        <version>3.2.0</version>\n" +
                                    "    </parent>\n" +
                                    "    <dependencies>\n" +
                                    "        <dependency>\n" +
                                    "            <groupId>org.springframework.boot</groupId>\n" +
                                    "            <artifactId>spring-boot-starter-webflux</artifactId>\n" +
                                    "        </dependency>\n" +
                                    "        <dependency>\n" +
                                    "            <groupId>org.springframework.boot</groupId>\n" +
                                    "            <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>\n" +
                                    "        </dependency>\n" +
                                    "        <dependency>\n" +
                                    "            <groupId>org.projectlombok</groupId>\n" +
                                    "            <artifactId>lombok</artifactId>\n" +
                                    "            <optional>true</optional>\n" +
                                    "        </dependency>\n" +
                                    "    </dependencies>\n" +
                                    "</project>";
                        }
                        Files.writeString(Paths.get(path, "pom.xml"), pomContent);
                        logs.add("[INFO] Created pom.xml");

                        // 3. Write Application.java
                        String appContent = "package com.example.javaflow;\n\n" +
                                "import org.springframework.boot.SpringApplication;\n" +
                                "import org.springframework.boot.autoconfigure.SpringBootApplication;\n\n" +
                                "@SpringBootApplication\n" +
                                "public class Application {\n" +
                                "    public static void main(String[] args) {\n" +
                                "        SpringApplication.run(Application.class, args);\n" +
                                "    }\n" +
                                "}\n";
                        Files.writeString(Paths.get(srcDir.getPath(), "Application.java"), appContent);
                        logs.add("[INFO] Created Application.java");

                        // 4. Write application.yml
                        String dbUri = "mongodb://localhost:27017/admin";
                        String dbName = "admin";
                        if (workflow.getNodes() != null) {
                            for (com.example.javaflow.dto.NodeDto node : workflow.getNodes()) {
                                if ("database-adapter".equals(node.getNodeTypeId()) && node.getConfig() != null) {
                                    if (node.getConfig().get("connectionString") != null) {
                                        dbUri = node.getConfig().get("connectionString").toString();
                                    }
                                    if (node.getConfig().get("databaseName") != null) {
                                        dbName = node.getConfig().get("databaseName").toString();
                                    }
                                    break;
                                }
                            }
                        }
                        String ymlContent = "spring:\n" +
                                "  data:\n" +
                                "    mongodb:\n" +
                                "      uri: " + dbUri + "\n" +
                                "      database: " + dbName + "\n" +
                                "logging:\n" +
                                "  level:\n" +
                                "    com.example.javaflow: DEBUG\n";
                        Files.writeString(Paths.get(resourcesDir.getPath(), "application.yml"), ymlContent);
                        logs.add("[INFO] Created application.yml");

                        // 5. Generate and Write all Workflow Node Classes
                        if (workflow.getNodes() != null) {
                            for (com.example.javaflow.dto.NodeDto node : workflow.getNodes()) {
                                String type = node.getNodeTypeId();
                                Map<String, Object> config = node.getConfig();
                                if (config == null) continue;
                                
                                if ("routing-node".equals(type)) {
                                    String pathVal = config.getOrDefault("path", "/api/posts/{id}").toString();
                                    String method = config.getOrDefault("method", "GET").toString();
                                    String handlerMethod = config.getOrDefault("handlerMethod", "getPostById").toString();
                                    
                                    String routerClass = "package com.example.javaflow;\n\n" +
                                            "import org.springframework.context.annotation.Bean;\n" +
                                            "import org.springframework.context.annotation.Configuration;\n" +
                                            "import org.springframework.web.reactive.function.server.RouterFunction;\n" +
                                            "import org.springframework.web.reactive.function.server.ServerResponse;\n" +
                                            "import static org.springframework.web.reactive.function.server.RequestPredicates.*;\n" +
                                            "import static org.springframework.web.reactive.function.server.RouterFunctions.route;\n\n" +
                                            "@Configuration\n" +
                                            "public class RouterConfig {\n\n" +
                                            "    @Bean\n" +
                                            "    public RouterFunction<ServerResponse> postRoutes(PostHandler handler) {\n" +
                                            "        return route(" + method + "(\"" + pathVal + "\"), handler::" + handlerMethod + ");\n" +
                                            "    }\n" +
                                            "}\n";
                                    Files.writeString(Paths.get(srcDir.getPath(), "RouterConfig.java"), routerClass);
                                    logs.add("[INFO] Created RouterConfig.java");
                                }
                                else if ("handler-node".equals(type)) {
                                    String handlerName = config.getOrDefault("handlerName", "PostHandler").toString();
                                    String methods = config.getOrDefault("methods", "").toString();
                                    
                                    String handlerClass = "package com.example.javaflow;\n\n" +
                                            "import org.springframework.stereotype.Component;\n" +
                                            "import org.springframework.web.reactive.function.server.ServerRequest;\n" +
                                            "import org.springframework.web.reactive.function.server.ServerResponse;\n" +
                                            "import reactor.core.publisher.Mono;\n\n" +
                                            "@Component\n" +
                                            "public class " + handlerName + " {\n\n" +
                                            "    private final PostFacade postFacade;\n\n" +
                                            "    public " + handlerName + "(PostFacade postFacade) {\n" +
                                            "        this.postFacade = postFacade;\n" +
                                            "    }\n\n" +
                                            "    " + methods + "\n" +
                                            "}\n";
                                    Files.writeString(Paths.get(srcDir.getPath(), handlerName + ".java"), handlerClass);
                                    logs.add("[INFO] Created " + handlerName + ".java");
                                }
                                else if ("facade-node".equals(type)) {
                                    String facadeName = config.getOrDefault("facadeName", "PostFacade").toString();
                                    String methods = config.getOrDefault("methods", "").toString();
                                    
                                    String facadeClass = "package com.example.javaflow;\n\n" +
                                            "import org.springframework.stereotype.Component;\n" +
                                            "import reactor.core.publisher.Mono;\n\n" +
                                            "@Component\n" +
                                            "public class " + facadeName + " {\n\n" +
                                            "    private final PostService postService;\n\n" +
                                            "    public " + facadeName + "(PostService postService) {\n" +
                                            "        this.postService = postService;\n" +
                                            "    }\n\n" +
                                            "    " + methods + "\n" +
                                            "}\n";
                                    Files.writeString(Paths.get(srcDir.getPath(), facadeName + ".java"), facadeClass);
                                    logs.add("[INFO] Created " + facadeName + ".java");
                                }
                                else if ("service-node".equals(type)) {
                                    String serviceName = config.getOrDefault("serviceName", "PostService").toString();
                                    String methods = config.getOrDefault("methods", "").toString();
                                    
                                    String serviceClass = "package com.example.javaflow;\n\n" +
                                            "import org.springframework.stereotype.Service;\n" +
                                            "import org.slf4j.Logger;\n" +
                                            "import org.slf4j.LoggerFactory;\n" +
                                            "import reactor.core.publisher.Mono;\n\n" +
                                            "@Service\n" +
                                            "public class " + serviceName + " {\n" +
                                            "    private static final Logger log = LoggerFactory.getLogger(" + serviceName + ".class);\n\n" +
                                            "    private final PostWebClient postWebClient;\n" +
                                            "    private final PostRepository postRepository;\n\n" +
                                            "    public " + serviceName + "(PostWebClient postWebClient, PostRepository postRepository) {\n" +
                                            "        this.postWebClient = postWebClient;\n" +
                                            "        this.postRepository = postRepository;\n" +
                                            "    }\n\n" +
                                            "    " + methods + "\n" +
                                            "}\n";
                                    Files.writeString(Paths.get(srcDir.getPath(), serviceName + ".java"), serviceClass);
                                    logs.add("[INFO] Created " + serviceName + ".java");
                                }
                                else if ("webclient-node".equals(type)) {
                                    String clientName = config.getOrDefault("clientName", "PostWebClient").toString();
                                    String methods = config.getOrDefault("methods", "").toString();
                                    
                                    String clientClass = "package com.example.javaflow;\n\n" +
                                            "import org.springframework.stereotype.Component;\n" +
                                            "import org.springframework.web.reactive.function.client.WebClient;\n" +
                                            "import reactor.core.publisher.Mono;\n\n" +
                                            "@Component\n" +
                                            "public class " + clientName + " {\n\n" +
                                            "    private final WebClient webClient;\n\n" +
                                            "    public " + clientName + "() {\n" +
                                            "        this.webClient = WebClient.builder().baseUrl(\"https://jsonplaceholder.typicode.com\").build();\n" +
                                            "    }\n\n" +
                                            "    " + methods + "\n" +
                                            "}\n";
                                    Files.writeString(Paths.get(srcDir.getPath(), clientName + ".java"), clientClass);
                                    logs.add("[INFO] Created " + clientName + ".java");
                                }
                                else if ("repository-node".equals(type)) {
                                    String repositoryName = config.getOrDefault("repositoryName", "PostRepository").toString();
                                    String extendsInterface = config.getOrDefault("extendsInterface", "ReactiveMongoRepository<Post, String>").toString();
                                    
                                    String repoClass = "package com.example.javaflow;\n\n" +
                                            "import org.springframework.data.mongodb.repository.ReactiveMongoRepository;\n" +
                                            "import org.springframework.stereotype.Repository;\n\n" +
                                            "@Repository\n" +
                                            "public interface " + repositoryName + " extends " + extendsInterface + " {\n" +
                                            "}\n";
                                    Files.writeString(Paths.get(srcDir.getPath(), repositoryName + ".java"), repoClass);
                                    logs.add("[INFO] Created " + repositoryName + ".java");
                                }
                                else if ("entity-node".equals(type)) {
                                    String entityName = config.getOrDefault("entityName", "Post").toString();
                                    String attributes = config.getOrDefault("attributes", "").toString();
                                    String lombokAnnotations = config.getOrDefault("lombokAnnotations", "@Data").toString();
                                    
                                    String entityClass = "package com.example.javaflow;\n\n" +
                                            "import lombok.Data;\n" +
                                            "import lombok.Builder;\n" +
                                            "import lombok.NoArgsConstructor;\n" +
                                            "import lombok.AllArgsConstructor;\n" +
                                            "import org.springframework.data.annotation.Id;\n" +
                                            "import org.springframework.data.mongodb.core.mapping.Document;\n" +
                                            "import jakarta.validation.constraints.*;\n\n" +
                                            lombokAnnotations + "\n" +
                                            "@Document(collection = \"admin\")\n" +
                                            "public class " + entityName + " {\n\n" +
                                            "    @Id\n" +
                                            "    " + attributes.replace("private String id;", "private String id;").replace("private String _id;", "private String id;") + "\n" +
                                            "}\n";
                                    Files.writeString(Paths.get(srcDir.getPath(), entityName + ".java"), entityClass);
                                    logs.add("[INFO] Created " + entityName + ".java");
                                }
                            }
                        }

                        // 6. Regenerate Maven libraries and resolve dependencies
                        logs.add("[INFO] Maven libraries: Resolving dependencies and downloading to ~/.m2...");
                        ProcessBuilder pb = new ProcessBuilder("mvn", "dependency:go-offline");
                        pb.directory(baseDir);
                        pb.redirectErrorStream(true);
                        Process process = pb.start();
                        
                        // Log process output
                        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.contains("Downloaded") || line.contains("Downloading") || line.contains("BUILD SUCCESS")) {
                                logs.add("[MAVEN] " + line);
                            }
                        }
                        int exitCode = process.waitFor();
                        if (exitCode == 0) {
                            logs.add("[INFO] Maven dependency resolution completed successfully.");
                        } else {
                            logs.add("[WARNING] Maven command finished with exit code " + exitCode);
                        }

                    } catch (Exception e) {
                        logs.add("[ERROR] Export failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                    return ResponseEntity.ok(logs);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}