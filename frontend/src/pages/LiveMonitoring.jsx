import React, { useState, useEffect } from 'react';
import {
  Activity,
  Zap,
  Radio,
  RefreshCw,
  PlusCircle,
  Filter,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
  Send,
} from 'lucide-react';
import { energyApi } from '../api/energyApi';
import Modal from '../components/common/Modal';
import Badge from '../components/common/Badge';

const LiveMonitoring = () => {
  const [readings, setReadings] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [injectModalOpen, setInjectModalOpen] = useState(false);
  const [devices, setDevices] = useState([]);

  // Ingestion form state
  const [injectForm, setInjectForm] = useState({
    deviceId: 'ESP32-001',
    voltage: 230.0,
    current: 4.5,
    powerFactor: 0.95,
  });
  const [injectSuccess, setInjectSuccess] = useState('');

  const fetchReadings = async (pageNumber = 0) => {
    try {
      const data = await energyApi.getReadings(pageNumber, 15, 'timestamp', 'desc');
      setReadings(data.content || []);
      setTotalPages(data.totalPages || 1);
      setPage(data.number || 0);
    } catch (err) {
      console.error('Failed to fetch readings:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReadings(page);
    energyApi.getDevices().then((res) => setDevices(res || [])).catch(() => {});
  }, [page]);

  useEffect(() => {
    if (!autoRefresh) return;
    const interval = setInterval(() => {
      fetchReadings(page);
    }, 4000); // 4s live poll
    return () => clearInterval(interval);
  }, [autoRefresh, page]);

  const handleInject = async (e) => {
    e.preventDefault();
    try {
      const p = injectForm.voltage * injectForm.current * injectForm.powerFactor;
      await energyApi.ingestReading({
        ...injectForm,
        power: Math.round(p * 10.0) / 10.0,
        timestamp: new Date().toISOString(),
      });
      setInjectSuccess('Packet injected successfully!');
      fetchReadings(0);
      setTimeout(() => {
        setInjectSuccess('');
        setInjectModalOpen(false);
      }, 1200);
    } catch (err) {
      alert('Error injecting telemetry: ' + (err.response?.data?.message || err.message));
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold text-slate-900 tracking-tight">Live IoT Telemetry Monitoring</h1>
            <span className="flex h-2.5 w-2.5 relative">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-emerald-500"></span>
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-0.5">
            Streaming electrical telemetry: Voltage (V), Current (A), Active Power (W), Energy (kWh), and Power Factor.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setAutoRefresh(!autoRefresh)}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold border flex items-center gap-1.5 transition-colors ${
              autoRefresh
                ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                : 'bg-slate-100 text-slate-600 border-slate-200'
            }`}
          >
            <Radio className={`w-3.5 h-3.5 ${autoRefresh ? 'text-emerald-500 animate-pulse' : 'text-slate-400'}`} />
            <span>{autoRefresh ? 'Live Stream Active (4s)' : 'Stream Paused'}</span>
          </button>

          <button
            onClick={() => setInjectModalOpen(true)}
            className="px-3 py-1.5 rounded-lg text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white flex items-center gap-1.5 shadow-xs transition-colors"
          >
            <PlusCircle className="w-3.5 h-3.5" />
            <span>Simulate Packet Injection</span>
          </button>
        </div>
      </div>

      {/* Real-time Telemetry Grid Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="p-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="text-xs font-semibold text-slate-800">
            Telemetry Packet Log ({readings.length} records in view)
          </div>
          <div className="text-xs text-slate-500">
            Page {page + 1} of {totalPages}
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-100/70 text-slate-600 uppercase tracking-wider text-[10px] border-b border-slate-200">
              <tr>
                <th className="py-3 px-4">Node / Device</th>
                <th className="py-3 px-4">Zone & Building</th>
                <th className="py-3 px-4">Voltage (V)</th>
                <th className="py-3 px-4">Current (A)</th>
                <th className="py-3 px-4">Active Power (W)</th>
                <th className="py-3 px-4">Interval Energy (kWh)</th>
                <th className="py-3 px-4">Power Factor</th>
                <th className="py-3 px-4">Origin</th>
                <th className="py-3 px-4">Timestamp</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {readings.map((r) => (
                <tr key={r.id} className="hover:bg-slate-50/80 transition-colors">
                  <td className="py-3 px-4 font-semibold text-slate-800">
                    <div>{r.deviceName}</div>
                    <div className="text-[10px] font-mono text-slate-400">{r.deviceIdentifier}</div>
                  </td>
                  <td className="py-3 px-4">
                    <div className="text-slate-800 font-medium">{r.zoneName}</div>
                    <div className="text-[10px] text-slate-400">{r.buildingName}</div>
                  </td>
                  <td className="py-3 px-4 font-mono font-medium text-slate-700">{r.voltage} V</td>
                  <td className="py-3 px-4 font-mono font-medium text-slate-700">{r.current} A</td>
                  <td className="py-3 px-4 font-mono font-bold text-emerald-600">{r.power} W</td>
                  <td className="py-3 px-4 font-mono text-slate-800 font-semibold">{r.energyKwh} kWh</td>
                  <td className="py-3 px-4 font-mono">
                    <span
                      className={`px-2 py-0.5 rounded text-[11px] font-semibold ${
                        r.powerFactor < 0.88 ? 'bg-amber-100 text-amber-800' : 'bg-slate-100 text-slate-700'
                      }`}
                    >
                      {r.powerFactor}
                    </span>
                  </td>
                  <td className="py-3 px-4">
                    {r.simulated ? (
                      <Badge variant="default" className="text-[10px]">SIMULATED</Badge>
                    ) : (
                      <Badge variant="online" className="text-[10px]">MQTT LIVE</Badge>
                    )}
                  </td>
                  <td className="py-3 px-4 text-slate-500 text-[11px] font-mono">
                    {new Date(r.timestamp).toLocaleTimeString()}
                  </td>
                </tr>
              ))}
              {readings.length === 0 && (
                <tr>
                  <td colSpan="9" className="py-12 text-center text-xs text-slate-400">
                    No telemetry packets recorded yet. Waiting for incoming telemetry...
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Controls */}
        <div className="p-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-600">
          <button
            disabled={page <= 0}
            onClick={() => setPage(page - 1)}
            className="px-3 py-1.5 rounded-lg border border-slate-200 disabled:opacity-40 hover:bg-slate-50 flex items-center gap-1"
          >
            <ChevronLeft className="w-3.5 h-3.5" /> Previous
          </button>
          <span>
            Page {page + 1} of {Math.max(1, totalPages)}
          </span>
          <button
            disabled={page >= totalPages - 1}
            onClick={() => setPage(page + 1)}
            className="px-3 py-1.5 rounded-lg border border-slate-200 disabled:opacity-40 hover:bg-slate-50 flex items-center gap-1"
          >
            Next <ChevronRight className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      {/* Manual Packet Injection Modal */}
      <Modal
        isOpen={injectModalOpen}
        onClose={() => setInjectModalOpen(false)}
        title="Simulate Inbound Telemetry Packet (Viva Demonstration)"
      >
        <form onSubmit={handleInject} className="space-y-4">
          <p className="text-xs text-slate-500">
            Inject a custom sensor payload over the REST telemetry endpoint to test real-time trapezoidal calculations and automated threshold alerts.
          </p>

          {injectSuccess && (
            <div className="p-3 rounded-lg bg-emerald-50 text-emerald-700 text-xs flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 text-emerald-500" />
              <span>{injectSuccess}</span>
            </div>
          )}

          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Target IoT Device</label>
            <select
              value={injectForm.deviceId}
              onChange={(e) => setInjectForm({ ...injectForm, deviceId: e.target.value })}
              className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
            >
              {devices.map((d) => (
                <option key={d.deviceId} value={d.deviceId}>
                  {d.name} ({d.deviceId}) - {d.zoneName}
                </option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Voltage (V)</label>
              <input
                type="number"
                step="0.1"
                required
                value={injectForm.voltage}
                onChange={(e) => setInjectForm({ ...injectForm, voltage: parseFloat(e.target.value) })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Current (A)</label>
              <input
                type="number"
                step="0.1"
                required
                value={injectForm.current}
                onChange={(e) => setInjectForm({ ...injectForm, current: parseFloat(e.target.value) })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Power Factor</label>
              <input
                type="number"
                step="0.01"
                min="0.5"
                max="1.0"
                required
                value={injectForm.powerFactor}
                onChange={(e) => setInjectForm({ ...injectForm, powerFactor: parseFloat(e.target.value) })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>
          </div>

          <div className="p-3 rounded-lg bg-slate-50 border border-slate-100 text-xs">
            <span className="text-slate-500">Calculated Active Power: </span>
            <span className="font-bold text-emerald-600 font-mono">
              {(injectForm.voltage * injectForm.current * injectForm.powerFactor).toFixed(1)} Watts
            </span>
            <div className="text-[10px] text-slate-400 mt-0.5">
              (Tip: Set current to 15.0 A to trigger the &gt;3000 W surge overload alert!)
            </div>
          </div>

          <button
            type="submit"
            className="w-full py-2.5 px-4 rounded-xl text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 flex items-center justify-center gap-2 shadow-xs transition-colors cursor-pointer"
          >
            <Send className="w-3.5 h-3.5" />
            <span>Transmit Telemetry Packet</span>
          </button>
        </form>
      </Modal>
    </div>
  );
};

export default LiveMonitoring;
