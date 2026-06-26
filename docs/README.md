# JavaFlow

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/spring-boot-%236DB33F.svg?style=for-the-badge&logo=spring-boot&logoColor=white)
![React](https://img.shields.io/badge/react-%2320232a.svg?style=for-the-badge&logo=react&logoColor=%2361DAFB)
![Node.js](https://img.shields.io/badge/node.js-%2343853D.svg?style=for-the-badge&logo=node.js&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-%234ea94b.svg?style=for-the-badge&logo=mongodb&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

Workflow middleware system based on Spring WebFlux and React.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- MongoDB (or use Docker Compose)

### Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd JavaFlow
   ```

2. **Install dependencies**
   ```bash
   # Backend
   cd backend
   mvn dependency:resolve
   
   # Frontend
   cd ../frontend
   npm install
   ```

3. **Start services**
   ```bash
   # Using Docker Compose (recommended for development)
   cd ..
   docker-compose up -d
   
   # Or manually:
   # Terminal 1: Backend
   cd backend
   mvn spring-boot:run
   
   # Terminal 2: Frontend  
   cd ../frontend
   npm start
   ```

4. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api
   - API Docs: http://localhost:8080/swagger-ui.html (when enabled)
   - MongoDB Express: http://localhost:8081 (if using docker-compose)

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

### End-to-End Tests (Playwright)
```bash
cd frontend
npx playwright test
```

### Test Coverage
- Backend: Unit & integration tests with JaCoCo
- Frontend: Jest unit tests + Playwright E2E tests

## 📚 Documentation

- [Setup Guide](SETUP.md) - Detailed installation and configuration
- [Prerequisites](PREREQUISITES.md) - System requirements
- [Architecture](ARCHITECTURE.md) - System design and components
- [Tech Stack](TECH_STACK.md) - Technologies and versions used
- [Context](context.md) - Project overview and goals
- [Progress Tracking](progress.md) - Development status and tracking

## 🐳 Docker Deployment

```bash
docker-compose up -d
```

Services will be available at:
- Backend API: http://localhost:8080
- Frontend: http://localhost:3000
- MongoDB: mongodb://localhost:27017
- MongoDB Express: http://localhost:8081

## 🔧 Configuration

Environment variables can be configured in:
- Backend: `backend/src/main/resources/application.yml` and `.env`
- Frontend: `.env` file in frontend directory

## 📈 API Documentation

When enabled, Swagger UI is available at:
http://localhost:8080/swagger-ui.html

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- React team for the UI library
- MongoDB for the document database
- Playwright team for end-to-end testing tools
