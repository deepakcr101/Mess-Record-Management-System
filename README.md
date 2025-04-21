🍽️ Mess Management System
A web-based application built with Spring Boot to manage mess operations, including user authentication, menu management, billing, reporting, and managing student account status based on bills.

📑 Table of Contents
Features

Technology Stack

Prerequisites

Getting Started

Cloning the Repository

Database Setup

Running the Application

Usage

Future Enhancements

✅ Features
User Authentication & Authorization: Separate roles for Admin and Student users using Spring Security.

Student Registration: Allows new students to register accounts.

Admin Dashboard: Centralized view for administrators.

Menu Management: Admin interface to add, edit, and delete daily/weekly menu items.

Billing Period Management: Admin functionality to generate bills based on mess consumption records.

View Generated Bills: Admin view to see all generated bills.

Financial Summary Report: Shows total billed, total paid, and total due amounts.

Account Locking: Automatically locks student accounts with outstanding bills.

Student Profile: Students can view and update contact information.

Mess Record Tracking: Implicitly handled through billing.

🛠️ Technology Stack
Backend
Java

Spring Boot

Spring Security

Spring Data JPA

Hibernate

Database
MySQL

Frontend
Thymeleaf

Bootstrap

Build Tool
Maven

📦 Prerequisites
Make sure the following are installed:

Java Development Kit (JDK) 17 or higher

Apache Maven

MySQL Server

Git

🚀 Getting Started
🔁 Cloning the Repository
bash
Copy
Edit
git clone <repository_url>
cd mess-management-system
Replace <repository_url> with the actual URL of your GitHub repository.

🛢️ Database Setup
Ensure MySQL server is running.

Create the database:

sql
Copy
Edit
CREATE DATABASE mess_db;
Update src/main/resources/application.properties:

properties
Copy
Edit
spring.datasource.url=jdbc:mysql://localhost:3306/mess_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
Replace your_mysql_username and your_mysql_password with your actual credentials.

▶️ Running the Application
In the terminal, from the root project directory:

bash
Copy
Edit
./mvnw spring-boot:run
The application will start on:
👉 http://localhost:8080

💻 Usage
Login at http://localhost:8080

Admin Login:
Username: admin_001

Password: adminpass (or the plain password matching your stored hash)

Student Login:
Use your registration number as username and corresponding password.

Register a New Student:
Visit http://localhost:8080/register

🔮 Future Enhancements
Admin workflow for approving new student registrations.

Automated bill generation and account status updates.

Email/SMS notifications for bills and payments.

Online payment gateway integration.

Detailed reporting: attendance, billing history.

Student interface for payment history and consumption.

Advanced admin panels for student management.


