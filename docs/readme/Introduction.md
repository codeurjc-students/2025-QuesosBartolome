### Introduction

The Quesos Bartolomé application has been developed following a Single Page Application (SPA) web architecture. This structure divides the system into three main parts: the client (frontend), the server (backend), and the database used to store the application’s information. This type of architecture improves web performance and enhances the user experience by allowing asynchronous interaction and content updates without the need to reload the entire page.

Additionally, the application is deployed using Docker, which ensures a consistent execution environment and makes both deployment and scalability much easier.

Detailed components:
| System Component | Description |
|---------|-----------|
| Frontend | - Developed with Angular, it is responsible for the graphical interface and user interaction. It makes requests and consumes the REST API provided by the backend. |
| Backend | - Implemented with Spring Boot, it handles business logic, access control, data validation, and communication with the database. |
| Database | - A MySQL database used to store the application’s data. |
