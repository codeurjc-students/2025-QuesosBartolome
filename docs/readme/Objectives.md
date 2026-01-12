### Objectives
#### Functional Objectives
The main functional objectives of the application are: for registered users, placing orders and leaving reviews for the products they purchase; and for administrators, managing orders, controlling products, generating invoices, and analyzing sales through different charts.

- **Placing orders:** Registered users can add different products to their order and then send it to the dairy.

- **Product reviews:** Registered users can leave reviews for the products they have purchased.

- **Profile management:** Registered users can update their profile whenever they wish.

- **Stock management:** Administrators can add new cheeses to the catalog as well as update stock levels for each cheese.

- **Order management:** Administrators can view and process orders placed with the dairy.

- **Sales charts:** Administrators can access charts that display different sales metrics by month, both by customer and by cheese type.

- **User management:** Administrators can ban users who have engaged in inappropriate behavior.

#### Technical Objectives
The more technical aspects of the application focus on thorough code control through automated unit and integration tests, covering business logic, database services, and verifying the behavior of components and services of a REST API. In addition, static code analysis will be performed to report violations and warnings, with continuous monitoring throughout the development of the application.
We will also integrate complementary technologies to enhance the user experience in our application and services, such as invoice generation, email sending, and the use of maps to locate elements.

- **Automated unit and integration tests:** Tests that run automatically to ensure good code quality by checking the business logic and the different services and components of the application.

- **Static code analysis:** Static code analysis with SonarQube to continuously improve the quality, security, and maintainability of the code throughout the project.

- **Email sending:** Sending emails to customers with important messages or invoices issued by the company.

- **PDF generation:** Generating invoices from customer orders.

- **Use of maps:** Using Google Maps to display the location of the dairy in the "About Us" section.