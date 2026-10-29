import React, { useState, useEffect } from 'react';
import {
  Zap,
  DollarSign,
  Cpu,
  WifiOff,
  Bell,
  Gauge,
  Activity,
  Lightbulb,
  ArrowUpRight,
  TrendingUp,
  RefreshCw,
  Radio,
  CheckCircle2,
  Clock,
  Layers,
} from 'lucide-react';
import {
  AreaChart,
  Area,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts';
import { energyApi } from '../api/energyApi';
import StatCard from '../components/common/StatCard';
import Badge from '../components/common/Badge';

const ZONE_COLORS = ['#10b981', '#06b6d4', '#6366f1', '#f59e0b', '#ec4899'];

const Dashboard = () => {
  const [summary, setSummary] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [lastRefreshed, setLastRefreshed] = useState(new Date());

  const fetchData = async () => {
    try {
      const [sumData, anaData] = await Promise.all([
        energyApi.getDashboardSummary(),
        energyApi.getAnalytics('today'),
      ]);
      setSummary(sumData);
      setAnalytics(anaData);
      setLastRefreshed(new Date());
    } catch (err) {
      console.error('Failed to load dashboard data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    if (!autoRefresh) return;
    const interval = setInterval(fetchData, 5000); // 5s live polling
    return () => clearInterval(interval);
  }, [autoRefresh]);

  if (loading && !summary) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="flex flex-col items-center gap-3">
          <RefreshCw className="w-8 h-8 text-emerald-500 animate-spin" />
          <p className="text-xs text-slate-500 font-medium">Loading building telemetry & analytics...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top Banner & Control Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-xl font-bold text-slate-900 tracking-tight">Executive Energy Dashboard</h1>
            {summary?.simulationModeActive && (
              <Badge variant="simulation">SIMULATION MODE ACTIVE</Badge>
            )}
          </div>
          <p className="text-xs text-slate-500 mt-0.5">
            Real-time IoT smart meter telemetry, power calculations, and automated demand mitigation.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 text-xs text-slate-500">
            <Clock className="w-3.5 h-3.5" />
            <span>Updated: {lastRefreshed.toLocaleTimeString()}</span>
          </div>

          <button
            onClick={() => setAutoRefresh(!autoRefresh)}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium border flex items-center gap-1.5 transition-colors ${
              autoRefresh
                ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                : 'bg-slate-100 text-slate-600 border-slate-200'
            }`}
          >
            <Radio className={`w-3.5 h-3.5 ${autoRefresh ? 'text-emerald-500 animate-pulse' : 'text-slate-400'}`} />
            <span>{autoRefresh ? 'Live Polling (5s)' : 'Polling Paused'}</span>
          </button>

          <button
            onClick={fetchData}
            title="Refresh Now"
            className="p-1.5 rounded-lg bg-white border border-slate-200 text-slate-600 hover:bg-slate-50 transition-colors"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* 6 Core KPI Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        <StatCard
          title="Instant Active Power"
          value={summary?.currentPowerWatts?.toLocaleString() || '0'}
          unit="Watts"
          icon={Zap}
          color="emerald"
          subtitle="Real-time building demand"
        />

        <StatCard
          title="Today's Energy"
          value={summary?.energyTodayKwh?.toFixed(2) || '0.00'}
          unit="kWh"
          icon={Gauge}
          color="blue"
          subtitle="Integrated Trapezoidal ∑"
        />

        <StatCard
          title="Today's Cost"
          value={`${summary?.currency || '$'}${summary?.estimatedCostToday || '0.00'}`}
          unit="est."
          icon={DollarSign}
          color="teal"
          subtitle="Configured tariff rate"
        />

        <StatCard
          title="Connected Devices"
          value={summary?.connectedDevicesCount || 0}
          unit="online"
          icon={Cpu}
          color="indigo"
          badge={<Badge variant="online">ONLINE</Badge>}
        />

        <StatCard
          title="Offline Devices"
          value={summary?.offlineDevicesCount || 0}
          unit="nodes"
          icon={WifiOff}
          color="rose"
          badge={summary?.offlineDevicesCount > 0 ? <Badge variant="offline">HEARTBEAT LOST</Badge> : <Badge variant="online">ALL HEALTHY</Badge>}
        />

        <StatCard
          title="Active Alerts"
          value={summary?.activeAlertsCount || 0}
          unit="unresolved"
          icon={Bell}
          color="amber"
          badge={summary?.activeAlertsCount > 0 ? <Badge variant="high">REQUIRES ATTENTION</Badge> : <Badge variant="success">NOMINAL</Badge>}
        />
      </div>

      {/* Main Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Hourly Consumption vs Target (2 cols) */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-sm font-semibold text-slate-900">Today's Hourly Energy & Target Benchmark</h2>
              <p className="text-xs text-slate-500">Hourly integrated energy (kWh) compared against dynamic peak ceiling</p>
            </div>
            <span className="text-xs font-medium text-emerald-600 bg-emerald-50 px-2.5 py-1 rounded-md border border-emerald-100">
              Trapezoidal Integration
            </span>
          </div>

          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={analytics?.hourlyTrends || []} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="energyGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                <XAxis dataKey="label" stroke="#94a3b8" fontSize={11} />
                <YAxis stroke="#94a3b8" fontSize={11} unit=" kWh" />
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#1e293b', borderRadius: '8px', color: '#fff', fontSize: '12px' }}
                  formatter={(value, name) => [`${value} kWh`, name === 'energyKwh' ? 'Measured Energy' : 'Target Limit']}
                />
                <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '8px' }} />
                <Area type="monotone" dataKey="energyKwh" name="Energy Consumed (kWh)" stroke="#10b981" strokeWidth={2} fillOpacity={1} fill="url(#energyGrad)" />
                <Area type="stepAfter" dataKey="targetKwh" name="Target Cap (kWh)" stroke="#f59e0b" strokeWidth={2} strokeDasharray="4 4" fillOpacity={0} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Zone Energy Breakdown (1 col) */}
        <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-sm font-semibold text-slate-900">Zone-wise Consumption</h2>
              <p className="text-xs text-slate-500">Distribution across facility zones</p>
            </div>
            <Layers className="w-4 h-4 text-slate-400" />
          </div>

          <div className="h-56">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={summary?.topConsumingZones || []}
                  dataKey="energyKwh"
                  nameKey="zoneName"
                  cx="50%"
                  cy="50%"
                  innerRadius={50}
                  outerRadius={80}
                  paddingAngle={4}
                >
                  {(summary?.topConsumingZones || []).map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={ZONE_COLORS[index % ZONE_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#1e293b', borderRadius: '8px', color: '#fff', fontSize: '12px' }}
                  formatter={(val, name, props) => [`${val} kWh (${props.payload.percentageOfTotal}%)`, name]}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>

          {/* Zone list breakdown */}
          <div className="space-y-2 mt-2 max-h-36 overflow-y-auto pr-1">
            {(summary?.topConsumingZones || []).map((z, idx) => (
              <div key={z.zoneId} className="flex items-center justify-between text-xs py-1 border-b border-slate-50">
                <div className="flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: ZONE_COLORS[idx % ZONE_COLORS.length] }}></span>
                  <span className="text-slate-700 font-medium truncate max-w-[130px]">{z.zoneName}</span>
                </div>
                <div className="flex items-center gap-3">
                  <span className="font-semibold text-slate-900">{z.energyKwh} kWh</span>
                  <span className="text-slate-500 text-[11px] font-mono">{z.percentageOfTotal}%</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Advisory & Automation Actions */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Smart Energy Advisory Card */}
        <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
          <div className="flex items-center gap-2.5 mb-3">
            <div className="p-2 rounded-lg bg-amber-50 text-amber-600 border border-amber-100">
              <Lightbulb className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-sm font-semibold text-slate-900">Automated Energy-Saving Advisory</h2>
              <p className="text-xs text-slate-500">System intelligence recommendations based on live load curves</p>
            </div>
          </div>

          <div className="space-y-2.5 mt-4">
            {(summary?.energySavingRecommendations || []).map((rec, i) => (
              <div key={i} className="p-3 rounded-lg bg-slate-50 border border-slate-100 text-xs text-slate-700 flex items-start gap-2.5">
                <ArrowUpRight className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
                <span>{rec}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Recent Automation Executions Audit Log */}
        <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center gap-2.5">
              <div className="p-2 rounded-lg bg-teal-50 text-teal-600 border border-teal-100">
                <Activity className="w-4 h-4" />
              </div>
              <div>
                <h2 className="text-sm font-semibold text-slate-900">Recent Automated Engine Actions</h2>
                <p className="text-xs text-slate-500">Autonomous responses triggered by smart rules</p>
              </div>
            </div>
            <Badge variant="info">AUDIT LOG</Badge>
          </div>

          <div className="space-y-2.5 mt-4 max-h-56 overflow-y-auto">
            {summary?.recentAutomationActions && summary.recentAutomationActions.length > 0 ? (
              summary.recentAutomationActions.slice(0, 5).map((act) => (
                <div key={act.id} className="p-3 rounded-lg bg-slate-50 border border-slate-100 text-xs space-y-1">
                  <div className="flex items-center justify-between font-semibold text-slate-800">
                    <span>{act.ruleName}</span>
                    <span className="text-[10px] text-slate-500 font-normal">
                      {new Date(act.executedAt).toLocaleTimeString()}
                    </span>
                  </div>
                  <div className="text-slate-600 text-[11px]">{act.actionTaken}</div>
                  <div className="text-[10px] text-emerald-600 font-mono flex items-center gap-1">
                    <CheckCircle2 className="w-3 h-3 text-emerald-500" />
                    <span>{act.status}</span>
                  </div>
                </div>
              ))
            ) : (
              <div className="py-8 text-center text-xs text-slate-400">
                No automated triggers executed recently. Nominal operating baseline maintained.
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Recent Sensor Telemetry & Active Alerts Table */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Recent Readings Table (2 cols) */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-sm font-semibold text-slate-900">Real-Time Ingestion Feed</h2>
              <p className="text-xs text-slate-500">Latest sensor packets received over MQTT / REST</p>
            </div>
            <span className="text-xs text-slate-500">Showing 5 latest</span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider text-[10px] border-y border-slate-100">
                <tr>
                  <th className="py-2.5 px-3">Device / Node</th>
                  <th className="py-2.5 px-3">Zone</th>
                  <th className="py-2.5 px-3">Voltage</th>
                  <th className="py-2.5 px-3">Current</th>
                  <th className="py-2.5 px-3">Active Power</th>
                  <th className="py-2.5 px-3">PF</th>
                  <th className="py-2.5 px-3">Timestamp</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {(summary?.recentReadings || []).slice(0, 6).map((r) => (
                  <tr key={r.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-2.5 px-3 font-semibold text-slate-800">
                      {r.deviceName}
                      <div className="text-[10px] font-mono text-slate-400">{r.deviceIdentifier}</div>
                    </td>
                    <td className="py-2.5 px-3 text-slate-600">{r.zoneName}</td>
                    <td className="py-2.5 px-3 font-mono">{r.voltage} V</td>
                    <td className="py-2.5 px-3 font-mono">{r.current} A</td>
                    <td className="py-2.5 px-3 font-semibold text-emerald-600 font-mono">{r.power} W</td>
                    <td className="py-2.5 px-3 font-mono">{r.powerFactor}</td>
                    <td className="py-2.5 px-3 text-slate-500 text-[11px]">
                      {new Date(r.timestamp).toLocaleTimeString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Recent Alerts List (1 col) */}
        <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-sm font-semibold text-slate-900">Latest Active Alerts</h2>
              <p className="text-xs text-slate-500">Exceptions flagged by threshold guards</p>
            </div>
            <Bell className="w-4 h-4 text-slate-400" />
          </div>

          <div className="space-y-3">
            {(summary?.recentAlerts || []).slice(0, 4).map((alert) => (
              <div key={alert.id} className="p-3 rounded-lg border border-slate-100 bg-slate-50 text-xs space-y-1">
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-slate-800 truncate max-w-[180px]">{alert.title}</span>
                  <Badge variant={alert.severity.toLowerCase()}>{alert.severity}</Badge>
                </div>
                <p className="text-slate-600 text-[11px] line-clamp-2">{alert.description}</p>
                <div className="text-[10px] text-slate-400 pt-1 flex items-center justify-between">
                  <span>{alert.zoneName || 'Facility Wide'}</span>
                  <span>{new Date(alert.createdAt).toLocaleTimeString()}</span>
                </div>
              </div>
            ))}
            {(!summary?.recentAlerts || summary.recentAlerts.length === 0) && (
              <div className="py-8 text-center text-xs text-slate-400">
                No active alerts. All building parameters are nominal.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
