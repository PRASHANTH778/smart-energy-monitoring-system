import React, { useState, useEffect } from 'react';
import {
  LineChart as LineChartIcon,
  Calendar,
  DollarSign,
  TrendingUp,
  Percent,
  Layers,
  Cpu,
  RefreshCw,
  Award,
} from 'lucide-react';
import {
  AreaChart,
  Area,
  BarChart,
  Bar,
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

const EnergyAnalytics = () => {
  const [range, setRange] = useState('today');
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchAnalytics = async (selectedRange) => {
    setLoading(true);
    try {
      const data = await energyApi.getAnalytics(selectedRange);
      setAnalytics(data);
    } catch (err) {
      console.error('Error fetching analytics:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnalytics(range);
  }, [range]);

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">Advanced Energy Analytics</h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Historical consumption patterns, baseline variance, cost analytics, and power quality.
          </p>
        </div>

        {/* Time Range Selector */}
        <div className="flex items-center gap-1 bg-white p-1 rounded-xl border border-slate-200 shadow-2xs">
          <button
            onClick={() => setRange('today')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              range === 'today' ? 'bg-emerald-600 text-white shadow-xs' : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            Today (Hourly)
          </button>
          <button
            onClick={() => setRange('week')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              range === 'week' ? 'bg-emerald-600 text-white shadow-xs' : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            Past 7 Days
          </button>
          <button
            onClick={() => setRange('month')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
              range === 'month' ? 'bg-emerald-600 text-white shadow-xs' : 'text-slate-600 hover:bg-slate-100'
            }`}
          >
            Past 30 Days
          </button>
        </div>
      </div>

      {/* KPI Highlights */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Aggregated Energy"
          value={analytics?.totalEnergyKwh?.toFixed(2) || '0.00'}
          unit="kWh"
          icon={TrendingUp}
          color="emerald"
          subtitle={`Window: ${range.toUpperCase()}`}
        />
        <StatCard
          title="Estimated Energy Cost"
          value={`$${analytics?.totalCost || '0.00'}`}
          unit="USD"
          icon={DollarSign}
          color="teal"
          subtitle="Tariff calculated"
        />
        <StatCard
          title="Average Power Demand"
          value={analytics?.avgPowerWatts?.toFixed(1) || '0.0'}
          unit="Watts"
          icon={LineChartIcon}
          color="indigo"
          subtitle="Mean instantaneous draw"
        />
        <StatCard
          title="Efficiency Savings"
          value={`$${analytics?.baselineSavings || '0.00'}`}
          unit="saved"
          icon={Award}
          color="amber"
          subtitle="Relative to target baseline"
          badge={<Badge variant="success">ECO GAIN</Badge>}
        />
      </div>

      {/* Main Trends Chart */}
      <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h2 className="text-sm font-semibold text-slate-900">
              {range === 'today' ? '24-Hour Energy Ingestion Profile' : 'Daily Energy Ingestion Profile'}
            </h2>
            <p className="text-xs text-slate-500">Continuous time-integrated power curve in kWh</p>
          </div>
          <button onClick={() => fetchAnalytics(range)} className="p-1 rounded-lg text-slate-400 hover:text-slate-700">
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
        </div>

        <div className="h-80">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart
              data={range === 'today' ? analytics?.hourlyTrends || [] : analytics?.dailyTrends || []}
              margin={{ top: 10, right: 10, left: -20, bottom: 0 }}
            >
              <defs>
                <linearGradient id="analyticsGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#06b6d4" stopOpacity={0.4} />
                  <stop offset="95%" stopColor="#06b6d4" stopOpacity={0.0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
              <XAxis dataKey="label" stroke="#94a3b8" fontSize={11} />
              <YAxis stroke="#94a3b8" fontSize={11} unit=" kWh" />
              <Tooltip
                contentStyle={{ backgroundColor: '#0f172a', borderColor: '#1e293b', borderRadius: '8px', color: '#fff', fontSize: '12px' }}
                formatter={(val, name) => [`${val} kWh`, name === 'energyKwh' ? 'Energy Consumed' : 'Target Baseline']}
              />
              <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '8px' }} />
              <Area type="monotone" dataKey="energyKwh" name="Energy Consumed (kWh)" stroke="#06b6d4" strokeWidth={2.5} fillOpacity={1} fill="url(#analyticsGrad)" />
              <Area type="stepAfter" dataKey="targetKwh" name="Target Cap (kWh)" stroke="#f59e0b" strokeWidth={2} strokeDasharray="4 4" fillOpacity={0} />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Zone and Device Consumption Comparison */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Zone Breakdown Bar Chart */}
        <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-sm font-semibold text-slate-900">Zone Consumption vs Target</h2>
              <p className="text-xs text-slate-500">Comparison of zone energy versus assigned targets</p>
            </div>
            <Layers className="w-4 h-4 text-slate-400" />
          </div>

          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={analytics?.zoneBreakdown || []} margin={{ top: 10, right: 10, left: -20, bottom: 20 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                <XAxis dataKey="zoneName" stroke="#94a3b8" fontSize={10} angle={-15} textAnchor="end" />
                <YAxis stroke="#94a3b8" fontSize={11} unit=" kWh" />
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#1e293b', borderRadius: '8px', color: '#fff', fontSize: '12px' }}
                  formatter={(val, name) => [`${val} kWh`, name === 'energyKwh' ? 'Actual' : 'Target']}
                />
                <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '8px' }} />
                <Bar dataKey="energyKwh" name="Actual Energy" fill="#10b981" radius={[4, 4, 0, 0]} />
                <Bar dataKey="targetKwh" name="Budget Limit" fill="#cbd5e1" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Device Breakdown Table */}
        <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-sm font-semibold text-slate-900">IoT Device Energy Contribution</h2>
              <p className="text-xs text-slate-500">Energy proportion per monitoring node</p>
            </div>
            <Cpu className="w-4 h-4 text-slate-400" />
          </div>

          <div className="overflow-x-auto max-h-64 overflow-y-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider text-[10px] sticky top-0 border-b border-slate-100">
                <tr>
                  <th className="py-2 px-3">Device</th>
                  <th className="py-2 px-3">Energy (kWh)</th>
                  <th className="py-2 px-3">Contribution</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {(analytics?.deviceBreakdown || []).map((dev) => (
                  <tr key={dev.deviceId} className="hover:bg-slate-50">
                    <td className="py-2.5 px-3">
                      <div className="font-semibold text-slate-800">{dev.deviceName}</div>
                      <div className="text-[10px] font-mono text-slate-400">{dev.deviceId}</div>
                    </td>
                    <td className="py-2.5 px-3 font-semibold text-slate-900 font-mono">
                      {dev.energyKwh} kWh
                    </td>
                    <td className="py-2.5 px-3">
                      <div className="flex items-center gap-2">
                        <div className="w-20 bg-slate-100 rounded-full h-1.5 overflow-hidden">
                          <div
                            className="bg-emerald-500 h-1.5 rounded-full"
                            style={{ width: `${Math.min(100, dev.percentage)}%` }}
                          />
                        </div>
                        <span className="text-[11px] font-mono text-slate-600">{dev.percentage}%</span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EnergyAnalytics;
