### Deployment
The application is packaged using Docker, ensuring a consistent deployment across different environments.
The application is built into a single Docker image that includes both the Angular client and the Spring Boot server.
Docker Compose is used to coordinate the required services.
The images are published through continuous integration in three possible ways:

- Manual Build Workflow: Allows a developer to create an image whenever they choose.
- Publish dev on main Workflow: Triggered when a branch is merged into main via a pull request.
- Publish on Release Workflow: Triggered when a release is published.

The final application artifact is published on DockerHub, from where it can be downloaded and executed directly:

- DockerHub repository URL: [DockerHub Repository](https://hub.docker.com/repositories/victorbartolome)