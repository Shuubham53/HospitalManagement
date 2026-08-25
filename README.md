# Hospital Management System 🏥

> A backend hospital management system built with **Spring Boot** to manage patients, doctors, appointments, billing, hospital inventory, and online payments with Stripe.

**Backend:** Java + Spring Boot + PostgreSQL

---

## ✨ Features

### 🔐 Authentication

* User registration and login
* JWT-based authentication
* Access token and refresh token generation
* BCrypt password hashing
* Role-based authorization
* Method-level security
* Account activation / soft delete
* Request validation
* Global exception handling

### 👤 Patient Management

* Register patients
* Patient login
* View patient profile
* Update patient information
* Soft delete patient accounts
* Validation for patient data

### 👨‍⚕️ Doctor Management

* Register doctors
* Doctor login
* View doctor profile
* Update doctor information
* Doctor email synchronization with the authentication user
* Soft delete doctor accounts
* Doctor-specific appointment management

### 📅 Appointments

* Create appointments
* View appointments
* Update appointments
* Doctor appointment confirmation
* Complete appointments
* Cancel appointments
* Appointment status management
* Appointment timing validation
* Business rules for appointment cancellation

### 💳 Billing & Payments

* Create and manage billing records
* Track payment status
* Store Stripe PaymentIntent references
* Stripe PaymentIntent integration
* Card payment processing
* Stripe webhook handling
* Automatic billing update after successful payment
* Refund processing
* Protection against duplicate refunds

### 📦 Inventory

* Create inventory items
* View inventory by ID
* View all inventory
* Update inventory
* Track quantity and reorder levels
* Automatic inventory status management
* Expiration-date validation

Inventory status is automatically handled based on stock:

```text
Quantity > Reorder Level
        ↓
    AVAILABLE

Quantity <= Reorder Level
        ↓
    LOW_STOCK

Quantity = 0
        ↓
  OUT_OF_STOCK
```

---

## 🛠️ Tech Stack

### Backend

| Technology         | Purpose                        |
| ------------------ | ------------------------------ |
| Java               | Core language                  |
| Spring Boot        | Backend framework              |
| Spring Security    | Authentication & Authorization |
| JWT                | Token-based authentication     |
| Spring Data JPA    | Data persistence               |
| Hibernate          | ORM                            |
| Lombok             | Boilerplate reduction          |
| BCrypt             | Password hashing               |
| Jakarta Validation | Request validation             |

### Database & Payments

| Technology | Purpose                      |
| ---------- | ---------------------------- |
| PostgreSQL | Primary database             |
| Stripe     | Online payments              |
| Ngrok      | Local Stripe webhook testing |

### Development Tools

| Tool          | Purpose               |
| ------------- | --------------------- |
| IntelliJ IDEA | Development           |
| Postman       | API testing           |
| DBeaver       | Database management   |
| Maven         | Dependency management |
| Git & GitHub  | Version control       |

---

## 🏗️ Architecture

The application follows a layered Spring Boot architecture:

```text
                         ┌─────────────────┐
                         │     Client      │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │   Controllers   │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │    Services     │
                         │ Business Logic  │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │   Repositories  │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │   PostgreSQL    │
                         └─────────────────┘
```

### Security Flow

```text
Login
  │
  ▼
AuthenticationManager
  │
  ▼
JWT Access + Refresh Tokens
  │
  ▼
JWT Filter
  │
  ▼
Authentication & Authorization
  │
  ▼
Protected API
```

---

## 💳 Stripe Payment Flow

The payment system uses Stripe PaymentIntent and webhook events.

```text
Patient
   │
   ▼
Create PaymentIntent
   │
   ▼
Stripe
   │
   ▼
Card Payment
   │
   ▼
payment_intent.succeeded
   │
   ▼
Stripe Webhook
   │
   ▼
PaymentController
   │
   ▼
BillingService
   │
   ▼
Billing Status → PAID
```

Refunds are also handled through the backend, with validation to prevent a bill that has already been refunded from being refunded again.

---

## 🗄️ Database

The application uses **PostgreSQL** with JPA/Hibernate.

Main entities include:

```text
User
 ├── Patient
 └── Doctor

Patient ─────── Appointment
Doctor ──────── Appointment

Patient ─────── Billing

Inventory
```

Main database tables:

```text
users
patient
doctor
appointment
billing
inventory
```

---

## 🚀 Getting Started

### Prerequisites

Make sure you have:

* Java 17+
* Maven
* PostgreSQL
* IntelliJ IDEA or another Java IDE
* Stripe test account for payment testing

### Clone the repository

```bash
git clone https://github.com/Shuubham53/HospitalManagement.git
cd HospitalManagement
```

### Configure the database

Create a PostgreSQL database and configure the connection in your local application configuration.

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Configure environment variables / secrets

