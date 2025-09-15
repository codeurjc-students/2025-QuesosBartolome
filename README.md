# 2025- QUESOS BARTOLOME
Quesos Bartolomé is a website that manages the sales of cheeses from a small-town dairy in Aldeonte, a village in Segovia.
The platform provides customers with a more convenient way to place their orders, while also allowing them to read reviews from other buyers and share their own feedback on the products they purchase.
In addition, the application gives administrators tools to track cheese sales through graphs and automates the process of confirming orders and generating invoices for customers.
## Objectives
### Functional Objectives
Los principales objetivos funcionales de la aplicación son la realización de pedidos por parte de los usuarios registrados y las valoraciones a los productos pedidos por estos, y por parte de los administradores la tramitación de pedidos, control de los productos, generación de facturas y el análisis de las ventas mediante diferentes gráficos.

- **Realización de pedidos:** Los usuarios registrados podrán añadir diferentes productos a su pedido y posteriormente mandar el pedido a la quesería.
- **Valoración de productos:** Los usuarios registrados podrán añadir reseñas de los productos que han pedido.
- **Control del perfil:** Los usuarios registrados podrán modificar su perfil siempre que lo deseen.
- **Control del Stock:** Los administradores podrán añadir nuevos quesos al catalogo así como añadir stock de cada uno de los quesos.
- **Control de los pedidos:** Los administradores podrán ver los pedidos realizados a la quesería y tramitarlos si así lo desean.
- **Visualización de graficas:** Los administradores podrán ver graficas que muestren diferentes parámetros de la venta de los quesos por meses tanto por los clientes como por cada tipo de queso.
- **Control de usuarios:** Los administradores podrán banear los usuarios que deseen sin han tenido algún tipo de conducta inapropiada.

### Objetivos Técnicos
Los aspectos más técnicos de la aplicación pasan por un control exhausto del código mediante pruebas automáticas unitarias y de integración testeando la lógica de negocio, los servicios con la base de datos y verificando el comportamiento de los componentes y servicios de una API REST. Además, también se hará un análisis estático del código que ira reportando las violaciones y warnings encontrados realizando un seguimiento durante todo el desarrollo de la aplicación.
También añadiremos tecnologías complementarias para mejorar la experiencia de los usuarios en nuestra aplicación y usando nuestros servicios como puede ser la generación de facturas, él envió de correos y el uso de mapas para posicionar elementos.

- **Pruebas automáticas unitarias y de integración:** Pruebas que se ejecutaran automáticamente para asegurar una buena calidad del código comprobando la lógica de negocio y los diferentes servicios y componentes de la aplicación.
- **Análisis estático de código:** Análisis estático de código con SonarQube con el fin de mejorar la calidad, seguridad y mantenibilidad del código de forma continua durante todo el proyecto.
- **Envió de correos:** Envió de correos a los clientes con mensajes importantes o con las facturas emitidas por la empresa.
- **Generación de PDFs:** Generación de facturas a partir de los pedidos de los clientes.
- **Uso de mapas:** Uso de un mapa de Google Maps para ubicar la quesería en la sección "acerca de nosotros".

## Metodología
El proyecto se ira realizando por fases cada una con una fecha de inicio y final, cada fase contiene una serie de tareas que tienen como objetivo la realización del trabajo de fin de grado de una forma organizada y efectiva.

