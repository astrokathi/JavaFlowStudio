# JavaFlow - Workflow Management System

A Spring WebFlux-based workflow middleware system with a visual editor similar to n8n.

## Features

- Visual workflow designer with drag-and-drop interface
- Reusable node templates with configurable properties
- Support for various node types:
  - HTTP Requests (REST API calls)
  - Database operations (MongoDB)
  - Data transformation (JavaScript via GraalVM)
  - Flow control (split, merge, delay, filter)
  - Notifications (email, webhook)
  - Custom scripts
- Workflow execution with detailed logging
- Environment-specific configuration
- Docker containerization
- RESTful API backend

## Architecture

Follows Clean Architecture principles:
- **Models**: Domain entities with MongoDB mappings
- **Repositories**: Data access layer
- **Services**: Business logic and orchestration
- **Controllers**: HTTP request/response handling
- **Routes**: API endpoint definitions

## Technology Stack

### Backend
- Java 17
- Spring Boot 3.2.0
- Spring WebFlux (Reactive)
- MongoDB Reactive
- Lombok
- GraalVM JavaScript Engine
- Docker

### Frontend
- React 18
- React Flow (for workflow visualization)
- Axios (HTTP client)
- Docker/Nginx

## Getting Started

### Prerequisites
- Docker and Docker Compose
- Java 17+ (if running locally)
- Node.js 18+ (if running locally)

### Running with Docker (Recommended)
```bash
docker-compose up --build
```

The application will be available at:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api
- API Documentation: http://localhost:8080/api-docs

### Running Locally
1. Start MongoDB: `docker run -d -p 27017:27017 --name mongo mongo:7`
2. Start backend: `./mvnw spring-boot:run` (from backend directory)
3. Start frontend: `npm start` (from frontend directory)

## API Endpoints

### Workflows
- `GET /api/workflows` - Get all workflows
- `GET /api/workflows/{id}` - Get workflow by ID
- `POST /api/workflows` - Create new workflow
- `PUT /api/workflows/{id}` - Update workflow
- `DELETE /api/workflows/{id}` - Delete workflow

### Node Types
- `GET /api/node-types` - Get all node types
- `GET /api/node-types/{id}` - Get node type by ID
- `POST /api/node-types` - Create new node type
- `PUT /api/node-types/{id}` - Update node type
- `DELETE /api/node-types/{id}` - Delete node type

### Execution Logs
- `GET /api/execution-logs` - Get all execution logs
- `GET /api/execution-logs/{id}` - Get execution log by ID
- `GET /api/execution-logs/workflow/{workflowId}` - Get logs by workflow
- `GET /api/execution-logs/execution/{executionId}` - Get logs by execution

## Development

### Backend
```bash
cd backend
./mvnw test
```

### Frontend
```bash
cd frontend
npm test
```

## License

MIT License
