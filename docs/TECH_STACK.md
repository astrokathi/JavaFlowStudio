# 🛠️ Technology Stack Directory

Below is the verified technology stack map for the JavaFlow Visual Studio project:

---

## ☕ Backend Architecture

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Language** | Java | `17` (Temurin) | Core backend codebase |
| **App Framework** | Spring Boot | `3.2.0` | Container and inversion-of-control container |
| **Web Layer** | Spring WebFlux | `3.2.0` (Reactive) | Non-blocking HTTP routes and WebClients |
| **Database Connector** | Spring Data MongoDB Reactive | `3.2.0` | Reactive persistence transactions |
| **Boilerplate Helper** | Lombok | `1.18.30` | Auto-generates builders, getters, and constructors |
| **API Spec** | SpringDoc OpenAPI 3 | `1.6.14` | Exposes dynamic Swagger UI docs |
| **Build System** | Apache Maven | `3.9` | Handles dependency management and offline syncs |

---

## 🎨 Frontend Architecture

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Library** | React | `18.2.0` | User Interface rendering layer |
| **Graph Visuals** | React Flow | `11.11.4` | Node canvas and connector layouts |
| **HTTP Client** | Axios | `1.6.0` | API requests to Spring Boot |
| **Scaffolder** | Create React App | `5.0.1` | Asset bundling and webpack management |
| **Styling** | Vanilla CSS | N/A | Premium custom dark styles |

---

## 🐳 DevOps & Deployment

| Component | Technology | Version | Purpose |
| :--- | :--- | :--- | :--- |
| **Container Engine** | Docker | `24.0+` | Bundles runtime environments |
| **Orchestration** | Docker Compose | `2.20+` | Spawns local stack (Backend, Frontend, Mongo) |
| **Database** | MongoDB | `7.0` | Persists workflows and logs metadata |
| **Web Server** | Nginx | `alpine` | Serves React production static assets |
| **E2E Tester** | Playwright | `1.40+` | Runs browser automation test suites |
