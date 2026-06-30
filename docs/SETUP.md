# 🚀 Setup and Installation Guide

Follow these steps to set up and run JavaFlow Visual Studio locally or inside containers.

---

## 🐳 Containerized Setup (Recommended)

Docker Compose starts the backend, frontend, and MongoDB databases instantly, with volume mounts configured to enable local file exports.

### 1. Build and Start the Stack
From the project root directory, run:
```bash
docker-compose up -d --build
```
This command builds the Spring WebFlux environment, compiles the React application bundle, starts the MongoDB container, and launches Nginx to serve the static frontend.

### 2. Verify Containers
Check that all three containers are active:
```bash
docker-compose ps
```

---

## 💻 Bare-Metal Setup (Host Machine)

If you prefer to run the components individually:

### 1. Start MongoDB
Run a local MongoDB instance:
```bash
docker run -d -p 27017:27017 --name javaflow-mongo mongo:7.0
```

### 2. Run backend
Navigate to the `backend` folder and boot the Spring Boot application:
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```
The REST API will bind to [http://localhost:8080](http://localhost:8080).

### 3. Run frontend
Navigate to the `frontend` folder and start the React dev server:
```bash
cd frontend
npm install
npm start
```
The browser will automatically open the dashboard at [http://localhost:3000](http://localhost:3000).

---

## 📂 Exporting Java Files on the Host

When you design a workflow (e.g. **Posts**) and click **Save Workflow**:
1. The studio prompts you to enter a target export path (defaults to `/Users/kathi.s/JF/Posts`).
2. The backend generates the Java class structure, writes them to the path, and fires a Maven resolve command to download dependencies to `~/.m2`.
3. Ensure the target path folder has write permissions so the container can write to your local directory.