### Fase 1: Definición de funcionalidades y pantallas.
Fecha inicio:
Fecha fin:
En esta fase se definirán los aspectos generales de la aplicación, se definirán sus funcionalidades dividiéndolas en básicas, intermedias y avanzadas, además de definir los roles de los diferentes usuarios. Por otra parte, se realizaran los diseños de las pantallas de la futura aplicación.
### Fase 2: Repositorio, pruebas y CI.
Fecha inicio:
Fecha fin:
Se creará el repositorio de git, se implementaran unos mínimos tests automáticos y se configurara el sistema de CI.
### Fase 3: Versión 0.1 - Funcionalidad básica y Docker.
Fecha inicio:
Fecha fin:
Se iniciará el desarrollo de la aplicación implementando la funcionalidad básica y sus pruebas automáticas y además se empaquetara la aplicación en Docker, al finalizar esta fase saldrá el primer reléase la versión 0.1 de la aplicación.
### Fase 4: Versión 0.2 - Funcionalidad intermedia.
Fecha inicio:
Fecha fin:
Se continuará el desarrollo implementando las funcionalidades intermedias y se obtendrá la versión 0.2 de la aplicación.
### Fase 5: Versión 1.0 - Funcionalidad avanzada.
Fecha inicio:
Fecha fin:
Se finalizará el desarrollo de la aplicación con la implementación de las funcionalidades avanzadas obteniendo el ultimo reléase la versión 1.0 de la aplicación.
### Fase 6: Memoria.
Fecha inicio:
Fecha fin:
Se elaborará la memoria del trabajo de fin de grado.
### Fase 7: Defensa.
Fecha inicio:
Fecha fin:
Se realiza el acto de defensa del TFG.
### Diagrama de Gant


## Funcionalidades detalladas
A continuación, presentaremos todas las funcionalidades de la aplicación divididas entre básicas, intermedias y avanzadas y especificando a que tipo de usuario va dirigida.

### Funcionalidad básica.
| Usuarios | Funcionalidades |
|----------|-----------------|
| Usuarios no registrados| -Visualizar los quesos, sus características y sus reseñas <br> -Ver sección "Acerca de Nosotros" <br> -Ver los perfiles de los usuarios de las reseñas <br> -Registrarse <br> -Iniciar Sesión |
| Usuarios registrados | -Hacer pedidos <br> -Acceder a su perfil |
| Administradores | -Añadir y eliminar quesos <br> -Ver pedidos <br> -Ver clientes <br> -Controlar Stock |

### Funcionalidad intermedia.
| Usuarios | Funcionalidades |
|----------|-----------------|
| Usuarios registrados | -Modificar su perfil <br> -Modificar sus credenciales <br> -Poder ver, modificar y eliminar sus reseñas |
| Administradores | -Banear Usuarios <br> -Ver gráficas |
### Funcionalidad Avanzada.
| Usuarios | Funcionalidades |
|----------|-----------------|
| Administradores | -Tramitar Pedidos <br> -Generar facturas <br> -Enviar correos a los usuarios <br> -Filtrar gráficos por quesos y clientes |

## Análisis
### Pantallas y navegación.
A continuación, mostraremos las pantallas de nuestra aplicación en forma de wireframes de estas con una breve descripción.
#### Pantalla principal
Esta pantalla muestra los quesos disponibles en la quesería y en función del usuario que acceda a esta tendrá diferentes opciones en el menú.

#### Registrarse
Formulario para rellenar los datos para realizar el registro de un nuevo cliente.

#### Iniciar sesión
Formulario para iniciar sesión.

#### Acerca de nosotros
Sección con un poco de información sobre la quesería y con un mapa que muestra su ubicación y un QR a algina página de interés.

#### Producto
Página que muestra la información detallada del queso su precio y desde donde se puede añadir a el pedido si eres un usuario registrado.

#### Mi pedido
Sección que muestra todos los quesos que has añadido al pedido y desde donde se lo puedes mandar a la empresa para que lo tramiten.

#### Valoración
Pequeña pantalla con un breve formulario para valorar el queso.

#### Ver pedido 
Apartado donde los administradores pueden ver todos los pedidos de la cola y pueden verlos y tramitarlos.

#### Tramitar Pedido
Pagina que muestra un resumen del pedido y que permite tramitar el pedido.

#### Ver clientes
Apartado que muestra una tabla con todos los clientes de la quesería pudiendo acceder a sus perfiles y banearlos si así lo desea el administrador.

#### Facturas
Sección donde se pueden ver las facturas emitidas por la quesería.

#### Nuevo Queso
Formulario para que el administrador pueda añadir un nuevo queso al catálogo.

#### Stock
Sección desde la cual el administrador controla el stock de los quesos existentes en el catálogo.

#### Graficas
Pagina desde la cual los administradores pueden observar las gráficas de las ventas de sus quesos y analizarlas.

#### Error
Ejemplo de un error en la aplicación y de cómo el sistema avisa al usuario independientemente del tipo que sea.

#### Navegación

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
