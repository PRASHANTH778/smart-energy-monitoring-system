import React, { useState, useEffect } from 'react';
import { Menu, Bell, Shield, Radio, RefreshCw, User, LogOut } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { energyApi } from '../../api/energyApi';
import Badge from '../common/Badge';

const Navbar = ({ toggleMobileMenu }) => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [simulationActive, setSimulationActive] = useState(true);
  const [activeAlertsCount, setActiveAlertsCount] = useState(0);
  const [dropdownOpen, setDropdownOpen] = useState(false);

  useEffect(() => {
    const fetchStatus = async () => {
      try {
        const summary = await energyApi.getDashboardSummary();
        setSimulationActive(summary.simulationModeActive);
        setActiveAlertsCount(summary.activeAlertsCount || 0);
      } catch (err) {
        // graceful
      }
    };
    fetchStatus();
    const interval = setInterval(fetchStatus, 15000);
    return () => clearInterval(interval);
  }, []);

  return (
    <header className="h-16 bg-white border-b border-slate-200/80 px-4 md:px-8 flex items-center justify-between sticky top-0 z-30 shadow-2xs">
      {/* Left section: mobile hamburger & breadcrumb */}
      <div className="flex items-center gap-3">
        <button
          onClick={toggleMobileMenu}
          className="md:hidden p-2 rounded-lg text-slate-600 hover:bg-slate-100 focus:outline-none"
        >
          <Menu className="w-5 h-5" />
        </button>

        <div className="hidden sm:flex items-center gap-2 text-xs text-slate-500">
          <span className="font-semibold text-slate-700">IoT Smart Facility</span>
          <span>/</span>
          <span className="text-slate-500">Continuous Energy Ingestion</span>
        </div>
      </div>

      {/* Right section: Simulation badge, Alerts counter, User profile */}
      <div className="flex items-center gap-4">
        {/* Simulation Mode Badge */}
        {simulationActive && (
          <div className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-amber-50 border border-amber-300 text-amber-900 text-xs font-semibold shadow-2xs">
            <Radio className="w-3.5 h-3.5 text-amber-600 animate-pulse" />
            <span>SIMULATION MODE</span>
          </div>
        )}

        {/* Alerts quick badge */}
        <Link
          to="/alerts"
          className="relative p-2 rounded-lg text-slate-500 hover:text-slate-800 hover:bg-slate-100 transition-colors"
          title="View Active Alerts"
        >
          <Bell className="w-5 h-5" />
          {activeAlertsCount > 0 && (
            <span className="absolute top-1.5 right-1.5 w-4 h-4 bg-rose-500 text-white rounded-full text-[10px] font-bold flex items-center justify-center animate-bounce">
              {activeAlertsCount}
            </span>
          )}
        </Link>

        {/* User Dropdown */}
        <div className="relative">
          <button
            onClick={() => setDropdownOpen(!dropdownOpen)}
            className="flex items-center gap-2.5 p-1 rounded-lg hover:bg-slate-50 transition-colors focus:outline-none"
          >
            <div className="w-8 h-8 rounded-full bg-emerald-600 text-white font-semibold flex items-center justify-center text-xs shadow-xs">
              {user?.username ? user.username.substring(0, 2).toUpperCase() : 'US'}
            </div>
            <div className="hidden lg:block text-left">
              <div className="text-xs font-semibold text-slate-800">{user?.fullName || user?.username}</div>
              <div className="text-[10px] text-slate-400 font-mono">{user?.role?.replace('ROLE_', '')}</div>
            </div>
          </button>

          {dropdownOpen && (
            <div
              className="absolute right-0 mt-2 w-48 bg-white rounded-xl shadow-xl border border-slate-100 py-1.5 z-40"
              onClick={() => setDropdownOpen(false)}
            >
              <div className="px-4 py-2 border-b border-slate-100">
                <div className="text-xs font-semibold text-slate-800">{user?.fullName}</div>
                <div className="text-[11px] text-slate-400 truncate">{user?.email}</div>
              </div>
              <Link
                to="/profile"
                className="flex items-center gap-2 px-4 py-2 text-xs text-slate-700 hover:bg-slate-50 transition-colors"
              >
                <User className="w-4 h-4 text-slate-400" />
                User Profile
              </Link>
              <Link
                to="/settings"
                className="flex items-center gap-2 px-4 py-2 text-xs text-slate-700 hover:bg-slate-50 transition-colors"
              >
                <Shield className="w-4 h-4 text-slate-400" />
                System Settings
              </Link>
              <div className="border-t border-slate-100 my-1"></div>
              <button
                onClick={logout}
                className="w-full flex items-center gap-2 px-4 py-2 text-xs text-rose-600 hover:bg-rose-50 transition-colors text-left"
              >
                <LogOut className="w-4 h-4 text-rose-500" />
                Sign Out
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Navbar;
