import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  LineChart,
  Activity,
  Building2,
  Cpu,
  Bell,
  Zap,
  FileText,
  Settings,
  User,
  LogOut,
  ShieldCheck,
  Radio,
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

const navItems = [
  { name: 'Dashboard', path: '/', icon: LayoutDashboard },
  { name: 'Energy Analytics', path: '/analytics', icon: LineChart },
  { name: 'Live Monitoring', path: '/monitoring', icon: Activity },
  { name: 'Buildings & Zones', path: '/buildings', icon: Building2 },
  { name: 'IoT Devices', path: '/devices', icon: Cpu },
  { name: 'Alerts & Events', path: '/alerts', icon: Bell },
  { name: 'Automation Rules', path: '/automation', icon: Zap },
  { name: 'Reports', path: '/reports', icon: FileText },
  { name: 'Settings', path: '/settings', icon: Settings },
  { name: 'User Profile', path: '/profile', icon: User },
];

const Sidebar = ({ closeMobileMenu }) => {
  const { user, logout } = useAuth();

  return (
    <aside className="w-64 bg-slate-900 text-slate-300 flex flex-col h-screen border-r border-slate-800 select-none">
      {/* Brand Header */}
      <div className="h-16 flex items-center gap-3 px-6 border-b border-slate-800/80 bg-slate-950/40">
        <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-400 flex items-center justify-center text-white shadow-md shadow-emerald-950/40">
          <Zap className="w-5 h-5 text-white" />
        </div>
        <div>
          <div className="font-bold text-white tracking-tight text-sm leading-tight flex items-center gap-1.5">
            SmartEnergy <span className="text-emerald-400 font-mono text-xs">IoT</span>
          </div>
          <p className="text-[11px] text-slate-400 font-medium">BMS Energy Automation</p>
        </div>
      </div>

      {/* Navigation Links */}
      <div className="flex-1 py-4 px-3 overflow-y-auto space-y-1">
        <div className="px-3 pb-2 text-[10px] font-semibold text-slate-400 uppercase tracking-wider">
          Management Console
        </div>
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              onClick={closeMobileMenu}
              end={item.path === '/'}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-xs font-medium transition-all ${
                  isActive
                    ? 'bg-emerald-500/10 text-emerald-400 font-semibold border-l-2 border-emerald-400 pl-3'
                    : 'text-slate-400 hover:text-slate-100 hover:bg-slate-800/60'
                }`
              }
            >
              <Icon className="w-4 h-4 shrink-0" />
              <span>{item.name}</span>
            </NavLink>
          );
        })}
      </div>

      {/* System Status Footprint */}
      <div className="px-4 py-3 border-t border-slate-800/60 bg-slate-950/30">
        <div className="flex items-center justify-between text-[11px] text-slate-400 mb-1">
          <span className="flex items-center gap-1.5">
            <Radio className="w-3.5 h-3.5 text-emerald-400 animate-pulse" />
            Telemetry Stream
          </span>
          <span className="text-emerald-400 font-mono text-[10px]">ACTIVE</span>
        </div>
        <div className="text-[10px] text-slate-400">
          Spring Boot 3.3.4 &bull; MySQL 8 &bull; MQTT
        </div>
      </div>

      {/* User Session Footer */}
      <div className="p-3 border-t border-slate-800 bg-slate-950/60 flex items-center justify-between">
        <div className="flex items-center gap-2.5 min-w-0">
          <div className="w-8 h-8 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-emerald-400 text-xs font-bold shrink-0">
            {user?.username ? user.username.substring(0, 2).toUpperCase() : 'US'}
          </div>
          <div className="min-w-0 flex-1">
            <div className="text-xs font-semibold text-slate-200 truncate">{user?.fullName || user?.username}</div>
            <div className="text-[10px] text-slate-400 font-mono truncate">{user?.role?.replace('ROLE_', '') || 'OPERATOR'}</div>
          </div>
        </div>
        <button
          onClick={logout}
          title="Sign Out"
          className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-slate-800 transition-colors"
        >
          <LogOut className="w-4 h-4" />
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
