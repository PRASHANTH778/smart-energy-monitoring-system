import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Layout from './components/layout/Layout';

// Pages
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import EnergyAnalytics from './pages/EnergyAnalytics';
import LiveMonitoring from './pages/LiveMonitoring';
import BuildingsZones from './pages/BuildingsZones';
import DeviceManagement from './pages/DeviceManagement';
import Alerts from './pages/Alerts';
import AutomationRules from './pages/AutomationRules';
import Reports from './pages/Reports';
import Settings from './pages/Settings';
import UserProfile from './pages/UserProfile';

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="h-screen w-screen flex items-center justify-center bg-slate-950 text-emerald-400 font-mono text-xs">
        Initializing SmartEnergy BMS System...
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Authentication Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Protected Application Routes */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Dashboard />} />
            <Route path="analytics" element={<EnergyAnalytics />} />
            <Route path="monitoring" element={<LiveMonitoring />} />
            <Route path="buildings" element={<BuildingsZones />} />
            <Route path="devices" element={<DeviceManagement />} />
            <Route path="alerts" element={<Alerts />} />
            <Route path="automation" element={<AutomationRules />} />
            <Route path="reports" element={<Reports />} />
            <Route path="settings" element={<Settings />} />
            <Route path="profile" element={<UserProfile />} />
          </Route>

          {/* Catch-all */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
