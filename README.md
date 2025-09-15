# 2025- QUESOS BARTOLOME
Quesos Bartolomé is a website that manages the sales of cheeses from a small-town dairy in Aldeonte, a village in Segovia.
The platform provides customers with a more convenient way to place their orders, while also allowing them to read reviews from other buyers and share their own feedback on the products they purchase.
In addition, the application gives administrators tools to track cheese sales through graphs and automates the process of confirming orders and generating invoices for customers.
## Objectives
### Functional Objectives
The main functional objectives of the application are: for registered users, placing orders and leaving reviews for the products they purchase; and for administrators, managing orders, controlling products, generating invoices, and analyzing sales through different charts.

- **Placing orders:** Registered users can add different products to their order and then send it to the dairy.

- **Product reviews:** Registered users can leave reviews for the products they have purchased.

- **Profile management:** Registered users can update their profile whenever they wish.

- **Stock management:** Administrators can add new cheeses to the catalog as well as update stock levels for each cheese.

- **Order management:** Administrators can view and process orders placed with the dairy.

- **Sales charts:** Administrators can access charts that display different sales metrics by month, both by customer and by cheese type.

- **User management:** Administrators can ban users who have engaged in inappropriate behavior.

### Technical Objectives
The more technical aspects of the application focus on thorough code control through automated unit and integration tests, covering business logic, database services, and verifying the behavior of components and services of a REST API. In addition, static code analysis will be performed to report violations and warnings, with continuous monitoring throughout the development of the application.
We will also integrate complementary technologies to enhance the user experience in our application and services, such as invoice generation, email sending, and the use of maps to locate elements.

- **Automated unit and integration tests:** Tests that run automatically to ensure good code quality by checking the business logic and the different services and components of the application.

- **Static code analysis:** Static code analysis with SonarQube to continuously improve the quality, security, and maintainability of the code throughout the project.

- **Email sending:** Sending emails to customers with important messages or invoices issued by the company.

- **PDF generation:** Generating invoices from customer orders.

- **Use of maps:** Using Google Maps to display the location of the dairy in the "About Us" section.

## Methodology
The project will be carried out in phases, each with a defined start and end date. Every phase includes a set of tasks aimed at completing the final degree project in an organized and effective way.

### Phase 1: Definition of functionalities and screens.
- Start date:
- End date:
- In this phase, the general aspects of the application will be defined, along with its functionalities, which will be divided into basic, intermediate, and advanced. The roles of the different users will also be established. Additionally, the designs of the application’s screens will be created.
### Phase 2: Repository, testing, and CI.
- Start date:
- End date:
- The Git repository will be created, a minimum set of automated tests will be implemented, and the CI system will be configured.
### Phase 3: Version 0.1 - Basic functionality and Docker.
- Start date:
- End date:
- Development of the application will begin by implementing the basic functionality and its automated tests. The application will also be packaged in Docker. At the end of this phase, the first release (version 0.1) of the application will be delivered.
### Phase 4: Version 0.2 - Intermediate functionality.
- Start date:
- End date:
- Development will continue with the implementation of intermediate functionalities, resulting in version 0.2 of the application.
### Phase 5: Version 1.0 - Advanced functionality.
- Start date:
- End date:
- The application development will be completed with the implementation of advanced functionalities, resulting in the final release (version 1.0) of the application.
### Phase 6: Report.
- Start date:
- End date:
- The final degree project report will be written.
### Phase 7: Defense.
- Start date:
- End date:
- The defense of the final degree project will take place.
### Gantt Chart


## Detailed Features
Below, we present all the functionalities of the application, divided into basic, intermediate, and advanced, specifying which type of user they are intended for.

### Basic Functionality.
| Users | Functionalities |
|----------|-----------------|
| Unregistered users | -View cheeses, their characteristics, and reviews <br> -See the "Acerca de nosotros" section <br> -View profiles of review authors <br> -Register <br> -Log in |
| Registered users | -Place orders <br> -Access their profile |
| Administrators | -Add and remove cheeses <br> -View orders <br> -View customers <br> -Manage stock |

