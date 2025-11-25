# Unified Billing & Reporting Platform for Multi-Client, Multi-Vendor Operations

A comprehensive multi-tenant corporate transportation management system built with **Spring Boot** and **Flutter**. This platform manages client onboarding, client-vendor relationships, automated billing calculations, and comprehensive reporting for ride-sharing shuttle services.

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (Flutter)                       │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐│
│  │   Admin     │ │   Client    │ │    Vendor/Employee      ││
│  │ Dashboard   │ │ Dashboard   │ │     Dashboard           ││
│  └─────────────┘ └─────────────┘ └─────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
                    ┌─────────▼─────────┐
                    │   REST API        │
                    │  (Spring Boot)    │
                    └─────────┬─────────┘
                              │
                    ┌─────────▼─────────┐
                    │   H2 Database     │
                    │   (In-Memory)     │
                    └───────────────────┘
```

## 🛠️ Tech Stack

### Backend

- **Framework**: Spring Boot 4.0.0
- **Language**: Java 17
- **Database**: H2 (in-memory)
- **Security**: Spring Security + JWT Authentication
- **ORM**: Spring Data JPA
- **Validation**: Jakarta Validation
- **Build Tool**: Maven
- **Additional Libraries**:
  - Lombok for boilerplate reduction
  - JJWT for JWT token handling

### Frontend

- **Framework**: Flutter 3.5.4+
- **Language**: Dart
- **HTTP Client**: Dio
- **Charts**: FL Chart
- **PDF Generation**: PDF + Printing packages
- **Local Storage**: Shared Preferences

## ✨ Key Features

### 🔐 Multi-Role Authentication System

- **Admin**: System-wide management and analytics
- **Client**: Employee management and trip oversight
- **Vendor**: Self-service dashboard and rate management
- **Employee**: Personal trip history and incentives

### 📊 Client-Vendor Management

- Manual client-vendor relationship setup via Admin
- Billing model compatibility matching (Package, Trip, Hybrid)

### 💰 Flexible Billing Models

- **Package Model**: Fixed monthly rates
- **Trip Model**: Per-trip billing
- **Hybrid Model**: Base monthly + per-trip charges
- Automated billing calculations with overage handling

### 📈 Comprehensive Reporting

- Role-based data filtering and tenant isolation
- Real-time performance analytics
- PDF report generation
- Interactive charts and visualizations

### 🎯 Multi-Tenant Architecture

- Strict data isolation between clients
- Scalable design for enterprise growth
- Secure role-based access control

## 🔒 Security Features

- **JWT Authentication**: Secure token-based authentication
- **Role-Based Access Control**: Granular permissions per user role
- **Data Isolation**: Multi-tenant architecture with strict data separation
- **Input Validation**: Comprehensive validation using Jakarta Validation
- **CORS Configuration**: Configurable cross-origin resource sharing

## 📊 Database Schema

The system uses a comprehensive H2 database schema with the following key entities:

- **Users**: Multi-role user management
- **Vendor Profiles**: Capacity and billing configuration
- **Client Profiles**: Business requirements and preferences
- **Trips**: Transportation records with billing data
- **Client-Vendor Assignments**: Relationship management
- **Payouts**: Financial transaction tracking


## 🖥️ User Interfaces

### Admin Dashboard

- System-wide analytics and metrics
- User management and role assignment
- Cross-tenant reporting

### Client Dashboard

- Trip booking and tracking
- Client performance monitoring
- Billing summary and cost analysis

### Vendor Dashboard

- Client assignment overview
- Trip management and tracking
- Performance metrics and earnings
- Rate management (self-service)
- Payout history

### Employee Dashboard

- Personal trip history
- Performance reports



## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.6+
- Flutter 3.5.4+
- Dart SDK

### Backend Setup

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd billing-platform-mis
   ```

2. **Database Setup**

   ```bash
   # No database setup required - uses H2 in-memory database
   # Data is automatically created when the application starts
   ```

3. **Set Environment Variables**

   ```bash
   # Copy and configure environment file
   cp .env.example .env
   # Edit .env with your JWT settings (database is pre-configured for H2)
   ```

4. **Run the Backend**
   ```bash
   mvn spring-boot:run
   ```

### Frontend Setup

1. **Navigate to Frontend Directory**

   ```bash
   cd frontend/billing_dashboard
   ```

2. **Install Dependencies**

   ```bash
   flutter pub get
   ```

3. **Configure Environment**

   ```bash
   # Update .env file with backend API URL
   echo "API_BASE_URL=http://localhost:8082/api" > .env
   ```

4. **Run the Frontend**
   ```bash
   flutter run
   ```



