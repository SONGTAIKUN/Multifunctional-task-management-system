# 📝 TaskManager - Spring Boot Task Management System

TaskManager is a full-featured task management web application built with **Spring Boot 3**. It supports user authentication, role-based access control, task creation and management, scheduled background jobs, and additional features like avatar uploads, Excel export, and email notifications.

---

## 🔧 Features

- ✅ **User Registration / Login / JWT Authentication**
- 🔐 **Login Failure Limiting (via Redis)**
- 📋 **Task CRUD Operations with Pagination and Filtering**
- 🧑‍💼 **Admin Panel to Create Tasks for Other Users**
- 🖼️ **Avatar Upload & Static File Access**
- ✉️ **Email Notification Support (Optional)**
- 📤 **Export Tasks to Excel**
- ⏱️ **Scheduled Background Jobs (e.g., Cleanup)**

---

## 📁 Project Structure

```
src/
 └── main/
     ├── java/com/example/taskmanager/
     │   ├── config        # Security and JWT configuration
     │   ├── controller    # RESTful API endpoints
     │   ├── dto           # Data Transfer Objects
     │   ├── model         # JPA Entities (User, Task, etc.)
     │   ├── repository    # Database interaction layer (Spring Data JPA)
     │   ├── scheduler     # Scheduled background jobs
     │   ├── service       # Business logic and services
     │   └── util          # Utility classes (e.g., JwtUtil)
     └── resources/        # Configuration, static files, templates
```

---

## ⚙️ Tech Stack

- **Spring Boot 3.1.3** – Core framework for building and running the application  
- **Spring Security + JWT** – Handles authentication, authorization, and token-based login  
- **Spring Data JPA + MySQL** – ORM and relational database for data persistence  
- **Redis** – In-memory data store for login throttling and caching  
- **Lombok** – Reduces boilerplate code via annotations  
- **Apache POI** – Generates and exports Excel files (.xlsx)  
- **JUnit 5 + Mockito** – Unit and integration testing frameworks  
- **Maven** – Build and dependency management  
- **SMTP Mail (Optional)** – For sending notification emails

---

## 📦 Requirements

- Java 17+  
- Maven 3.6+  
- MySQL 8+  
- Redis  
- SMTP Mail Server (optional, for email reminders)

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-username/taskmanager.git
cd taskmanager
```

### 2. Create and configure your database

```bash
# In MySQL CLI
CREATE DATABASE taskmanager;
```

Then import the schema:

```bash
mysql -u root -p taskmanager < init_taskmanager.sql
```

### 3. Configure `application.properties`

Update database credentials, Redis config, JWT secret, and mail settings inside:
```
src/main/resources/application.properties
```

### 4. Run the project

```bash
./mvnw spring-boot:run
```

or

```bash
mvn spring-boot:run
```

---

## 📬 Contact

Feel free to open issues or submit pull requests. For questions, contact: [a1249587971@icloud.com](mailto:a1249587971@icloud.com)

---

## 📄 License

This project is licensed under the MIT License.
