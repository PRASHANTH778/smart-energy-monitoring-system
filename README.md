# IoT-Based Automated Energy Monitoring and Management System for Smart Buildings

A full-stack, enterprise-grade energy monitoring and automated management platform tailored for modern commercial campuses, educational facilities, and high-density industrial buildings.

The system integrates **IoT smart meters**, **time-series telemetry processing**, **automated threshold alert generation**, and **interactive energy analytics** to help building facility managers detect electrical anomalies, reduce peak-demand utility surcharges, and optimize facility sustainability.

---

## 🏛 System Architecture

```mermaid
graph TD
    subgraph IoT Field Layer
        ESP1["ESP32 Meter (CRAC Unit)"]
        ESP2["ESP32 Meter (Chiller Plant)"]
        ESP3["Sub-Panel Monitor (Robotics Lab)"]
        SIM["IoTSimulationService (@Scheduled 4000ms)"]
    end

    subgraph Backend - Spring Boot (Port 8081)
        API["REST API & IoT Ingestion (/api/iot, /api/energy)"]
        SEC["Spring Security + JWT Authentication"]
        ENG["Energy Calculation Engine (Trapezoidal Integration)"]
        ALT["Automated Alert & Anomaly Detector"]
    end

    subgraph Persistence Layer - MySQL (Port 3306)
        DB[("MySQL Database: smart_energy_db")]
    end

    subgraph Frontend - React + Vite (Port 5173)
        DASH["Dashboard & Real-Time KPIs"]
        CHART["Energy Charts (Load Curves, Demand)"]
        DEV["Device & Relay Management"]
        ALRT["Incident & Alert Triage Center"]
    end

    ESP1 -->|HTTP POST / Ingest| API
    ESP2 -->|HTTP POST / Ingest| API
    ESP3 -->|HTTP POST / Ingest| API
    SIM -->|Simulated Telemetry| ENG
    API --> SEC
    API --> ENG
    ENG --> ALT
    ENG --> DB
    ALT --> DB
    DASH -->|Axios Bearer JWT| API
    CHART -->|Axios Bearer JWT| API
    DEV -->|Axios Bearer JWT| API
    ALRT -->|Axios Bearer JWT| API
```

---

## 📁 Mandatory Project Structure

The project strictly separates the frontend and backend into independent decoupled environments:

```text
smart-energy-monitoring-system/
├── frontend/              # React + Vite Application
│   ├── src/
│   │   ├── components/    # Reusable UI cards, tables, badges, layouts
│   │   ├── pages/         # Dashboard, Analytics, Devices, Alerts, Reports, Settings
│   │   ├── services/      # Axios API communication layer
│   │   │   ├── axiosClient.js
│   │   │   ├── authService.js
│   │   │   ├── deviceService.js
│   │   │   ├── energyService.js
│   │   │   ├── alertService.js
│   │   │   └── api.js
│   │   ├── App.jsx        # Routing configuration
│   │   └── main.jsx       # DOM mount
│   ├── .env               # VITE_API_BASE_URL=http://localhost:8081/api
│   ├── package.json       # React dependencies and scripts
│   └── README.md          # Frontend documentation
├── backend/               # Java 21 + Spring Boot Application
│   ├── src/main/java/com/smartenergy/
│   │   ├── controller/    # REST endpoints (auth, devices, energy, alerts, dashboard)
│   │   ├── service/       # Business logic, calculations, energy integration
│   │   ├── repository/    # Spring Data JPA repositories
│   │   ├── entity/        # Domain entities (User, IoTDevice, SensorReading, Alert)
│   │   ├── config/        # Security, JWT, CORS, DataInitializer
│   │   └── iot/           # IoT simulation engine, telemetry controller, MQTT gateway
│   ├── src/main/resources/application.properties # Server port 8081, MySQL configuration
│   ├── pom.xml            # Maven project descriptor
│   └── README.md          # Backend documentation
├── database/
│   └── schema.sql         # Complete MySQL DDL & DML seed dataset
├── .gitignore             # Git ignore configuration
└── README.md              # Project master documentation
```

---

## ⚡ Tech Stack Summary

| Layer | Technologies |
| :--- | :--- |
| **Frontend** | React 18, Vite 5, JavaScript (ES6+), Tailwind CSS, Recharts, Lucide Icons, Axios |
| **Backend** | Java 21 LTS, Spring Boot 3.3.4, Spring Data JPA, Spring Security 6, JJWT 0.12.6, Maven |
| **Database** | MySQL 8.0 (`smart_energy_db`) |
| **IoT / Edge** | Simulated ESP32 microcontrollers, PZEM-004T / CT Clamps telemetry, MQTT integration hook |

---

## 🚀 Step-by-Step Setup and Execution

### 1. Database Setup (MySQL)

Ensure MySQL is running on `localhost:3306`. Create the database and seed it:

```bash
# Log in to MySQL client
mysql -u root -p

# Create database and import schema
CREATE DATABASE IF NOT EXISTS smart_energy_db;
USE smart_energy_db;
SOURCE database/schema.sql;
```

*(Note: If MySQL runs with user `root` and password `root`, the Spring Boot backend will also automatically create and update the schema on first run via `ddl-auto=update` and seed initial records with `DataInitializer`).*

---

### 2. Backend Terminal

Open a dedicated terminal, navigate to the `backend` directory, and run the Spring Boot service:

```bash
cd backend
mvn spring-boot:run
```

* **Backend Port:** `8081`
* **API Base URL:** `http://localhost:8081/api`
* **Health / Actuator:** `http://localhost:8081/actuator/health`

---

### 3. Frontend Terminal

Open a second terminal, navigate to the `frontend` directory, install packages, and start the Vite dev server:

```bash
cd frontend
npm install
npm run dev
```

* **Frontend Port:** `5173`
* **Web Dashboard URL:** `http://localhost:5173`

---

## 🔐 Demonstration Credentials

The database comes pre-seeded with four role accounts for testing and evaluation:

| Role | Username | Password | Key Responsibilities |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` | Complete system access, device CRUD, user admin |
| **Energy Manager** | `manager` | `manager123` | Tariff modifications, baseline analysis, reports |
| **Facility Operator** | `operator` | `operator123` | Smart relay switching, alert triage & acknowledgement |
| **Auditor / Guest** | `viewer` | `viewer123` | Read-only analytics & executive dashboards |

---

## 🔬 IoT Simulation & Automated Alert Verification

1. **Automatic Simulation**: Once the backend starts, `IoTSimulationService` begins dispatching simulated multi-sensor telemetry every 4 seconds.
2. **Threshold Violation**: The simulation automatically injects periodic high-demand spikes (> 3000 W) and voltage deviations (< 200V or > 255V).
3. **Automated Alert Generation**: When a limit is breached, the system creates an alert in the `alerts` table with `HIGH` or `MEDIUM` severity.
4. **Interactive Surge Demonstration**:
   You can manually trigger an immediate load surge on any device via curl or frontend:
   ```bash
   curl -X POST http://localhost:8081/api/iot/spike/ESP32-002
   ```
   Check the **Alerts** tab on `http://localhost:5173` to see the new alert appear in real time!
5. **Relay Control**: Navigate to the **Devices** tab and click the **Toggle** button on any sub-meter to remotely turn it off/on.

---

## 🧪 Testing

### Backend Unit & Integration Tests:
```bash
cd backend
mvn test
```

### Frontend Production Build:
```bash
cd frontend
npm run build
```
