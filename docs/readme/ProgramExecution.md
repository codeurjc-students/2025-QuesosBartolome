## Program execution

### Prerequisites
Before running the application, you must have Docker installed on your system:
- **Windows:** Install [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- **Linux:** Install [Docker Engine](https://docs.docker.com/engine/install/) and [Docker Compose](https://docs.docker.com/compose/install/)

---

### Running the application
Execute the following commands:

```bash
docker pull victorbartolome/quesosbartolome:dev
docker pull victorbartolome/quesosbartolome-compose:dev

docker create --name temp-compose victorbartolome/quesosbartolome-compose:dev cmd.exe
docker cp temp-compose:/docker-compose-dev.yml ./docker-compose.yml
docker rm temp-compose
```

To run the application, you must create a `.env` file with the environment variables shown below.
This file must be located in the same directory as the generated `docker-compose.yml`.

```bash
MYSQL_ROOT_PASSWORD=
MYSQL_DATABASE=
MYSQL_USER=
MYSQL_PASSWORD=

DB_HOST=db
DB_PORT=3306
DB_NAME=
DB_USER=
DB_PASSWORD=

SERVER_PORT=443
SERVER_SSL_KEY_STORE_PASSWORD=password
```
Now start the application with:

```bash
docker compose up -d
```


Once the application is running, access it at:
https://localhost:443

### Access and Application Information
The website includes two default users: one USER and one ADMIN.

| Type  | Username | Password     |
|-------|----------|--------------|
| ADMIN | German   | password123  |
| USER  | Victor   | password123  |

These names are used because they correspond to the owners of the cheese factory.  
In future versions, more generic names may be used.

#### Sample Data
The website includes a set of default data to demonstrate its functionality:

**Users**

- **User:** Victor, with all the fields of a registered user and an empty cart.  
- **Admin:** German, with all the fields of an administrator.

**Cheeses**

- The website includes **5 different types of cheeses**, each with its own attributes.  
  All of them have boxes available for ordering except **Chevrett**, the fourth cheese.  
- No reviews or orders have been added initially in this version, as they were not considered necessary to demonstrate the website’s functionality.
