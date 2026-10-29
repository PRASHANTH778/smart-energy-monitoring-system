# Smart Energy Monitoring System - Backend Application

A robust, enterprise-grade backend service built with **Java 21**, **Spring Boot 3.3.4**, **Spring Data JPA**, and **MySQL** for real-time IoT energy monitoring, telemetry ingestion, automated anomaly detection, and energy billing calculation.

---

## 🚀 Key Features

* **Real-Time IoT Simulation Engine (`com.smartenergy.iot.IoTSimulationService`)**:
  * Simulates electrical telemetry (AC Voltage 220V–240V, Current Amperage, Active Power Watts, Power Factor, and Frequency) across connected sub-meters.
  * Injects periodic load fluctuations and high-demand power surges to validate automated alert triggers.
  * Provides a manual spike injection endpoint for interactive live presentations.
* **Automated Anomaly & Threshold Alert Generator**:
  * Continuously evaluates incoming telemetry against configured safety limits.
  * Automatically creates categorized incidents (HIGH_POWER, ABNORMAL_USAGE, LOW_POWER_FACTOR, VOLTAGE_SAG) with cooldown suppression to avoid notification flooding.
* **Numerical Energy Integration**:
  * Implements trapezoidal numerical integration ($E = \int P \, dt$) to convert instantaneous power readings into accurate cumulative kilowatt-hours (kWh).
* **Role-Based Security & JWT Authentication**:
  * Stateless token-based security via JJWT (`io.jsonwebtoken 0.12.6`).
  * BCrypt password hashing and role enforcement across `ADMIN`, `MANAGER`, `OPERATOR`, and `VIEWER`.
* **CORS & Preflight Handling**:
  * Pre-configured for seamless integration with the frontend at `http://localhost:5173`.
* **Database Auto-Seeding**:
  * `DataInitializer` seeds demonstration users, facilities, zones, smart meters, automation rules, and 48 hours of historical readings on startup.

---

## 🛠 Tech Stack

| Technology | Version / Specification | Purpose |
| :--- | :--- | :--- |
| **Java** | 21 (LTS) | Modern Java language runtime |
| **Spring Boot** | 3.3.4 | Core enterprise application framework |
| **Spring Data JPA** | 3.3.4 (Hibernate 6) | ORM and transactional persistence |
| **MySQL Connector/J**| 8.3+ | High-performance MySQL database driver |
| **Spring Security** | 6.3+ | Authentication, authorization and CORS |
| **JJWT** | 0.12.6 | JSON Web Token generation & validation |
| **Eclipse Paho MQTT**| 1.2.5 | MQTT client for edge hardware (ESP32) |
| **Maven** | 3.9+ | Build and dependency management |

---

## 📁 Package Architecture

```text
backend/src/main/java/com/smartenergy/
├── config/                  # SecurityConfig, JwtTokenProvider, DataInitializer, UserDetails
├── controller/              # REST API controllers
│   ├── AuthController.java
│   ├── DeviceController.java (IoTDeviceController)
│   ├── EnergyController.java
│   ├── SensorReadingController.java
│   ├── AlertController.java
│   ├── DashboardController.java
│   ├── EnergyAnalyticsController.java
│   ├── BuildingController.java
│   ├── ZoneController.java
│   ├── ReportController.java
│   ├── TariffController.java
│   ├── AutomationController.java
│   └── SettingsController.java
├── service/                 # Business logic & calculation services
│   ├── AuthService.java
│   ├── IoTDeviceService.java
│   ├── SensorReadingService.java
│   ├── AlertService.java
│   ├── EnergyAnalyticsService.java
│   ├── EnergyCalculationService.java
│   ├── BuildingService.java
│   ├── ZoneService.java
│   ├── ReportService.java
│   ├── TariffService.java
│   └── AutomationService.java
├── repository/              # Spring Data JPA repositories
│   ├── UserRepository.java
│   ├── IoTDeviceRepository.java
│   ├── SensorReadingRepository.java
│   ├── AlertRepository.java
│   ├── BuildingRepository.java
│   ├── ZoneRepository.java
│   ├── EnergyTariffRepository.java
│   ├── AutomationRuleRepository.java
│   └── AutomationExecutionRepository.java
├── entity/                  # JPA domain entities
│   ├── User.java
│   ├── Role.java
│   ├── IoTDevice.java
│   ├── SensorReading.java
│   ├── Alert.java
│   ├── Building.java
│   ├── Zone.java
│   ├── EnergyTariff.java
│   ├── AutomationRule.java
│   └── AutomationExecution.java
├── iot/                     # Real-time IoT simulation and field gateways
│   ├── IoTSimulationService.java
│   ├── IoTTelemetryController.java
│   └── MqttGatewayService.java
├── dto/                     # Request and response data transfer objects
└── exception/               # GlobalExceptionHandler and custom exceptions
```

---

## ⚙️ Configuration (`application.properties`)

The configuration file is located at `backend/src/main/resources/application.properties`:

```properties
# Server Port
server.port=8081

# MySQL Database
spring.datasource.url=jdbc:mysql://localhost:3306/smart_energy_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA & Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT Security
app.jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
app.jwt.expiration-ms=86400000

# IoT Simulation
app.simulation.enabled=true
app.simulation.interval-ms=4000
```

---

## 🚦 Getting Started

### 1. Prerequisites
* **Java 21** or later installed and configured (`java -version`)
* **Maven 3.9+** installed (`mvn -version`)
* **MySQL 8.0+** running on `localhost:3306`

### 2. Prepare Database
The database will be automatically created on startup, or you can run:

```sql
CREATE DATABASE IF NOT EXISTS smart_energy_db;
```

*(Optional: Run `database/schema.sql` if manual initialization is preferred).*

### 3. Run Backend
From the `backend` folder:

```bash
cd backend
mvn spring-boot:run
```

The service will start on:
👉 **`http://localhost:8081`**

### 4. Run Unit Tests
```bash
mvn test
```

---

## 🔌 Primary REST Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/login` | Authenticate user and receive JWT bearer token |
| `POST` | `/api/auth/register` | Register new user account |
| `GET` | `/api/auth/profile` | Retrieve current authenticated user profile |
| `GET` | `/api/dashboard/summary`| Complete facility overview (total kWh, active kW, alert count) |
| `GET` | `/api/energy/summary` | Instantaneous power and energy summary |
| `GET` | `/api/energy/latest` | Recent sensor telemetry across all devices |
| `GET` | `/api/energy/analytics?range=today`| Time-series load curve data for charts |
| `GET` | `/api/devices` | List all IoT sub-meters and monitored equipment |
| `POST` | `/api/devices` | Register a new IoT device |
| `PUT` | `/api/devices/{id}` | Update device configuration |
| `DELETE`| `/api/devices/{id}` | Delete device |
| `POST` | `/api/devices/{id}/toggle` | Toggle device active / relay state |
| `GET` | `/api/alerts` | Paginated alerts with severity filtering |
| `GET` | `/api/alerts/recent` | 10 most recent active alerts |
| `GET` | `/api/alerts/count` | Count of unresolved alerts |
| `PUT` | `/api/alerts/{id}/acknowledge`| Acknowledge an alert |
| `PUT` | `/api/alerts/{id}/resolve`| Mark an alert as resolved |
| `POST` | `/api/iot/telemetry` | Ingest external sensor reading payload |
| `POST` | `/api/iot/spike/{deviceId}`| Inject instantaneous power surge for testing |
| `POST` | `/api/iot/toggle` | Toggle background IoT simulation |

