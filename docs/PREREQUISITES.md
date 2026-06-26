# Prerequisites

## Software Requirements

### Backend (Java/Spring Boot)
- **Java Development Kit (JDK)**: Version 17 or higher
  - Download: https://adoptium.net/
  - Verify installation: `java -version`
- **Apache Maven**: Version 3.8 or higher
  - Download: https://maven.apache.org/download.cgi
  - Verify installation: `mvn -v`

### Frontend (React)
- **Node.js**: Version 18 or higher (LTS recommended)
  - Download: https://nodejs.org/
  - Verify installation: `node --version` and `npm --version`
- **Git**: Version 2.0 or higher
  - Download: https://git-scm.com/
  - Verify installation: `git --version`

### Development Tools
- **IDE**: IntelliJ IDEA Ultimate/Community Edition, VS Code, or Eclipse
- **API Client**: Postman or curl for testing endpoints
- **Database Client**: MongoDB Compass (if using MongoDB)
- **Container Runtime**: Docker Desktop (for containerized deployment)
  - Docker version: 20.10 or higher
  - Docker Compose version: 2.0 or higher

### Optional but Recommended
- **Linux Subsystem**: WSL2 (Windows) or native Linux for better Docker performance
- **IDE Extensions**: 
  - Java: Language Support for Java(TM) by Red Hat (VS Code)
  - Spring: Spring Boot Extension Pack (VS Code)
  - React: ES7+ React/Redux/React-Native snippets (VS Code)
  - ESLint and Prettier for code formatting

## System Requirements
- **Minimum**: 4GB RAM, 2 CPU cores, 10GB disk space
- **Recommended**: 8GB RAM, 4 CPU cores, 20GB SSD storage
- **Operating System**: Windows 10/11, macOS 12+, or Linux (Ubuntu 20.04+/CentOS 7+)

## Network Requirements
- Internet access for dependency downloads
- Port 8080 available for backend (configurable)
- Port 3000 available for frontend (configurable)
- Port 27017 available for MongoDB (if using local instance)

## Verification Steps
After installing prerequisites, verify your setup:

```bash
# Check Java
java -version

# Check Maven
mvn -v

# Check Node.js and npm
node --version
npm --version

# Check Git
git --version

# Check Docker (if installed)
docker --version
docker-compose --version
```
