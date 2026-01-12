### Architecture

#### Deployment
The Quesos Bartolomé application has a deployment architecture with independent processes that communicate with each other.

- Frontend: Implemented in Angular, it runs as an SPA, contains the user interface, and communicates with the service through the REST API.
- Backend: Developed with Spring Boot, it functions as the application service, providing the REST API consumed by the client.

#### Domain Model
This diagram shows the persistent entities of the application.

![DomainModel](/Images/DomainModel.png)

#### API REST
The REST API is properly documented using OpenAPI.

Documentation: [DOC APIREST](https://raw.githack.com/codeurjc-students/2025-QuesosBartolome/refs/heads/main/docs/OpenApi/openapi.html)

#### Server Architecture
“Server class diagram showing its layer separation and the responsibility of each layer.

![ServerArchitecture](/Images/ServerArchitecture.png)

#### Client Architecture
Client class diagram showing its layer separation and the responsibility of each layer.

![ClientArchitecture](/Images/ClientArchitecture.png)