Sensitive credentials should be supplied through environment variables or local configuration.

```text
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
JWT_SECRET
STRIPE_SECRET_KEY
STRIPE_WEBHOOK_SECRET
```

Do **not** commit real credentials or API keys to GitHub.

### Run the application

Using Maven:

```bash
mvn spring-boot:run
```

Or run the Spring Boot application directly from IntelliJ IDEA.

The backend runs on:

```text
http://localhost:8080
```

---

## 📡 API Endpoints

### 🔐 Authentication

| Method | Endpoint         | Description          |
| ------ | ---------------- | -------------------- |
| POST   | `/auth/register` | Register user        |
| POST   | `/auth/login`    | Login                |
| POST   | `/auth/refresh`  | Refresh access token |

### 👤 Patient

| Method | Endpoint            | Description         |
| ------ | ------------------- | ------------------- |
| POST   | `/patient/register` | Register patient    |
| GET    | `/patient/...`      | Patient information |
| PUT    | `/patient/...`      | Update patient      |
| DELETE | `/patient/...`      | Soft delete patient |

### 👨‍⚕️ Doctor

| Method | Endpoint           | Description        |
| ------ | ------------------ | ------------------ |
| POST   | `/doctor/register` | Register doctor    |
| GET    | `/doctor/...`      | Doctor information |
| PUT    | `/doctor/...`      | Update doctor      |
| DELETE | `/doctor/...`      | Soft delete doctor |

### 📅 Appointment

| Method | Endpoint           | Description               |
| ------ | ------------------ | ------------------------- |
| POST   | `/appointment`     | Create appointment        |
| GET    | `/appointment/...` | Retrieve appointments     |
| PUT    | `/appointment/...` | Update appointment        |
| PATCH  | `/appointment/...` | Update appointment status |

### 💳 Billing & Payments

| Method | Endpoint                          | Description                       |
| ------ | --------------------------------- | --------------------------------- |
| GET    | `/billing/...`                    | Retrieve billing information      |
| POST   | `/payments/create-payment-intent` | Create Stripe PaymentIntent       |
| POST   | `/payments/webhook`               | Receive Stripe webhook            |
| POST   | `/payments/...`                   | Process payment/refund operations |

### 📦 Inventory

| Method | Endpoint            | Description         |
| ------ | ------------------- | ------------------- |
| POST   | `/inventories`      | Create inventory    |
| GET    | `/inventories`      | Get all inventory   |
| GET    | `/inventories/{id}` | Get inventory by ID |
| PUT    | `/inventories/{id}` | Update inventory    |

> Endpoint paths may vary slightly depending on the controller mappings in the current version of the project.

---

## 🗂️ Project Structure

```text
src/main/java/com/Application/
│
├── config/
│   ├── SecurityConfig
│   ├── StripeConfig
│   └── ...
│
├── controller/
│   ├── AuthController
│   ├── PatientController
│   ├── DoctorController
│   ├── AppointmentController
│   ├── BillingController
│   ├── PaymentController
│   └── InventoryController
│
├── dto/
│   ├── request/
│   └── response/
│
├── entity/
│   ├── User
│   ├── Patient
│   ├── Doctor
│   ├── Appointment
│   ├── Billing
│   └── Inventory
│
├── exception/
│   └── Global Exception Handling
│
├── repository/
│   └── JPA Repositories
│
├── security/
│   ├── JwtFilter
│   └── JWT utilities
│
└── service/
    ├── AuthService
    ├── PatientService
    ├── DoctorService
    ├── AppointmentService
    ├── BillingService
    ├── PaymentService
    └── InventoryService
```

---

## 🧪 Testing

The major backend workflows have been manually tested using API requests.

Tested:

* JWT authentication
* Role-based authorization
* Patient operations
* Doctor operations
* Appointment lifecycle
* Appointment cancellation rules
* Billing operations
* Stripe PaymentIntent
* Card payment
* Stripe webhook
* Payment → billing synchronization
* Refund
* Duplicate refund protection
* Inventory CRUD
* Inventory stock-status rules
* Expiration validation
* Global exception handling
* Soft deletion
* CORS

During testing, real integration issues were identified and fixed, including Stripe payment-state handling, webhook connectivity, authentication issues, and synchronization between Doctor and User records.

---

## 🔒 Security

Sensitive configuration must never be committed to source control.

The following should remain local or be provided through environment variables:

```text
application.properties
Database credentials
JWT secrets
Stripe API keys
Stripe webhook secrets
Email/API credentials
```

For production deployments, use a proper secret-management mechanism.

---


## 👨‍💻 Author

**Shubham Nishad**

GitHub: [@Shuubham53](https://github.com/Shuubham53)

---

## 📄 License

This project is open source and available under the **MIT License**.

---

⭐ If you found this project useful, consider giving it a star!