### Intermediate Functionality.
| Users | Functionalities |
|----------|-----------------|
| Registered users | -Edit their profile <br> -Change their credentials <br> -View, edit, and delete their reviews |
| Administrators | -Ban users <br> -View charts |
### Advanced Functionality.
| Users | Functionalities |
|----------|-----------------|
| Administrators | -Process orders <br> -Generate invoices <br> -Send emails to users <br> -Filter charts by cheeses and customers |

## Analysis
### Screens and Navigation.
Below, we present the screens of our application in the form of wireframes, along with a brief description.

#### Main Screen

This screen displays the cheeses available in the dairy. Depending on the type of user accessing it, different options will appear in the menu.

#### Register

Form for entering the data required to register a new customer.

#### Log In

Form to log in.

#### About Us

Section with some information about the dairy, including a map showing its location and a QR code linking to a page of interest.

#### Product

Page displaying detailed information about a cheese, its price, and—if you are a registered user—an option to add it to your order.

#### My Order

Section showing all the cheeses you have added to your order, from where you can send it to the company for processing.

#### Review

Small screen with a brief form for reviewing a cheese.

#### View Order

Section where administrators can see all pending orders in the queue and decide whether to process them.

#### Process Order

Page showing a summary of the order and allowing it to be processed.

#### View Customers

Section displaying a table with all of the dairy’s customers, allowing administrators to access their profiles and ban them if necessary.

#### Invoices

Section where invoices issued by the dairy can be viewed.

#### New Cheese

Form that allows the administrator to add a new cheese to the catalog.

#### Stock

Section where the administrator manages the stock of existing cheeses in the catalog.

#### Charts

Page where administrators can view and analyze charts of cheese sales.

#### Error

Example of an application error and how the system notifies the user, regardless of the type of error.

#### Navigation

### Entidades
| Entidad | Atributos | Relaciones |
|---------|-----------|------------|
| Usuario | -Id <br> -Nombre <br> -Contraseña <br> -Gmail <br> -Dirección <br> -NIF <br> -Imagen | |
| Queso | -Id <br> -Nombre <br> -Precio <br> -Descripción <br> -Fecha de fabricación <br> -Fecha de caducidad <br> -Tipo <br> -Imagen | |
| Pedidos | -Id <br> -Cliente <br> -Lista de quesos | |
| Facturas | -Id <br> -Nº de factura <br> -Cliente <br> -Lista de quesos <br> -Kg <br> - Precio total | |
| Valoración | -Id <br> -Usuario <br> -Queso <br> -Puntuación <br> -Comentario | |

### Permisos de los usuarios

Los usuarios registrados son dueños de sus propias reseñas que hayan puesto a cada queso, ellos pueden crearlas, verlas, modificarlas y borrarlas cuando ellos lo deseen.
Los administradores por otra parte pueden hacer los mismo con los quesos del catálogo.

### Imágenes

Las entidades de Queso y Usuario tendrán una imagen asociada.

### Gráficos

Los grafico serán gráficos de barras, únicamente serán visibles por los administradores en el eje x se mostraran los meses del año y en el eje y los € vendidos cada mes o los Kg vendidos cada mes, además estos se podrán filtrar por cliente para saber lo que compra cada cliente por mes y también por queso para saber lo que se vende cada queso, para posteriormente poder analizar las ventas de la quesería.

### Tecnología complementaria

- **Envió de correos:** Envió de correos a los clientes con mensajes importantes o con las facturas emitidas por la empresa.
- **Generación de PDFs:** Generación de facturas a partir de los pedidos de los clientes.
- **Uso de mapas:** Uso de un mapa de Google Maps para ubicar la quesería en la sección "acerca de nosotros".

### Algoritmo o consulta avanzada

En la página principal se mostrarán primero los quesos cuya valoración media sea más alta obteniendo esta de las valoraciones de los clientes.

## Seguimiento

- Blog
- Github Project

## Autor

El desarrollo de esta aplicación se hace en el contexto de trabajo de fin de grado del grado en Ingeniería de software en la ETSII de la Universidad Rey Juan Carlos de Madrid.
Trabajo realizado por el estudiante de 4º de carrera Víctor Bartolomé Letosa y tutorizado por Michel Maes Bermejo.
