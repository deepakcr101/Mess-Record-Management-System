# 🍽️ Mess Management System

A web-based application built with **Spring Boot** to streamline mess operations in educational institutions. It handles **user authentication**, **menu and billing management**, **mess attendance tracking**, and more, with role-based access for admins and students.

---

## 📚 Table of Contents

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

- 🔐 **User Authentication & Authorization**: Secure login with roles (Admin / Student) using Spring Security.
- 📝 **Student Registration**: Easy self-registration for new students.
- 📊 **Admin Dashboard**: Central hub for managing mess operations.
- 🍽️ **Menu Management**: CRUD interface for daily/weekly menu.
- 💰 **Billing Period Management**: Generate bills based on consumption records.
- 📄 **View Generated Bills**: Quick overview of student billing.
- 📈 **Financial Summary Report**: Track total billed, paid, and due amounts.
- 🔒 **Account Locking**: Automatically lock accounts with overdue bills.
- 👤 **Student Profile**: View and update contact information.
- 🧾 **Mess Record Tracking**: Tracks presence/consumption for billing.

---

## 🛠️ Technology Stack

**Backend:**
- Java 17+
- Spring Boot
- Spring Security
- Spring Data JPA (Hibernate)

**Database:**
- MySQL

**Frontend:**
- Thymeleaf (HTML templating)
- Bootstrap (for UI styling)

**Build Tool:**
- Maven

---

## ⚙️ Prerequisites

Make sure you have the following installed:

- Java Development Kit (JDK) 17 or higher
- Apache Maven
- MySQL Server
- Git

---

## 🚀 Getting Started

### 🔽 Cloning the Repository

bash
git clone <repository_url>
cd mess-management-system
Replace <repository_url> with your GitHub clone URL.

## 🗃️ Database Setup
Start your MySQL server.

Create the database:

sql
Copy
Edit
CREATE DATABASE mess_db;
Update src/main/resources/application.properties:

### properties
Copy
Edit
spring.datasource.url=jdbc:mysql://localhost:3306/mess_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
Replace your_mysql_username and your_mysql_password with your MySQL credentials.

## ▶️ Running the Application
In your terminal:

bash
Copy
Edit
./mvnw spring-boot:run
Once the server is up, visit:

arduino
Copy
Edit
http://localhost:8080

## 📋 Usage
Login Page: Visit http://localhost:8080

Admin Login:

Username: admin_001

Password: adminpass (or your manually inserted password hash)

Student Login:

Username: Registration number

Password: Chosen password during registration

Register: New students can register at http://localhost:8080/register

## 🔮 Future Enhancements
Admin approval for new registrations

Automated bill generation and status updates

Email/SMS notifications

Online payment gateway integration

Enhanced reporting (billing history, attendance reports)

Student dashboard with bill & payment tracking

Admin panel for managing student records

## Made with ☕ and 💻 by Deepak
## 🙌 Contributions
Contributions are welcome! Feel free to fork the repo and submit a pull request.

## 📄 License
This project is for educational purposes and currently does not include a license. You may add one as needed.


