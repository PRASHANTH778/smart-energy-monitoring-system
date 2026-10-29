# Smart Energy Monitoring System - Frontend Application

A responsive, high-performance web dashboard built with **React.js**, **Vite**, and **Tailwind CSS** for IoT-based automated energy monitoring and management in smart commercial and institutional buildings.

---

## 🚀 Key Features

* **Real-Time Energy Dashboard**: Live KPIs including instantaneous facility power demand (kW), cumulative energy consumption (kWh), active device count, power factor status, and open anomaly alerts.
* **Interactive Energy Analytics**: Multi-timespan (Today, Yesterday, Week, Month) historical load curves, peak demand indicators, and zone-by-zone energy distribution charts powered by Recharts.
* **Device Management**: Full lifecycle management for building sub-meters, smart circuit breakers, HVAC feeders, server room UPS units, and dimmers, including remote switch/relay toggle controls.
* **Alerts & Anomaly Center**: Real-time notification and triage feed for over-power spikes, abnormal voltage sags/swells, and low power factor conditions with one-click **Acknowledge** and **Resolve** workflows.
* **Live Telemetry Stream**: Rapid-refresh gauge metrics (Voltage, Amperage, Active Power, Power Factor, Timestamp) updated directly from backend IoT simulation cycles.
* **Building & Zone Hierarchy**: Multi-facility breakdown tracking energy footprint by floor, lab, server room, and department.
* **Automated Rules & Tariffs**: Threshold-based automated load shedding simulation and cost calculations against commercial peak tariffs.
* **Automated CSV Reports**: On-demand generation and download of audit-ready daily and monthly energy statements.

---

## 🛠 Tech Stack

| Technology | Purpose |
| :--- | :--- |
| **React 18** | UI component architecture and reactive state management |
| **Vite 5** | High-speed frontend build tooling and development server |
| **Tailwind CSS** | Styling framework with high-contrast dark dashboard aesthetics |
| **Recharts** | Smooth SVG charting for load profiles and power trends |
| **Lucide React** | Clean, modern iconography |
| **Axios** | REST API communication with Bearer JWT interceptors |
| **React Router 6** | Client-side routing with authentication guards |

---

## 📁 Directory Structure

```text
frontend/
├── src/
│   ├── components/       # Layout, Navbar, Sidebar, StatCards, Badges, Modals
│   ├── context/          # AuthContext for session management
│   ├── pages/            # Dashboard, Analytics, Devices, Alerts, Reports, Settings
│   ├── services/         # Axios API clients & service modules
│   │   ├── axiosClient.js
│   │   ├── authService.js
│   │   ├── deviceService.js
│   │   ├── energyService.js
│   │   ├── alertService.js
│   │   └── api.js
│   ├── App.jsx           # Main routing configuration
│   ├── main.jsx          # React DOM entry point
│   └── index.css         # Tailwind & theme directives
├── .env                  # Backend API URL configuration
├── package.json          # Node dependencies and build scripts
└── vite.config.js        # Vite build & reverse-proxy configuration
```

---

## ⚙️ Configuration (`.env`)

The frontend communicates with the Spring Boot backend on port `8081`. The base URL is stored in `frontend/.env`:

```env
VITE_API_BASE_URL=http://localhost:8081/api
```

---

## 🚦 Getting Started

### 1. Prerequisites
* **Node.js** v18+ or v20+ / v24+
* **npm** v9+

### 2. Installation
Open a terminal in the `frontend` folder:

```bash
cd frontend
npm install
```

### 3. Launch Development Server
```bash
npm run dev
```

The application will start at:
👉 **`http://localhost:5173`**

### 4. Build for Production
```bash
npm run build
```
Compiled assets will be output to `frontend/dist/`.

---

## 🔑 Default Demonstration Credentials

| Role | Username | Password | Access Level |
| :--- | :--- | :--- | :--- |
| **System Admin** | `admin` | `admin123` | Full administrative control |
| **Energy Manager** | `manager` | `manager123` | Analytics, rule adjustment & reports |
| **Operator** | `operator` | `operator123` | Device controls & alert acknowledgement |
| **Viewer** | `viewer` | `viewer123` | Read-only analytics & dashboard |

