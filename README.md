# Mess Management System

A comprehensive, full-stack web application designed to streamline mess and cafeteria operations for educational institutions and organizations. This system provides a seamless experience for both administrators and students, from menu management to online payments.

## Features

### For Students (Users)

- **Authentication:** Secure user registration and login.
- **Profile Management:** View and update personal information.
- **Weekly Menu:** View the complete meal menu for the week.
- **Subscription:** Purchase monthly or custom mess subscription plans.
- **Individual Meal Purchase:** Buy single meals on the go.
- **Meal Tracking:** Mark meal entries (breakfast, lunch, dinner).
- **History:** View subscription status, purchase history, and meal entry records.

### For Administrators

- **User Management:** Full CRUD (Create, Read, Update, Delete) operations for user accounts.
- **Menu Management:** Create, update, and manage menu items. Set the weekly menu with ease.
- **Subscription Management:** View and manage all user subscriptions.
- **Reporting:** Generate insightful reports on:
  - Total meal consumption.
  - Sales from subscriptions and individual purchases.
  - User activity and trends.
- **Dashboard:** A centralized dashboard for a complete overview of the system's operations.

## Tech Stack

### Backend

- **Java:** A robust and scalable foundation.
- **Spring Boot:** For building RESTful APIs with speed and efficiency.
- **Spring Security & JWT:** For secure authentication and authorization.
- **Spring Data JPA (Hibernate):** For powerful and flexible database interactions.
- **MySQL/PostgreSQL:** (or any other relational database).
- **Stripe:** Integrated for secure online payment processing.
- **Maven:** For project dependency management.

### Frontend

- **React:** A declarative and efficient JavaScript library for building user interfaces.
- **TypeScript:** For adding static typing to JavaScript, improving code quality and maintainability.
- **Vite:** A fast and modern build tool for frontend development.
- **Material-UI (MUI):** A comprehensive suite of UI tools to help you ship new features faster.
- **Axios:** For making HTTP requests to the backend API.
- **React Router:** For declarative routing in the application.

## Getting Started

Follow these instructions to get a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- **Java JDK 17 or higher:** [Download & Install Java](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- **Maven:** [Download & Install Maven](https://maven.apache.org/download.cgi)
- **Node.js and npm:** [Download & Install Node.js](https://nodejs.org/)
- **MySQL or PostgreSQL:** Install and configure a relational database.
- **Stripe Account:** To handle payments, you'll need a Stripe account and API keys.

### Backend Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/mess-management-system.git
   cd mess-management-system/backend
   ```

2. **Configure the database:**
   - Open `src/main/resources/application.properties`.
   - Update the `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` properties to match your database configuration.

3. **Set Environment Variables:**
   - Create a `.env` file in the `backend` directory.
   - Add the following environment variables:
     ```
     JWT_SECRET=your_jwt_secret
     STRIPE_SECRET_KEY=your_stripe_secret_key
     ```

4. **Install dependencies and run the application:**
   ```bash
   mvn spring-boot:run
   ```
   The backend server will start on `http://localhost:8080`.

### Frontend Setup

1. **Navigate to the frontend directory:**
   ```bash
   cd ../frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Set Environment Variables:**
   - Create a `.env` file in the `frontend` directory.
   - Add the following environment variable:
     ```
     VITE_API_BASE_URL=http://localhost:8080
     ```

4. **Run the application:**
   ```bash
   npm run dev
   ```
   The frontend development server will start on `http://localhost:5173`.

## Configuration

### Environment Variables

- **`JWT_SECRET`:** A secret key for signing and verifying JSON Web Tokens.
- **`STRIPE_SECRET_KEY`:** Your secret API key from your Stripe account.
- **`VITE_API_BASE_URL`:** The base URL of the backend API, which the frontend will use to make requests.

## API Endpoints

A detailed list of API endpoints can be found in the backend source code and will be documented further using a tool like Swagger or Postman.

## Contributing

Contributions are welcome! Please feel free to open an issue or submit a pull request.

1. Fork the Project.
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`).
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the Branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.
