# 🍽️ Mess Management System

A web-based application built with **Spring Boot** to manage mess operations, including user authentication, menu management, billing, reporting, and managing student account status based on bills.

---

## 📑 Table of Contents

- [Features](#features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [Cloning the Repository](#cloning-the-repository)
  - [Database Setup](#database-setup)
  - [Running the Application](#running-the-application)
- [Usage](#usage)
- [Future Enhancements](#future-enhancements)

---

## ✅ Features

- **User Authentication & Authorization**: Separate roles for Admin and Student users using Spring Security.
- **Student Registration**: Allows new students to register accounts.
- **Admin Dashboard**: Centralized view for administrators.
- **Menu Management**: Admin interface to add, edit, and delete daily/weekly menu items.
- **Billing Period Management**: Admin functionality to generate bills based on mess consumption records.
- **View Generated Bills**: Admin view to see all generated bills.
- **Financial Summary Report**: Shows total billed, total paid, and total due amounts.
- **Account Locking**: Automatically locks student accounts with outstanding bills.
- **Student Profile**: Students can view and update contact information.
- **Mess Record Tracking**: Implicitly handled through billing.

---

## 🛠️ Technology Stack

### Backend
- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate

### Database
- MySQL

### Frontend
- Thymeleaf
- Bootstrap

### Build Tool
- Maven

---

## 📦 Prerequisites

Before running the application, ensure you have the following installed:

- Java Development Kit (JDK) 17 or higher
- Apache Maven
- MySQL Server
- Git

---

## 🚀 Getting Started

### 🔁 Cloning the Repository

```bash
git clone <repository_url>
cd mess-management-system
Replace <repository_url> with your actual GitHub repository URL.

## 🛢️ Database Setup
Ensure your MySQL server is running and create the database:

sql
Copy
Edit
CREATE DATABASE mess_db;
Then update the database configuration in src/main/resources/application.properties:

### properties
Copy
Edit
spring.datasource.url=jdbc:mysql://localhost:3306/mess_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
Replace your_mysql_username and your_mysql_password with your MySQL credentials.

## ▶️ Running the Application
In the project root directory, run:

bash
Copy
Edit
./mvnw spring-boot:run
This will start the application and open it at:

http://localhost:8080

## 💻 Usage
### 🔐 Admin Login
Username: admin_001

Password: adminpass (or your own password corresponding to the hashed one in the DB)

### 👨‍🎓 Student Login
Use your registration number as the username and your chosen password.

### 📝 Student Registration
Visit: http://localhost:8080/register

## 🔮 Future Enhancements
Admin workflow for approving new student registrations.

Automated scheduled bill generation and account status updates.

Email or SMS notifications for bills and payments.

Online payment gateway integration.

More detailed reports (e.g., daily attendance, individual billing history).

Student interface for bills, payment history, and consumption tracking.

Dedicated admin pages for managing students.

