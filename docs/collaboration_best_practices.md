# Collaborative Development Best Practices - Employee Information System (EIS)

Welcome to the Employee Information System (EIS) codebase! This document outlines the technical standards, workflows, and best practices to ensure a clean, stable, and efficient development collaboration.

---

## 1. Local Environment & Setup

To ensure everyone has identical running environments and to prevent "works on my machine" issues:

- **Docker for Databases & Cache**: Do not install PostgreSQL or Redis directly on host systems. Use the provided [docker-compose.yml](file:///c:/Users/ACER%20PREDATOR/Desktop/ENSET%20MASTER/Pfe/EIS/docker-compose.yml):
  ```bash
  docker-compose up -d
  ```
- **Java Version Alignment**: Ensure your local IDE and JVM path are configured to **Java 21 or later** (matching `pom.xml`).
- **Maven Wrapper**: Always use `./mvnw` (Linux/macOS) or `.\mvnw.cmd` (Windows) instead of a globally installed `mvn` command. This ensures Maven version consistency.
- **Node & NPM**: Keep dependencies clean. When checking out changes in `eis-frontend`, run:
  ```bash
  npm install
  ```

---

## 2. Git Workflow & Branching Strategy

A clean Git history prevents merge conflicts and code loss:

- **Branch Naming Conventions**:
  - `feature/feature-name` for new features (e.g., `feature/ticket-system`)
  - `bugfix/issue-description` for bug fixes (e.g., `bugfix/employee-email-update`)
  - `refactor/component-name` for code cleanups
- **Pull Requests (PRs)**:
  - Keep PRs small and focused (ideally under 400 lines of code changed).
  - Ensure the backend compiles successfully (`mvn clean compile`) and tests pass before raising a PR.
- **Commit Messages**: Use clear, imperative style commit messages:
  - *Good*: `feat: add ticket resolution status update`
  - *Good*: `fix: save email when updating employee details`
  - *Bad*: `fixed stuff`, `more changes`

---

## 3. Backend (Spring Boot) Best Practices

- **Strict Controller-Service-Repository Separation**:
  - **Controllers**: Keep controllers thin. They should only handle HTTP routing, request validation (`@Valid`), and mapping response status codes.
  - **Services**: All business logic goes in `@Service` classes.
  - **Repositories**: Keep repository interfaces clean; use custom queries (`@Query`) only when Spring Data JPA method names become unreadable.
- **Data Transfer Objects (DTOs)**:
  - Never expose database `@Entity` classes directly in API endpoints.
  - Always map entities to DTOs (e.g., using builder patterns or MapStruct) before returning data to the frontend.
- **Caching (`@Cacheable` / `@CacheEvict`)**:
  - Cache heavy reads (`getAllEmployees`, `getDepartmentAnalytics`).
  - Remember to evict the cache using `@CacheEvict` on any write operations (`create`, `update`, `delete`) to prevent stale data.
- **Transaction Management**:
  - Mark service methods that perform multiple database updates or writes with `@Transactional` to ensure atomicity.

---

## 4. Frontend (React & Tailwind) Best Practices

- **Component Reuse**:
  - Extract repeating UI components (modals, buttons, input fields) into the `src/components` directory.
- **State Management**:
  - Utilize hooks cleanly. When updating nested objects in states, always use the functional updater pattern to avoid state collisions:
    ```javascript
    setFormData(prev => ({ ...prev, fieldName: newValue }));
    ```
- **Axios Interceptors & Error Handling**:
  - All API calls must go through the configured Axios client in [api.js](file:///c:/Users/ACER%20PREDATOR/Desktop/ENSET%20MASTER/Pfe/EIS/eis-frontend/src/services/api.js) to automatically attach JWT tokens and handle `401 Unauthorized` responses.
  - Always wrap API calls with `try-catch` blocks and use `toast.error()` to notify users when a request fails.
- **Responsive Layouts**:
  - Design mobile-first using Tailwind CSS responsive prefixes (`sm:`, `md:`, `lg:`).

---

## 5. Security & Sensitive Configurations

- **No Secrets in Source Control**:
  - Never commit API keys (such as the NVIDIA Cloud LLM key), database passwords, or JWT secrets to Git.
  - Use `.env` files in the frontend and `application.properties` placeholder references (e.g., `${NVIDIA_API_KEY}`) on the backend, reading values from system environment variables.
- **Gitignore Maintenance**:
  - Ensure `target/`, `node_modules/`, `.env`, and IDE config directories (`.idea/`, `.vscode/`) remain listed in `.gitignore`.
