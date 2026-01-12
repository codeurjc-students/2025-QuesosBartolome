### Analysis

#### Screens and Navigation.
Below, we present the screens of our application in the form of wireframes, along with a brief description.

##### Main Screen

This screen displays the cheeses available in the dairy. Depending on the type of user accessing it, different options will appear in the menu.
![Main Screen](/Images/MainScreen.png)

##### Register

Form for entering the data required to register a new customer.

![Register](/Images/Register.png)

##### Log In

Form to log in.

![Log In](/Images/login.png)

##### About Us

Section with some information about the dairy, including a map showing its location and a QR code linking to a page of interest.

![About Us](/Images/AcercaDeNosotros.png)

##### Product

Page displaying detailed information about a cheese, its price, and—if you are a registered user—an option to add it to your order.

![Product](/Images/Chesee.png)

##### My Order

Section showing all the cheeses you have added to your order, from where you can send it to the company for processing.

![My Order](/Images/MyOrder.png)

##### Review

Small screen with a brief form for reviewing a cheese.

![Review](/Images/Review.png)

##### View Orders

Section where administrators can see all pending orders in the queue and decide whether to process them.

![View Order](/Images/ViewOrders.png)

##### Process Order

Page showing a summary of the order and allowing it to be processed.

![Process Order](/Images/ProcessOrder.png)

##### View Customers

Section displaying a table with all of the dairy’s customers, allowing administrators to access their profiles and ban them if necessary.

![View Customers](/Images/ViewCustomers.png)

##### Invoices

Section where invoices issued by the dairy can be viewed.

![Invoices](/Images/Invoices.png)

##### New Cheese

Form that allows the administrator to add a new cheese to the catalog.

![New Cheese](/Images/NewCheese.png)

##### Stock

Section where the administrator manages the stock of existing cheeses in the catalog.

![Stock](/Images/Stock.png)

##### Charts

Page where administrators can view and analyze charts of cheese sales.

![Charts](/Images/Charts.png)

##### Error

Example of an application error and how the system notifies the user, regardless of the type of error.

![Error](/Images/Error.png)

##### Navigation

![Navigation](/Images/Navigation.png)

#### Entities  
| Entity | Attributes | Relationships |
|---------|-----------|------------|
| User | -Id <br> -Name <br> -Password <br> -Email <br> -Address <br> -Type <br> -Tax ID <br> -Image | -Users are related to reviews <br> -Users are related to orders <br> -Users are related to invoices |
| Cheese | -Id <br> -Name <br> -Price <br> -Description <br> -Manufacturing date <br> -Expiration date <br> -Type <br> -Image <br> -Review list | -Cheeses are related to orders <br> -Cheeses are related to reviews |
| Orders | -Id <br> -Customer <br> -Cheese list | -Orders are related to users <br> -Orders are related to cheeses <br> -Orders are related to invoices |
| Invoices | -Id <br> -Invoice No. <br> -Customer <br> -Cheese list <br> -Kg <br> -Total price | -Invoices are related to users <br> -Invoices are related to orders |
| Review | -Id <br> -User <br> -Cheese <br> -Rating <br> -Comment | -Reviews are related to users <br> -Reviews are related to cheeses |


![Entities](/Images/Entities.png)

#### User Permissions  

Registered users are the owners of their own reviews for each cheese; they can create, view, edit, and delete them whenever they wish.  
Users are also the owners of their active order until it is confirmed; they can edit and view it whenever they want.  
Administrators, on the other hand, can create, view, edit, and delete cheeses in the catalog.  
Administrators can convert orders into invoices and view them.  
Administrators can also ban users.  

#### Images  

The Cheese and User entities will have an associated image.  

#### Charts  

The charts will be bar charts, visible only to administrators. On the x-axis, the months of the year will be displayed, and on the y-axis, either the € sold each month or the Kg sold each month.  
Additionally, they can be filtered by customer (to see what each customer buys per month) and by cheese (to see how much of each cheese is sold), in order to analyze the dairy’s sales.  

#### Complementary Technology  

- **Email sending:** Sending emails to customers with important messages or invoices issued by the company.  
- **PDF generation:** Generating invoices from customer orders.  
- **Use of maps:** Using Google Maps to display the dairy’s location in the "About Us" section.  

#### Advanced Algorithm or Query  

On the main page, the cheeses with the highest average rating will be displayed first, with this rating calculated from customer reviews.