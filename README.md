# Order Management System API

A Java EE RESTful API for managing orders, stock movements, items, and users.

## Technologies

- Java 8
- Spring Boot
- JPA/Hibernate
- PostgreSQL
- Log4j2
- Maven

## Prerequisites

- Java 8 JDK
- Maven
- PostgreSQL

## Setup

1. Clone the repository
git clone https://github.com/yourusername/order-manager-api.git
cd order-manager-api

2. Create a PostgreSQL database
createdb ordermanager

3. Configure the database connection in `src/main/resources/application.properties`
spring.datasource.url=jdbc:postgresql://localhost:5432/ordermanager
spring.datasource.username=your_username
spring.datasource.password=your_password

4. Configure email settings in `src/main/resources/application.properties`
spring.mail.host=your_smtp_server
spring.mail.port=your_smtp_port
spring.mail.username=your_email
spring.mail.password=your_email_password

5.  Build the project
mvn clean install

6. Run the application
mvn spring-boot:run

## API Endpoints

### Items

- `GET /api/items` - Get all items
- `GET /api/items/{id}` - Get item by ID
- `POST /api/items` - Create a new item
- `PUT /api/items/{id}` - Update an item
- `DELETE /api/items/{id}` - Delete an item

### Users

- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get order by ID
- `POST /api/users` - Create a new order
- `PUT /api/users/{id}` - Update a user
- `DELETE /api/users/{id}` - Delete a user

### Orders

- `GET /api/orders` - Get all orders
- `GET /api/orders/{id}` - Get user by ID
- `POST /api/orders` - Create a new user
- `PUT /api/orders/{id}` - Update an order
- `DELETE /api/orders/{id}` - Delete an order

### Stock Movements

- `GET /api/stock-movements` - Get all stock movements
- `GET /api/stock-movements/{id}` - Get stock movement by ID
- `POST /api/stock-movements` - Create a new stock movement
- `PUT /api/stock-movements/{id}` - Update a stock movement
- `DELETE /api/stock-movements/{id}` - Delete a stock movement
