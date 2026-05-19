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
This website includes several users and one administrator user. Below are the access credentials for the administrator and two users of the site.

| Type  | Username | Password     |
|-------|----------|--------------|
| ADMIN | Admin   | password123  |
| USER  | Tienda Artesanal de Riaza   | password123  |
| USER  | Supermercado Aldeonte   | password123  |

#### Sample Data
The website includes a set of default data to demonstrate its functionality:

**Users**

- **User:** 10 clients from fictional stores inspired by the cheese production area.
- **Admin:** Admin, with all the fields of an administrator.

**Cheeses**

- The website includes **5 different types of cheeses**, each with its own attributes.  
  All of them have boxes available for ordering except **Chevrett**, the fourth cheese.
- The cheese images correspond to real products from the dairy.

**Reviews**

- Between 2 and 4 reviews per cheese, written by clients, each with its corresponding rating and comment, visible on the detail page of each cheese.

**Orders and invoices**

- A total of 149 randomly generated invoices, with orders created in a realistic context to provide a database from which to extract metrics.
- Additionally, each invoice has its corresponding processed order, and there are 10 unprocessed orders belonging to different clients.

