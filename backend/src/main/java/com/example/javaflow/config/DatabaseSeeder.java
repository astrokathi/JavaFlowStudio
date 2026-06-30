package com.example.javaflow.config;

import com.example.javaflow.model.NodeType;
import com.example.javaflow.model.Workflow;
import com.example.javaflow.model.Node;
import com.example.javaflow.model.Edge;
import com.example.javaflow.repository.NodeTypeRepository;
import com.example.javaflow.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final NodeTypeRepository nodeTypeRepository;
    private final WorkflowRepository workflowRepository;

    @Autowired
    public DatabaseSeeder(NodeTypeRepository nodeTypeRepository, WorkflowRepository workflowRepository) {
        this.nodeTypeRepository = nodeTypeRepository;
        this.workflowRepository = workflowRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Resetting and seeding default node types...");
        nodeTypeRepository.deleteAll()
            .then(seedNodeTypes())
            .then(workflowRepository.deleteAll())
            .then(seedSchoolWorkflow())
            .then(seedPostsWorkflow())
            .subscribe();
    }

    private Mono<Void> seedNodeTypes() {
        // 1. Mapper Node
        NodeType mapper = new NodeType();
        mapper.setName("mapper");
        mapper.setDescription("Maps attributes from a source JSON schema to a target JSON schema");
        Map<String, Object> mapperSchema = new HashMap<>();
        mapperSchema.put("sourceSchema", createField("Source JSON Schema", "textarea", "{}", "JSON schema of the source object"));
        mapperSchema.put("targetSchema", createField("Target JSON Schema", "textarea", "{}", "JSON schema of the target object"));
        mapperSchema.put("mappings", createField("Mappings Config", "textarea", "{\n  \"sourceField\": \"targetField\"\n}", "Key-value mapping of fields"));
        mapper.setConfigSchema(mapperSchema);

        // 2. Converter Node
        NodeType converter = new NodeType();
        converter.setName("converter");
        converter.setDescription("Converts input data format (JSON, XML, CSV)");
        Map<String, Object> converterSchema = new HashMap<>();
        converterSchema.put("fromFormat", createSelectField("From Format", "json", 
            List.of(Map.of("value", "json", "label", "JSON"), 
                    Map.of("value", "xml", "label", "XML"), 
                    Map.of("value", "csv", "label", "CSV")), 
            "Format of the input payload"));
        converterSchema.put("toFormat", createSelectField("To Format", "json", 
            List.of(Map.of("value", "json", "label", "JSON"), 
                    Map.of("value", "xml", "label", "XML"), 
                    Map.of("value", "csv", "label", "CSV")), 
            "Format to convert the payload into"));
        converter.setConfigSchema(converterSchema);

        // 3. Database Adapter Node
        NodeType dbAdapter = new NodeType();
        dbAdapter.setName("database-adapter");
        dbAdapter.setDescription("Plug-and-play database adapter (supports MongoDB and SQL databases)");
        Map<String, Object> dbSchema = new HashMap<>();
        dbSchema.put("dbType", createSelectField("Database Type", "mongodb",
            List.of(Map.of("value", "mongodb", "label", "MongoDB (NoSQL)"),
                    Map.of("value", "sql", "label", "SQL (PostgreSQL/MySQL)")),
            "Database technology type"));
        dbSchema.put("connectionString", createField("Connection URI", "text", "mongodb://localhost:27017/javaflow", "Database connection string"));
        dbSchema.put("query", createField("Database Query / Command", "textarea", "db.users.find({})", "Database query to run"));
        dbSchema.put("associationMode", createSelectField("Association Mode", "single",
            List.of(Map.of("value", "single", "label", "Single Service Attachment"),
                    Map.of("value", "shared", "label", "Shared Connection Pool")),
            "How this DB adapter is shared across Spring microservices"));
        dbAdapter.setConfigSchema(dbSchema);

        // 4. Configuration Processor Node
        NodeType configProcessor = new NodeType();
        configProcessor.setName("configuration-processor");
        configProcessor.setDescription("Injects environment variables and parameters from application.yml");
        Map<String, Object> configProcSchema = new HashMap<>();
        configProcSchema.put("envVariables", createField("Environment Variables", "textarea", "DATABASE_PASSWORD=secret\nPORT=8081", "Environment variables to inject (KEY=VALUE)"));
        configProcSchema.put("injectFromYml", createField("Inject from application.yml", "boolean", true, "Automatically read and bind variables from application.yml"));
        configProcessor.setConfigSchema(configProcSchema);

        // 5. Scheduler Node
        NodeType scheduler = new NodeType();
        scheduler.setName("scheduler");
        scheduler.setDescription("Triggers executions periodically via cron expressions or fixed intervals");
        Map<String, Object> schedulerSchema = new HashMap<>();
        schedulerSchema.put("triggerType", createSelectField("Trigger Type", "interval",
            List.of(Map.of("value", "interval", "label", "Fixed Interval"),
                    Map.of("value", "cron", "label", "Cron Expression")),
            "Timing model"));
        schedulerSchema.put("cronExpression", createField("Cron Expression", "text", "0 */5 * * * *", "Standard 5/6 field cron expression"));
        schedulerSchema.put("intervalSeconds", createField("Interval (Seconds)", "text", "60", "Run execution every N seconds"));
        scheduler.setConfigSchema(schedulerSchema);

        // 6. Consumer Node
        NodeType consumer = new NodeType();
        consumer.setName("consumer");
        consumer.setDescription("Subscribes to messages from RabbitMQ/Kafka brokers");
        Map<String, Object> consumerSchema = new HashMap<>();
        consumerSchema.put("brokerType", createSelectField("Broker Type", "rabbitmq",
            List.of(Map.of("value", "rabbitmq", "label", "RabbitMQ"),
                    Map.of("value", "kafka", "label", "Apache Kafka")),
            "Messaging broker technology"));
        consumerSchema.put("queueName", createField("Queue / Topic Name", "text", "orders-queue", "Name of queue or topic to listen on"));
        consumer.setConfigSchema(consumerSchema);

        // 7. Producer Node
        NodeType producer = new NodeType();
        producer.setName("producer");
        producer.setDescription("Publishes messages to RabbitMQ/Kafka brokers");
        Map<String, Object> producerSchema = new HashMap<>();
        producerSchema.put("brokerType", createSelectField("Broker Type", "rabbitmq",
            List.of(Map.of("value", "rabbitmq", "label", "RabbitMQ"),
                    Map.of("value", "kafka", "label", "Apache Kafka")),
            "Messaging broker technology"));
        producerSchema.put("destination", createField("Exchange / Topic Name", "text", "orders-exchange", "Target exchange/topic"));
        producerSchema.put("routingKey", createField("Routing / Partition Key", "text", "orders.new", "Routing or partition key"));
        producer.setConfigSchema(producerSchema);

        // 8. Business Logic Node
        NodeType bizLogic = new NodeType();
        bizLogic.setName("business-logic");
        bizLogic.setDescription("Executes custom business logic with embedded code snippets");
        Map<String, Object> bizSchema = new HashMap<>();
        bizSchema.put("language", createSelectField("Language", "javascript",
            List.of(Map.of("value", "javascript", "label", "JavaScript (GraalVM)"),
                    Map.of("value", "java", "label", "Java Expression")),
            "Scripting language to use"));
        bizSchema.put("code", createField("Code Snippet", "textarea", "function execute(input) {\n  // write business logic here\n  return { success: true, data: input };\n}", "Logic to run"));
        bizLogic.setConfigSchema(bizSchema);

        // 9. Keep existing default types for backwards compatibility or visual flow
        NodeType httpRequest = new NodeType();
        httpRequest.setName("http-request");
        httpRequest.setDescription("Sends HTTP requests to external services");
        Map<String, Object> hrSchema = new HashMap<>();
        hrSchema.put("url", createField("URL", "text", "https://api.example.com", "HTTP request destination"));
        hrSchema.put("method", createSelectField("Method", "GET",
            List.of(Map.of("value", "GET", "label", "GET"),
                    Map.of("value", "POST", "label", "POST"),
                    Map.of("value", "PUT", "label", "PUT"),
                    Map.of("value", "DELETE", "label", "DELETE")),
            "HTTP verb"));
        httpRequest.setConfigSchema(hrSchema);

        NodeType delay = new NodeType();
        delay.setName("delay");
        delay.setDescription("Delays execution of the next step");
        Map<String, Object> delaySchema = new HashMap<>();
        delaySchema.put("delayMs", createField("Delay (ms)", "text", "1000", "Duration to pause execution"));
        delay.setConfigSchema(delaySchema);

        NodeType filter = new NodeType();
        filter.setName("filter");
        filter.setDescription("Filters incoming pipeline payloads");
        Map<String, Object> filterSchema = new HashMap<>();
        filterSchema.put("expression", createField("Filter Expression", "text", "input.value > 10", "Boolean JavaScript expression"));
        filter.setConfigSchema(filterSchema);

        // 10. Routing Node
        NodeType routingNode = new NodeType();
        routingNode.setName("routing-node");
        routingNode.setDescription("Routes WebFlux HTTP requests to different handler nodes");
        Map<String, Object> routingSchema = new HashMap<>();
        routingSchema.put("path", createField("Route Path", "text", "/api/orders", "HTTP path mapping"));
        routingSchema.put("method", createSelectField("HTTP Method", "POST",
            List.of(Map.of("value", "GET", "label", "GET"),
                    Map.of("value", "POST", "label", "POST"),
                    Map.of("value", "PUT", "label", "PUT"),
                    Map.of("value", "DELETE", "label", "DELETE")),
            "HTTP method to route"));
        routingSchema.put("handlerMethod", createField("Handler Method", "text", "createOrder", "Name of method in Spring handler class"));
        routingNode.setConfigSchema(routingSchema);

        // 11. Configuration Node
        NodeType configNode = new NodeType();
        configNode.setName("configuration-node");
        configNode.setDescription("Defines Spring @Configuration beans and settings");
        Map<String, Object> configNodeSchema = new HashMap<>();
        configNodeSchema.put("className", createField("Class Name", "text", "AppConfig", "Name of configuration class"));
        configNodeSchema.put("beans", createField("Beans / Code", "textarea", "@Bean\npublic RestTemplate restTemplate() {\n  return new RestTemplate();\n}", "Spring @Bean methods"));
        configNode.setConfigSchema(configNodeSchema);

        // 12. Service Node
        NodeType serviceNode = new NodeType();
        serviceNode.setName("service-node");
        serviceNode.setDescription("Defines Spring @Service layer business logic");
        Map<String, Object> serviceSchema = new HashMap<>();
        serviceSchema.put("serviceName", createField("Service Name", "text", "OrderService", "Name of service class"));
        serviceSchema.put("methods", createField("Methods / Business Logic", "textarea", "public Mono<Order> save(Order order) {\n  return orderRepository.save(order);\n}", "Service class methods"));
        serviceNode.setConfigSchema(serviceSchema);

        // 13. Component Node
        NodeType componentNode = new NodeType();
        componentNode.setName("component-node");
        componentNode.setDescription("Defines Spring @Component utility methods");
        Map<String, Object> componentSchema = new HashMap<>();
        componentSchema.put("componentName", createField("Component Name", "text", "OrderValidator", "Name of component class"));
        componentSchema.put("methods", createField("Methods / Code", "textarea", "public boolean isValid(Order order) {\n  return order.getAmount() > 0;\n}", "Component class methods"));
        componentNode.setConfigSchema(componentSchema);

        // 14. Handler Node
        NodeType handlerNode = new NodeType();
        handlerNode.setName("handler-node");
        handlerNode.setDescription("Defines WebFlux request handlers to call the Facade layer");
        Map<String, Object> handlerSchema = new HashMap<>();
        handlerSchema.put("handlerName", createField("Handler Name", "text", "PostHandler", "Name of handler class"));
        handlerSchema.put("methods", createField("Methods / Logic", "textarea", "public Mono<ServerResponse> getPostById(ServerRequest request) {\n  String id = request.pathVariable(\"id\");\n  return postFacade.getPost(id)\n    .flatMap(post -> ServerResponse.ok().bodyValue(post));\n}", "Handler methods"));
        handlerNode.setConfigSchema(handlerSchema);

        // 15. Facade Node
        NodeType facadeNode = new NodeType();
        facadeNode.setName("facade-node");
        facadeNode.setDescription("Defines @Component facade layer delegating to services");
        Map<String, Object> facadeSchema = new HashMap<>();
        facadeSchema.put("facadeName", createField("Facade Name", "text", "PostFacade", "Name of facade class"));
        facadeSchema.put("methods", createField("Methods / Code", "textarea", "public Mono<Post> getPost(String id) {\n  return postService.getPostById(id);\n}", "Facade methods"));
        facadeNode.setConfigSchema(facadeSchema);

        // 16. Entity Node
        NodeType entityNode = new NodeType();
        entityNode.setName("entity-node");
        entityNode.setDescription("Defines POJO classes with Lombok and builder annotations");
        Map<String, Object> entitySchema = new HashMap<>();
        entitySchema.put("entityName", createField("Entity Name", "text", "Post", "Name of entity class"));
        entitySchema.put("attributes", createField("Attributes / Fields", "textarea", "private String id;\nprivate String title;\nprivate String body;\nprivate Integer userId;", "Fields declaration"));
        entitySchema.put("lombokAnnotations", createField("Lombok Annotations", "text", "@Data\n@Builder\n@NoArgsConstructor\n@AllArgsConstructor", "Lombok annotations to prepend"));
        entityNode.setConfigSchema(entitySchema);

        // 17. POM XML Node
        NodeType pomXmlNode = new NodeType();
        pomXmlNode.setName("pom-xml-node");
        pomXmlNode.setDescription("Defines and renders pom.xml maven configuration");
        Map<String, Object> pomSchema = new HashMap<>();
        pomSchema.put("content", createField("POM Content", "textarea", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n    <modelVersion>4.0.0</modelVersion>\n    <groupId>com.example</groupId>\n    <artifactId>posts-service</artifactId>\n    <version>0.0.1-SNAPSHOT</version>\n    <parent>\n        <groupId>org.springframework.boot</groupId>\n        <artifactId>spring-boot-starter-parent</artifactId>\n        <version>3.2.0</version>\n    </parent>\n    <dependencies>\n        <dependency>\n            <groupId>org.springframework.boot</groupId>\n            <artifactId>spring-boot-starter-webflux</artifactId>\n        </dependency>\n        <dependency>\n            <groupId>org.springframework.boot</groupId>\n            <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>\n        </dependency>\n        <dependency>\n            <groupId>org.projectlombok</groupId>\n            <artifactId>lombok</artifactId>\n            <optional>true</optional>\n        </dependency>\n    </dependencies>\n</project>", "Maven pom.xml definition"));
        pomXmlNode.setConfigSchema(pomSchema);

        // 18. Repository Node
        NodeType repositoryNode = new NodeType();
        repositoryNode.setName("repository-node");
        repositoryNode.setDescription("Defines Database interface repositories extending CrudRepository");
        Map<String, Object> repoSchema = new HashMap<>();
        repoSchema.put("repositoryName", createField("Repository Name", "text", "PostRepository", "Name of repository interface"));
        repoSchema.put("extendsInterface", createField("Extends", "text", "ReactiveMongoRepository<Post, String>", "Parent repository interface"));
        repoSchema.put("methods", createField("Custom Methods", "textarea", "// Custom database queries", "Repository methods"));
        repositoryNode.setConfigSchema(repoSchema);

        // 19. WebClient Node
        NodeType webClientNode = new NodeType();
        webClientNode.setName("webclient-node");
        webClientNode.setDescription("Defines reactive WebClient HTTP call templates");
        Map<String, Object> wcSchema = new HashMap<>();
        wcSchema.put("clientName", createField("Client Name", "text", "PostWebClient", "Name of client class"));
        wcSchema.put("url", createField("Target URL", "text", "https://jsonplaceholder.typicode.com/posts/{id}", "Endpoint URL"));
        wcSchema.put("methods", createField("Methods / Code", "textarea", "public Mono<Post> fetchPost(String id) {\n  return webClient.get().uri(\"/posts/\" + id).retrieve().bodyToMono(Post.class);\n}", "HTTP request methods"));
        webClientNode.setConfigSchema(wcSchema);

        return Flux.just(mapper, converter, dbAdapter, configProcessor, scheduler, consumer, producer, bizLogic, httpRequest, delay, filter, routingNode, configNode, serviceNode, componentNode, handlerNode, facadeNode, entityNode, pomXmlNode, repositoryNode, webClientNode)
            .flatMap(nodeTypeRepository::save)
            .then()
            .doOnSuccess(v -> log.info("Successfully seeded default node types."));
    }

    private Map<String, Object> createField(String label, String type, Object defaultValue, String description) {
        Map<String, Object> field = new HashMap<>();
        field.put("label", label);
        field.put("type", type);
        field.put("defaultValue", defaultValue);
        field.put("description", description);
        return field;
    }

    private Map<String, Object> createSelectField(String label, Object defaultValue, List<Map<String, String>> options, String description) {
        Map<String, Object> field = createField(label, "select", defaultValue, description);
        field.put("options", options);
        return field;
    }

    private Mono<Void> seedSchoolWorkflow() {
        Workflow school = new Workflow();
        school.setName("School");
        school.setDescription("School CRUD operations for Classrooms, Teachers, and Students");
        school.setActive(true);

        // 1. Database Adapter
        Node dbAdapter = new Node();
        dbAdapter.setId("node-db");
        dbAdapter.setNodeTypeId("database-adapter");
        dbAdapter.setName("School DB Adapter");
        dbAdapter.setConfig(Map.of(
            "databaseType", "mongodb",
            "connectionUri", "mongodb://mongo:27017/school",
            "databaseName", "school",
            "description", "Classrooms, Teachers, and Students collection mappings"
        ));
        Node.Position posDb = new Node.Position();
        posDb.setX(100); posDb.setY(100);
        dbAdapter.setPosition(posDb);

        // 2. Classrooms Router
        Node classroomsRouter = new Node();
        classroomsRouter.setId("node-classroom-route");
        classroomsRouter.setNodeTypeId("routing-node");
        classroomsRouter.setName("Classrooms Route");
        classroomsRouter.setConfig(Map.of(
            "path", "/api/classrooms",
            "method", "POST",
            "handlerMethod", "updateClassroom"
        ));
        Node.Position posCr = new Node.Position();
        posCr.setX(100); posCr.setY(300);
        classroomsRouter.setPosition(posCr);

        // 3. Teachers Router
        Node teachersRouter = new Node();
        teachersRouter.setId("node-teacher-route");
        teachersRouter.setNodeTypeId("routing-node");
        teachersRouter.setName("Teachers Route");
        teachersRouter.setConfig(Map.of(
            "path", "/api/teachers",
            "method", "POST",
            "handlerMethod", "updateTeacher"
        ));
        Node.Position posTr = new Node.Position();
        posTr.setX(100); posTr.setY(450);
        teachersRouter.setPosition(posTr);

        // 4. Students Router
        Node studentsRouter = new Node();
        studentsRouter.setId("node-student-route");
        studentsRouter.setNodeTypeId("routing-node");
        studentsRouter.setName("Students Route");
        studentsRouter.setConfig(Map.of(
            "path", "/api/students",
            "method", "POST",
            "handlerMethod", "updateStudent"
        ));
        Node.Position posSr = new Node.Position();
        posSr.setX(100); posSr.setY(600);
        studentsRouter.setPosition(posSr);

        // 5. SchoolService
        Node schoolService = new Node();
        schoolService.setId("node-school-service");
        schoolService.setNodeTypeId("service-node");
        schoolService.setName("SchoolService");
        schoolService.setConfig(Map.of(
            "serviceName", "SchoolService",
            "methods", "public Mono<Classroom> updateClassroom(Classroom cr) {\n  return mongoTemplate.save(cr);\n}\npublic Mono<Teacher> updateTeacher(Teacher t) {\n  return mongoTemplate.save(t);\n}\npublic Mono<Student> updateStudent(Student s) {\n  return mongoTemplate.save(s);\n}"
        ));
        Node.Position posService = new Node.Position();
        posService.setX(450); posService.setY(300);
        schoolService.setPosition(posService);

        // 6. RelationshipValidator
        Node validator = new Node();
        validator.setId("node-validator");
        validator.setNodeTypeId("component-node");
        validator.setName("RelationshipValidator");
        validator.setConfig(Map.of(
            "componentName", "RelationshipValidator",
            "methods", "public boolean validateRelationships(Student s, Classroom c, Teacher t) {\n  // student belongs to class and can learn from teachers\n  return s.getClassroomId().equals(c.getId()) && t.getStudents().contains(s.getId());\n}"
        ));
        Node.Position posVal = new Node.Position();
        posVal.setX(450); posVal.setY(500);
        validator.setPosition(posVal);

        school.setNodes(List.of(dbAdapter, classroomsRouter, teachersRouter, studentsRouter, schoolService, validator));

        // Edges
        Edge edge1 = new Edge();
        edge1.setId("edge-cr-service");
        edge1.setSource("node-classroom-route");
        edge1.setTarget("node-school-service");

        Edge edge2 = new Edge();
        edge2.setId("edge-tr-service");
        edge2.setSource("node-teacher-route");
        edge2.setTarget("node-school-service");

        Edge edge3 = new Edge();
        edge3.setId("edge-sr-service");
        edge3.setSource("node-student-route");
        edge3.setTarget("node-school-service");

        Edge edge4 = new Edge();
        edge4.setId("edge-service-db");
        edge4.setSource("node-school-service");
        edge4.setTarget("node-db");

        Edge edge5 = new Edge();
        edge5.setId("edge-service-val");
        edge5.setSource("node-school-service");
        edge5.setTarget("node-validator");

        school.setEdges(List.of(edge1, edge2, edge3, edge4, edge5));

        return workflowRepository.save(school)
            .doOnSuccess(w -> log.info("Successfully seeded 'School' workflow in database."))
            .then();
    }

    private Mono<Void> seedPostsWorkflow() {
        Workflow posts = new Workflow();
        posts.setName("Posts");
        posts.setDescription("Fetches posts from placeholder API and saves them to MongoDB admin collection");
        posts.setActive(true);

        // 1. POM XML Node
        Node pomNode = new Node();
        pomNode.setId("node-posts-pom");
        pomNode.setNodeTypeId("pom-xml-node");
        pomNode.setName("pom.xml");
        pomNode.setConfig(Map.of(
            "content", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n    <modelVersion>4.0.0</modelVersion>\n    <groupId>com.example</groupId>\n    <artifactId>posts-service</artifactId>\n    <version>0.0.1-SNAPSHOT</version>\n    <parent>\n        <groupId>org.springframework.boot</groupId>\n        <artifactId>spring-boot-starter-parent</artifactId>\n        <version>3.2.0</version>\n    </parent>\n    <dependencies>\n        <dependency>\n            <groupId>org.springframework.boot</groupId>\n            <artifactId>spring-boot-starter-webflux</artifactId>\n        </dependency>\n        <dependency>\n            <groupId>org.springframework.boot</groupId>\n            <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>\n        </dependency>\n        <dependency>\n            <groupId>org.projectlombok</groupId>\n            <artifactId>lombok</artifactId>\n            <optional>true</optional>\n        </dependency>\n    </dependencies>\n</project>"
        ));
        Node.Position posPom = new Node.Position();
        posPom.setX(100); posPom.setY(100);
        pomNode.setPosition(posPom);

        // 2. GetPostsById Router Node
        Node routeNode = new Node();
        routeNode.setId("node-posts-route");
        routeNode.setNodeTypeId("routing-node");
        routeNode.setName("GetPostsById Route");
        routeNode.setConfig(Map.of(
            "path", "/api/posts/{id}",
            "method", "GET",
            "handlerMethod", "getPostById"
        ));
        Node.Position posRoute = new Node.Position();
        posRoute.setX(100); posRoute.setY(300);
        routeNode.setPosition(posRoute);

        // 3. PostHandler Node
        Node handlerNode = new Node();
        handlerNode.setId("node-posts-handler");
        handlerNode.setNodeTypeId("handler-node");
        handlerNode.setName("PostHandler");
        handlerNode.setConfig(Map.of(
            "handlerName", "PostHandler",
            "methods", "public Mono<ServerResponse> getPostById(ServerRequest request) {\n  String id = request.pathVariable(\"id\");\n  return postFacade.getPost(id)\n    .flatMap(post -> ServerResponse.ok().bodyValue(post));\n}"
        ));
        Node.Position posHandler = new Node.Position();
        posHandler.setX(300); posHandler.setY(300);
        handlerNode.setPosition(posHandler);

        // 4. PostFacade Node
        Node facadeNode = new Node();
        facadeNode.setId("node-posts-facade");
        facadeNode.setNodeTypeId("facade-node");
        facadeNode.setName("PostFacade");
        facadeNode.setConfig(Map.of(
            "facadeName", "PostFacade",
            "methods", "public Mono<Post> getPost(String id) {\n  return postService.getPostById(id);\n}"
        ));
        Node.Position posFacade = new Node.Position();
        posFacade.setX(500); posFacade.setY(300);
        facadeNode.setPosition(posFacade);

        // 5. PostService Node
        Node serviceNode = new Node();
        serviceNode.setId("node-posts-service");
        serviceNode.setNodeTypeId("service-node");
        serviceNode.setName("PostService");
        serviceNode.setConfig(Map.of(
            "serviceName", "PostService",
            "methods", "public Mono<Post> getPostById(String id) {\n  log.debug(\"Fetching post for id: {}\", id);\n  return postWebClient.fetchPost(id)\n    .flatMap(post -> postRepository.save(post));\n}"
        ));
        Node.Position posService = new Node.Position();
        posService.setX(700); posService.setY(300);
        serviceNode.setPosition(posService);

        // 6. PostWebClient Node
        Node webclientNode = new Node();
        webclientNode.setId("node-posts-webclient");
        webclientNode.setNodeTypeId("webclient-node");
        webclientNode.setName("PostWebClient");
        webclientNode.setConfig(Map.of(
            "clientName", "PostWebClient",
            "url", "https://jsonplaceholder.typicode.com/posts/{id}",
            "methods", "public Mono<Post> fetchPost(String id) {\n  return webClient.get().uri(\"/posts/\" + id).retrieve().bodyToMono(Post.class);\n}"
        ));
        Node.Position posWc = new Node.Position();
        posWc.setX(900); posWc.setY(150);
        webclientNode.setPosition(posWc);

        // 7. PostRepository Node
        Node repoNode = new Node();
        repoNode.setId("node-posts-repo");
        repoNode.setNodeTypeId("repository-node");
        repoNode.setName("PostRepository");
        repoNode.setConfig(Map.of(
            "repositoryName", "PostRepository",
            "extendsInterface", "ReactiveMongoRepository<Post, String>",
            "methods", "// Custom database queries"
        ));
        Node.Position posRepo = new Node.Position();
        posRepo.setX(900); posRepo.setY(350);
        repoNode.setPosition(posRepo);

        // 8. Post Entity Node
        Node entityNode = new Node();
        entityNode.setId("node-posts-entity");
        entityNode.setNodeTypeId("entity-node");
        entityNode.setName("Post");
        entityNode.setConfig(Map.of(
            "entityName", "Post",
            "attributes", "private String id;\nprivate String title;\nprivate String body;\nprivate Integer userId;",
            "lombokAnnotations", "@Data\n@Builder\n@NoArgsConstructor\n@AllArgsConstructor"
        ));
        Node.Position posEntity = new Node.Position();
        posEntity.setX(900); posEntity.setY(500);
        entityNode.setPosition(posEntity);

        // 9. Database Adapter Node (admin db)
        Node dbNode = new Node();
        dbNode.setId("node-posts-db");
        dbNode.setNodeTypeId("database-adapter");
        dbNode.setName("Admin DB Adapter");
        dbNode.setConfig(Map.of(
            "databaseType", "mongodb",
            "connectionString", "mongodb://mongo:27017/admin",
            "databaseName", "admin",
            "description", "Ingests JSON posts under the admin collection"
        ));
        Node.Position posDb = new Node.Position();
        posDb.setX(1120); posDb.setY(350);
        dbNode.setPosition(posDb);

        posts.setNodes(List.of(pomNode, routeNode, handlerNode, facadeNode, serviceNode, webclientNode, repoNode, entityNode, dbNode));

        // Edges linking the pipeline
        Edge e1 = new Edge();
        e1.setId("edge-posts-route-handler");
        e1.setSource("node-posts-route");
        e1.setTarget("node-posts-handler");

        Edge e2 = new Edge();
        e2.setId("edge-posts-handler-facade");
        e2.setSource("node-posts-handler");
        e2.setTarget("node-posts-facade");

        Edge e3 = new Edge();
        e3.setId("edge-posts-facade-service");
        e3.setSource("node-posts-facade");
        e3.setTarget("node-posts-service");

        Edge e4 = new Edge();
        e4.setId("edge-posts-service-webclient");
        e4.setSource("node-posts-service");
        e4.setTarget("node-posts-webclient");

        Edge e5 = new Edge();
        e5.setId("edge-posts-service-repo");
        e5.setSource("node-posts-service");
        e5.setTarget("node-posts-repo");

        Edge e6 = new Edge();
        e6.setId("edge-posts-repo-db");
        e6.setSource("node-posts-repo");
        e6.setTarget("node-posts-db");

        Edge e7 = new Edge();
        e7.setId("edge-posts-repo-entity");
        e7.setSource("node-posts-repo");
        e7.setTarget("node-posts-entity");

        posts.setEdges(List.of(e1, e2, e3, e4, e5, e6, e7));

        return workflowRepository.save(posts)
            .doOnSuccess(w -> log.info("Successfully seeded 'Posts' workflow in database."))
            .then();
    }
}
