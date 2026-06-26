# Setup and Installation

## Prerequisites
See [PREREQUISITES.md](./prerequisites.md) for detailed requirements.

## Quick Start

### Backend Setup
```bash
# Navigate to backend directory
cd backend

# Install dependencies (Maven Wrapper should handle this)
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The backend will start on http://localhost:8080

### Frontend Setup
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies are already installed during project creation
# npm install

# Start development server
npm start
```

The frontend will be available at http://localhost:3000

### Docker Setup
```bash
# From project root
docker-compose up --build

# To run in detached mode
docker-compose up -d --build

# To stop and remove containers
docker-compose down
```

## Database Setup
The application currently uses an in-memory database for development.
For production setup, configure MongoDB connection in application-{profile}.yml

## Environment Variables
Create a `.env` file in the root directory with the following variables:
```
# Server Configuration
SERVER_PORT=8080

# Database Configuration (example for MongoDB)
MONGODB_URI=mongodb://localhost:27017/javaflow
DATABASE_NAME=javaflow

# JWT Security
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION_MS=86400000

# External Service Integrations (examples)
SHAREPOINT_CLIENT_ID=your-client-id
SHAREPOINT_TENANT_ID=your-tenant-id
COUPA_API_KEY=your-coupa-api-key
```

## Verification
- Backend API health check: http://localhost:8080/actuator/health
- Frontend application: http://localhost:3000
- API Documentation (when enabled): http://localhost:8080/swagger-ui.html
