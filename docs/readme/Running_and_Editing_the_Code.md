### Running and Editing the Code
Below are the steps to run the application.

#### Cloning the Repository

```bash
git clone https://github.com/codeurjc-students/quesos-bartolome.git
cd quesos-bartolome
```
#### Running the Application

- **Configure the Database (SQL)**

Set the environment variables:
```bash
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
```

- **Server (Backend)**

```bash
cd backend
mvn clean install
mvn spring-boot:run
```
The server will start at http://localhost:8080

- **Client (Frontend)**

```bash
cd frontend
npm install
ng serve
```
The application will run at http://localhost:4200 

#### Using Tools

- Visual Studio Code: Main environment for viewing and editing source code for both the server and client.

- Postman: Tool to interact with the server’s REST API. Postman Collection in `docs/Postman/QuesosBartolome.postman_collection.json`


#### Running Tests

-Cliente: 
```bash
cd frontend
ng test
```
-Servidor: 
```bash
cd backend
mvn clean test
```

#### Creating a Release

To generate a deployable version:

- Backend (Spring Boot)
```bash
cd backend
mvn clean package
```
This generates an executable `.jar` file in `target/quesosbartolome-0.0.1-SNAPSHOT.jar`

```bash
java -jar target/quesosbartolome-0.0.1-SNAPSHOT.jar
```

- Frontend (Angular)
```bash
cd frontend
ng build --configuration production
```
The output is generated in `frontend/dist/`, ready to be deployed to a web server or integrated with the backend.