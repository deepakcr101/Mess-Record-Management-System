Mess Management System
A web-based application built with Spring Boot to manage mess operations, including user authentication, menu management, billing, reporting, and student account status based on bills.

Table of Contents
Features
Technology Stack
Prerequisites
Getting Started
Cloning the Repository
Database Setup
Running the Application
Usage
Future Enhancements
Features
User Authentication & Authorization: Separate roles for Admin and Student users using Spring Security.
Student Registration: Allows new students to register accounts.
Admin Dashboard: Centralized view for administrators.
Menu Management: Admin interface to add, edit, and delete daily/weekly menu items.
Billing Period Management: Admin functionality to generate bills for students over a specified date range based on mess consumption records.
View Generated Bills: Admin view to see all generated bills.
Financial Summary Report: Admin report showing total billed, total paid, and total due amounts.
Account Locking: Admin can initiate a process to automatically lock student accounts with outstanding (unpaid, past-due) bills.
Student Profile: Students can view and potentially update their contact information.
Mess Record Tracking: Implicitly handled as part of the billing process (records of mess consumption/presence).
Technology Stack
Backend:
Java
Spring Boot
Spring Security (Authentication and Authorization)
Spring Data JPA (Data Access Layer)
Hibernate (JPA Implementation)
Database:
MySQL
Frontend:
Thymeleaf (Server-side templating engine)
Bootstrap (CSS Framework for basic styling)
Build Tool:
Maven
Prerequisites
Before running the application, you need to have the following installed:

Java Development Kit (JDK) 17 or higher
Apache Maven
MySQL Server
Git
Getting Started
Follow these steps to get your local copy up and running.

Cloning the Repository
Bash

git clone <repository_url>
cd mess-management-system
(Replace <repository_url> with the actual URL of your Git repository)

Database Setup
Ensure your MySQL server is running.

Create a database named mess_db.

SQL

CREATE DATABASE mess_db;
Update the database connection properties in 1  src/main/resources/application.properties with your MySQL username and password.   
1.
github.com
github.com

Properties

spring.datasource.url=jdbc:mysql://localhost:3306/mess_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
# Hibernate will automatically create/update the database schema based on entities
spring.jpa.hibernate.ddl-auto=update
(Replace your_mysql_username and your_mysql_password)

Running the Application
Navigate to the project root directory in your terminal and execute the Spring Boot run command using the Maven wrapper:

Bash

./mvnw spring-boot:run
This command will compile the project, update the database schema (if ddl-auto=update is set and changes are needed), and start the embedded Tomcat server.

The application will be accessible at http://localhost:8080.

Usage
Access the application: Open your web browser and go to http://localhost:8080. You should see the login page.
Admin Login: Use the admin account you manually created in the database. If you followed previous instructions, the credentials might be:
Username: admin_001
Password: adminpass (or the plain text password corresponding to the BCrypt hash you inserted)
Student Login: Use the registration number of a registered student as the username and their corresponding password.
Student Registration: You can register new students via the /register page (http://localhost:8080/register).
Future Enhancements
Admin workflow for approving new student registrations.
Automated scheduled bill generation and account status updates.
Email or SMS notifications for bills and payments.
Online payment gateway integration.
More detailed reports (e.g., daily attendance, individual student billing history).
Student interface for viewing their bills, payment history, and consumption.
Dedicated admin pages for managing students.
