# RevShop – E-Commerce Application

## Project Overview

RevShop is a full-stack monolithic e-commerce application developed using Java and Spring Boot.
The platform connects Buyers and Sellers, where Sellers can list products and manage orders,
and Buyers can browse, add to cart, purchase products, and leave reviews.

This project follows a layered MVC architecture using Spring Boot, Spring Security, and Thymeleaf.

---

## Technologies Used

### Backend
- Java
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- Hibernate

### Frontend
- Thymeleaf
- HTML
- CSS
- JavaScript

### Database
- Oracle Database (XE)

### Tools & Libraries
- Maven
- Log4J2
- JUnit 5
- Mockito
- BCrypt Password Encoder

---

## System Architecture

```
Client (Browser)
      ↓
Thymeleaf UI (HTML Templates)
      ↓
Spring MVC Controllers
      ↓
Service Interfaces
      ↓
Service Implementation Layer (Business Logic)
      ↓
Repository Layer (Spring Data JPA)
      ↓
Oracle Database
```

---

## Modules

### Authentication Module
- User Registration (BUYER / SELLER)
- User Login with BCrypt password verification
- Role-based redirect after login
- Security Question-based Password Recovery
- Change Password

### Product Module
- Add / Edit / Delete Products (Seller)
- View All Products (Public)
- Search Products by Keyword
- Product Image Upload
- Category Management

### Cart Module
- Add to Cart
- Update Item Quantity
- Remove Item from Cart
- View Cart with Total Price

### Order Module
- Place Order
- View Order History (Buyer)
- Accept / Reject / Deliver Orders (Seller)
- Cancel Order
- Stock Validation on Order Placement

### Review Module
- Post Product Reviews (Buyer)
- View Reviews on Product Detail Page
- Star Rating System

### Favorites Module
- Save Products to Wishlist
- Remove from Favorites
- View Favorites Page

### Notification Module
- Automated Notifications on Order Status Changes
- Mark Notifications as Read

### Seller Dashboard
- View and Manage Own Listings
- Manage Incoming Orders

---

## Database Entities

- User
- Product
- Category
- Cart
- CartItem
- Order
- OrderItem
- Payment
- Review
- ProductImage
- Favorite
- Notification
- SecurityQuestion

---

## How to Run the Project

### Prerequisites
- Java 17+
- Oracle Database XE installed and running
- Maven installed

### Steps

1. Clone the repository
```
git clone <repository-url>
```

2. Import the project into IntelliJ IDEA or Eclipse

3. Set up the Oracle Database
```
Run RevshopP2.sql to create the schema and tables
```

4. Configure database credentials in `application.properties`
```
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:xe
spring.datasource.username=your_username
spring.datasource.password=your_password
```

5. Run the Spring Boot application
```
mvn spring-boot:run
```

6. Access the application at:
```
http://localhost:8888
```

---

## Project Structure

```
src/
├── main/
│   ├── java/com/revshopproject/revshop/
│   │   ├── Application.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   └── WebConfig.java
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   ├── security/
│   │   ├── service/
│   │   │   └── impl/
│   │   └── utils/
│   └── resources/
│       ├── application.properties
│       ├── log4j2-spring.xml
│       ├── static/
│       └── templates/
└── test/
    └── java/com/revshopproject/revshop/
        └── service/impl/
```

---

## User Roles

### BUYER
- Browse and search products
- Add products to cart
- Place and track orders
- Leave reviews on products
- Save products to favorites
- Receive order notifications

### SELLER
- List, edit, and delete products
- Upload product images
- Manage product categories
- View and manage incoming orders (Accept / Reject / Deliver)
- Access seller dashboard

---

## Security

- Passwords are hashed using BCrypt before storing in the database
- Role-based access control enforced via Spring Security
- CSRF protection enabled for all secure endpoints
- Sensitive fields annotated with @JsonIgnore to prevent exposure
- DTOs used throughout to prevent raw entity exposure

---

## API Endpoints Summary

| Method | Endpoint | Access |
|--------|----------|--------|
| POST | /api/users/register | Public |
| POST | /api/users/forgot-password | Public |
| GET | /api/products | Public |
| GET | /api/products/search?q= | Public |
| GET | /api/categories | Public |
| POST | /api/products | SELLER |
| PUT | /api/products/{id} | SELLER |
| DELETE | /api/products/{id} | SELLER |
| GET | /api/cart | BUYER |
| POST | /api/cart/add | BUYER |
| POST | /api/orders/place | BUYER |
| GET | /api/orders/my-orders | BUYER |
| PATCH | /api/orders/accept/{id} | SELLER |
| PATCH | /api/orders/reject/{id} | SELLER |
| PATCH | /api/orders/deliver/{id} | SELLER |
| POST | /api/reviews | BUYER |
| GET | /api/notifications | Authenticated |

---

## Team

RevShop — Training Project
Developed by a team of 5 Full Stack Developers

---

## Future Improvements

- Microservices architecture
- Docker containerization
- Cloud deployment (AWS / Azure)
- Payment gateway integration
- Email notification system
- Product recommendation engine
