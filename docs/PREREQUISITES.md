# 🛠️ Installation Prerequisites

Before launching JavaFlow Visual Studio, ensure that your local machine meets the following environment specifications:

---

## 💻 System Software Requirements

### 1. Docker & Container Runtime
Since JavaFlow compiles Java code and runs dependency synchronization in the background, Docker is the recommended runtime environment.
- **Docker Engine**: Version 24.0.0 or higher.
- **Docker Compose**: Version 2.20.0 or higher.
- **Docker Desktop Permissions**:
  > [!IMPORTANT]
  > Ensure that file sharing is enabled in your Docker Desktop settings (Preferences -> Resources -> File Sharing) for your home directory (e.g. `/Users/kathi.s`), as the exporter mounts host folders to compile Java source files and cache Maven dependencies.

### 2. Native Toolchain (For Bare-Metal Runs)
If you prefer running the frontend React server and Spring Boot API directly on your host machine without Docker:
- **Java Development Kit (JDK)**: Version 17 or higher (Eclipse Temurin is recommended).
- **Apache Maven**: Version 3.8.0 or higher.
- **Node.js**: Version 18.0.0 or higher (LTS recommended) and **npm** 9.0.0+.

---

## 🛜 Network & Port Mapping

Ensure that the following local ports are unoccupied before launching:
- **Port `3000`**: Exposes the React-based frontend Visual Studio application.
- **Port `8080`**: Exposes the Spring WebFlux REST backend API.
- **Port `27017`**: Exposes the MongoDB metadata server (used for persistence).

---

## 🔍 Verifying Installation

Verify that your command-line tools are correctly configured by running:
```bash
# Verify Docker is running and responsive
docker ps

# Verify Java Compiler (if running locally)
javac -version

# Verify Node.js (if running locally)
node --version
npm --version
```
