### Technical Summary

| Category | Description |
|---------|-----------|
| Type | - Web SPA + REST API (decoupled client-server architecture). |
| Frontend | - Angular, developed in TypeScript and compiled to JavaScript. |
| Backend | -Spring Boot (Java 21). Manages business logic, authentication, and the REST API. Maven is used for dependency management and project build. |
| Database | - MySQL database where system entities are stored (Cheeses, Orders, Users, etc.). |
| Development Tools | - Visual Studio Code for frontend and backend. npm is used for frontend package management and Maven for the backend. Docker is used for containerized deployment, and Postman for REST API testing. |
| Quality Control | - Unit, integration, and system tests. JUnit, RestAssured, Karma/Jasmine, Selenium. SonarQube for static code analysis and GitHub Actions for automated CI/CD. |
| Deployment | - Docker |
| Process | -Iterative and incremental development with version control using a simplified Git Flow (feature/ and main branches). Continuous integration configured in GitHub Actions. |