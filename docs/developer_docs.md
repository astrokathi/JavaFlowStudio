# JavaFlow Developer Documentation & Node Directory

Welcome to the **JavaFlow Visual Studio Developer Documentation**. This guide provides an in-depth breakdown of the system features, available visual nodes, configuration schemas, and instructions on how to extend the platform.

---

## 🚀 Core Features

JavaFlow Visual Studio is an agentic, low-code platform designed to visually compose, test, and export reactive, non-blocking **Spring WebFlux Java Microservices**.

1. **Reactive Node Graph Engine**: Uses a drag-and-drop React Flow canvas to connect endpoints, handlers, services, external clients, and databases topologically.
2. **Dynamic Java WebFlux Code Generator**: Instantly generates full Spring Boot directory structures, compile-ready configurations (`application.yml`), handlers, services, interfaces, Lombok entities, and `pom.xml`.
3. **Maven Dependency Caching**: Integrates with the host system using Docker volume mounts, executing `mvn dependency:go-offline` dynamically and caching libraries directly to the developer's local `~/.m2` directory.
4. **Live Integration test runner**: Execute real workflow pipelines against live APIs in memory and view execution steps, outputs, and database operations in real-time.
5. **Interactive Console Logs**: Read live logs from compilers, maven builds, and runtime execution stacks within the terminal console panel.
6. **Workflow JSON Portability**: Export and import complete workflow graphs as simple `.json` definition files.

---

## 🧩 Architectural Node Directory

Here is an overview of the available standard visual nodes and how to configure them for WebFlux projects.

### 1. Webflux Router / Webhook Nodes
- **`routing-node`**: Specifies the HTTP endpoint mapping.
  - **Path**: e.g., `/api/posts/{id}`
  - **Method**: `GET`, `POST`, `PUT`, `DELETE`
  - **Handler Method**: The name of the handler function to map to.
- **`webhook`**: Custom trigger endpoint.
  - **Path**: Webhook listener endpoint.

### 2. Request Dispatch & Facade Nodes
- **`handler-node`**: Extracted endpoint controller that handles input ServerRequest arguments.
  - **Handler Name**: e.g., `PostHandler`
  - **Methods**: Declared router-to-handler function signature mapping.
- **`facade-node`**: Decouples the presentation layer from the service layer.
  - **Facade Name**: e.g., `PostFacade`
  - **Methods**: Functions that wrap and delegate parameters to the service layer.

### 3. Business Logic Node
- **`service-node`**: Houses core business logic.
  - **Service Name**: e.g., `PostService`
  - **Defined Methods**: Handles reactive transformations and calls database repositories or external HTTP clients.

### 4. Integration Nodes
- **`webclient-node`**: Invokes external REST API endpoints reactively.
  - **Client Name**: e.g., `PostWebClient`
  - **Target URL**: The external API endpoint (e.g., `https://jsonplaceholder.typicode.com/posts/{id}`).
- **`pom-xml-node`**: Directly edits the Maven project dependency descriptor.
  - **Content**: Custom XML configurations. Synchronizes and downloads target jars to `~/.m2` upon save.

### 5. Repository & Database Nodes
- **`repository-node`**: Interface layer for MongoDB operations.
  - **Repository Name**: e.g., `PostRepository`
  - **Extends Interface**: Usually `ReactiveMongoRepository<Post, String>`.
- **`entity-node`**: Defines the document entity model class.
  - **Entity Name**: Name of the POJO class (e.g., `Post`).
  - **Lombok Annotations**: Automatically inserts `@Data`, `@Builder`, `@NoArgsConstructor`, and `@AllArgsConstructor`.
  - **Attributes**: Multi-line fields definitions, types, and annotations (e.g., `@NotNull`).
- **`database-adapter`**: Controls backend database connections.
  - **Type**: `mongodb`, `postgresql`
  - **Connection String**: Connection URL (e.g., `mongodb://mongo:27017/admin`).

### 6. Processing & Formatting Nodes
- **`mapper`**: Transforms and maps JSON keys from incoming nodes to output nodes.
  - **Mappings**: JSON schema key projections.
- **`converter`**: Formats inputs into desired serializations.
  - **From / To Formats**: `json`, `xml`, `csv`.
- **`component-node`**: Injectable custom Spring component helper class.
  - **Component Name**: e.g., `AppUtility`
  - **Methods**: Declared methods and logic.

### 7. Trigger & Message Nodes
- **`scheduler`**: Cron expression based scheduler trigger.
  - **Cron Expression**: e.g., `*/5 * * * *`
- **`consumer`**: Broker message listener.
  - **Broker Type**: `rabbitmq`, `kafka`
  - **Destination**: Queue / Topic name.
- **`producer`**: Broker publisher.
  - **Broker Type**: `rabbitmq`, `kafka`
  - **Routing Key / Destination**: e.g., `post.events`

---

## 🛠️ Adding Custom Node Types

You can register custom node templates directly inside the left side-panel:
1. Provide a unique **Name** and select a **Category** (Trigger, Processing, Adapter).
2. Add custom key-value configuration schemas.
3. Click **Add Node Type**. It will appear instantly under the **Custom Node Types** sidebar.
4. Custom templates can be deleted at any time by clicking the trash icon (`🗑️`), while standard system nodes remain protected.
