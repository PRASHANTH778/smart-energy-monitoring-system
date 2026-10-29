import React, { useState, useEffect } from 'react';
import {
  Cpu,
  Plus,
  Edit2,
  Trash2,
  Wifi,
  WifiOff,
  Radio,
  Clock,
  Zap,
  Activity,
  Layers,
  CheckCircle2,
  AlertCircle,
  Eye,
} from 'lucide-react';
import { energyApi } from '../api/energyApi';
import Modal from '../components/common/Modal';
import Badge from '../components/common/Badge';

const DeviceManagement = () => {
  const [devices, setDevices] = useState([]);
  const [zones, setZones] = useState([]);
  const [loading, setLoading] = useState(true);

  // Modals
  const [deviceModalOpen, setDeviceModalOpen] = useState(false);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [editingDevice, setEditingDevice] = useState(null);
  const [viewingDevice, setViewingDevice] = useState(null);

  // Form State
  const [deviceForm, setDeviceForm] = useState({
    deviceId: '',
    name: '',
    deviceType: 'ESP32_PZEM004T',
    zoneId: '',
    protocol: 'MQTT',
    firmwareVersion: 'v2.1.0-PROD',
    active: true,
    simulationEnabled: true,
  });

  const [feedback, setFeedback] = useState({ type: '', text: '' });

  const loadData = async () => {
    try {
      const [dData, zData] = await Promise.all([
        energyApi.getDevices(),
        energyApi.getZones(),
      ]);
      setDevices(dData || []);
      setZones(zData || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 8000);
    return () => clearInterval(interval);
  }, []);

  const showFeedback = (type, text) => {
    setFeedback({ type, text });
    setTimeout(() => setFeedback({ type: '', text: '' }), 3500);
  };

  const handleOpenModal = (d = null) => {
    if (d) {
      setEditingDevice(d);
      setDeviceForm({
        deviceId: d.deviceId,
        name: d.name,
        deviceType: d.deviceType || 'ESP32_PZEM004T',
        zoneId: d.zoneId,
        protocol: d.protocol || 'MQTT',
        firmwareVersion: d.firmwareVersion || 'v2.1.0-PROD',
        active: d.active,
        simulationEnabled: d.simulationEnabled,
      });
    } else {
      setEditingDevice(null);
      setDeviceForm({
        deviceId: `ESP32-${String(devices.length + 1).padStart(3, '0')}`,
        name: '',
        deviceType: 'ESP32_PZEM004T',
        zoneId: zones[0]?.id || '',
        protocol: 'MQTT',
        firmwareVersion: 'v2.1.0-PROD',
        active: true,
        simulationEnabled: true,
      });
    }
    setDeviceModalOpen(true);
  };

  const handleSaveDevice = async (e) => {
    e.preventDefault();
    try {
      if (editingDevice) {
        await energyApi.updateDevice(editingDevice.id, deviceForm);
        showFeedback('success', 'IoT device updated successfully');
      } else {
        await energyApi.createDevice(deviceForm);
        showFeedback('success', 'New IoT device registered');
      }
      setDeviceModalOpen(false);
      loadData();
    } catch (err) {
      showFeedback('error', err.response?.data?.message || err.message);
    }
  };

  const handleDeleteDevice = async (id) => {
    if (!window.confirm('Are you sure you want to delete this IoT device? Historical readings will be preserved.')) return;
    try {
      await energyApi.deleteDevice(id);
      showFeedback('success', 'Device deleted');
      loadData();
    } catch (err) {
      showFeedback('error', err.response?.data?.message || err.message);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">IoT Device Inventory & Node Health</h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Manage ESP32 microcontrollers, energy sensor nodes, connection timeouts, and telemetry bindings.
          </p>
        </div>

        <button
          onClick={() => handleOpenModal()}
          className="px-3 py-1.5 rounded-lg text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white flex items-center gap-1.5 shadow-xs transition-colors self-start sm:self-auto"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Register New Device</span>
        </button>
      </div>

      {feedback.text && (
        <div
          className={`p-3 rounded-xl text-xs flex items-center gap-2 ${
            feedback.type === 'success'
              ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
              : 'bg-rose-50 text-rose-800 border border-rose-200'
          }`}
        >
          {feedback.type === 'success' ? <CheckCircle2 className="w-4 h-4" /> : <AlertCircle className="w-4 h-4" />}
          <span>{feedback.text}</span>
        </div>
      )}

      {/* Devices Grid Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {devices.map((dev) => (
          <div
            key={dev.id}
            className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs flex flex-col justify-between hover:border-slate-300 transition-all"
          >
            <div>
              {/* Header */}
              <div className="flex items-start justify-between gap-2">
                <div className="flex items-center gap-2.5">
                  <div className={`p-2.5 rounded-xl border ${dev.online ? 'bg-emerald-50 text-emerald-600 border-emerald-100' : 'bg-rose-50 text-rose-600 border-rose-100'}`}>
                    <Cpu className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="font-bold text-slate-900 text-sm">{dev.name}</h3>
                    <span className="font-mono text-xs text-slate-500 font-semibold">{dev.deviceId}</span>
                  </div>
                </div>

                <div className="flex items-center gap-1">
                  <button
                    onClick={() => { setViewingDevice(dev); setDetailModalOpen(true); }}
                    className="p-1.5 rounded-md text-slate-400 hover:text-slate-700 hover:bg-slate-100"
                    title="View Details"
                  >
                    <Eye className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => handleOpenModal(dev)}
                    className="p-1.5 rounded-md text-slate-400 hover:text-slate-700 hover:bg-slate-100"
                    title="Edit Device"
                  >
                    <Edit2 className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => handleDeleteDevice(dev.id)}
                    className="p-1.5 rounded-md text-slate-400 hover:text-rose-600 hover:bg-rose-50"
                    title="Delete Device"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              {/* Status and Zone Badge Row */}
              <div className="flex items-center gap-2 mt-3 flex-wrap">
                <Badge variant={dev.online ? 'online' : 'offline'}>
                  {dev.online ? 'CONNECTED' : 'OFFLINE'}
                </Badge>
                <Badge variant="default">{dev.deviceType}</Badge>
                {dev.simulationEnabled && <Badge variant="simulation">SIMULATOR</Badge>}
              </div>

              {/* Location & Firmware Specs */}
              <div className="mt-4 pt-3 border-t border-slate-100 text-xs space-y-1.5 text-slate-600">
                <div className="flex items-center justify-between">
                  <span className="text-slate-400">Assigned Zone:</span>
                  <span className="font-medium text-slate-800">{dev.zoneName}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-400">Facility:</span>
                  <span className="font-medium text-slate-800">{dev.buildingName}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-400">Firmware:</span>
                  <span className="font-mono text-slate-500 text-[11px]">{dev.firmwareVersion}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-400">Protocol:</span>
                  <span className="font-mono text-slate-500 text-[11px]">{dev.protocol}</span>
                </div>
              </div>

              {/* Current Telemetry Snapshot */}
              {dev.currentPowerWatts != null && (
                <div className="mt-3 p-2.5 rounded-lg bg-slate-50 border border-slate-100 flex items-center justify-between text-xs font-mono">
                  <div>
                    <div className="text-[10px] text-slate-400 uppercase font-sans">Current Demand</div>
                    <div className="font-bold text-emerald-600">{dev.currentPowerWatts} W</div>
                  </div>
                  <div className="text-right">
                    <div className="text-[10px] text-slate-400 uppercase font-sans">Voltage / Current</div>
                    <div className="text-slate-700">{dev.currentVoltage}V / {dev.currentAmperes}A</div>
                  </div>
                </div>
              )}
            </div>

            {/* Footer with last heartbeat */}
            <div className="mt-4 pt-3 border-t border-slate-50 flex items-center justify-between text-[11px] text-slate-400">
              <span className="flex items-center gap-1">
                <Clock className="w-3 h-3" />
                Last seen:
              </span>
              <span className="font-mono">
                {dev.lastSeenAt ? new Date(dev.lastSeenAt).toLocaleTimeString() : 'Never'}
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Device Create/Edit Modal */}
      <Modal
        isOpen={deviceModalOpen}
        onClose={() => setDeviceModalOpen(false)}
        title={editingDevice ? 'Edit IoT Device Configuration' : 'Register New IoT Smart Meter'}
      >
        <form onSubmit={handleSaveDevice} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Unique Device ID (MAC / Serial)</label>
              <input
                type="text"
                required
                value={deviceForm.deviceId}
                onChange={(e) => setDeviceForm({ ...deviceForm, deviceId: e.target.value })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg font-mono focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
                placeholder="ESP32-001"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Device Friendly Name</label>
              <input
                type="text"
                required
                value={deviceForm.name}
                onChange={(e) => setDeviceForm({ ...deviceForm, name: e.target.value })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
                placeholder="e.g. Main Incomer Meter"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Hardware Sensor Type</label>
              <select
                value={deviceForm.deviceType}
                onChange={(e) => setDeviceForm({ ...deviceForm, deviceType: e.target.value })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              >
                <option value="ESP32_PZEM004T">ESP32 + PZEM-004T (V, I, P, PF, E)</option>
                <option value="ESP32_ACS712">ESP32 + ACS712 Current Sensor</option>
                <option value="SMART_METER">Digital RS485 / Modbus Smart Meter</option>
                <option value="SIMULATED_ENERGY_METER">Virtual Simulation Node</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Assigned Zone</label>
              <select
                required
                value={deviceForm.zoneId}
                onChange={(e) => setDeviceForm({ ...deviceForm, zoneId: parseInt(e.target.value) })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              >
                {zones.map((z) => (
                  <option key={z.id} value={z.id}>
                    {z.name} ({z.buildingName})
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Communication Protocol</label>
              <select
                value={deviceForm.protocol}
                onChange={(e) => setDeviceForm({ ...deviceForm, protocol: e.target.value })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              >
                <option value="MQTT">MQTT (Topic: smartbuilding/...)</option>
                <option value="HTTP_REST">HTTP REST Ingestion</option>
                <option value="WEBSOCKET">WebSocket Stream</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Firmware Version</label>
              <input
                type="text"
                value={deviceForm.firmwareVersion}
                onChange={(e) => setDeviceForm({ ...deviceForm, firmwareVersion: e.target.value })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg font-mono focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
                placeholder="v2.1.0-PROD"
              />
            </div>
          </div>

          <div className="flex items-center justify-between p-3 rounded-lg bg-slate-50 border border-slate-100">
            <div>
              <div className="text-xs font-medium text-slate-800">Enable In-Browser Telemetry Simulation</div>
              <div className="text-[10px] text-slate-400">Generate realistic load when physical ESP32 is unlinked</div>
            </div>
            <input
              type="checkbox"
              checked={deviceForm.simulationEnabled}
              onChange={(e) => setDeviceForm({ ...deviceForm, simulationEnabled: e.target.checked })}
              className="rounded text-emerald-600 focus:ring-emerald-500 h-4 w-4"
            />
          </div>

          <button
            type="submit"
            className="w-full py-2.5 px-4 rounded-xl text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 transition-colors"
          >
            {editingDevice ? 'Save Configuration' : 'Register Device'}
          </button>
        </form>
      </Modal>

      {/* Device Detail Modal */}
      <Modal
        isOpen={detailModalOpen}
        onClose={() => setDetailModalOpen(false)}
        title={viewingDevice ? `Device Diagnostics: ${viewingDevice.name}` : 'Device Details'}
      >
        {viewingDevice && (
          <div className="space-y-4 text-xs">
            <div className="p-3 rounded-xl bg-slate-50 border border-slate-100 space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-slate-500">Device ID:</span>
                <span className="font-mono font-bold text-slate-800">{viewingDevice.deviceId}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-slate-500">Status:</span>
                <Badge variant={viewingDevice.online ? 'online' : 'offline'}>
                  {viewingDevice.online ? 'ONLINE' : 'OFFLINE'}
                </Badge>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-slate-500">Zone / Building:</span>
                <span className="font-semibold text-slate-800">{viewingDevice.zoneName} ({viewingDevice.buildingName})</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-slate-500">MQTT Telemetry Topic:</span>
                <span className="font-mono text-emerald-700 text-[11px]">
                  smartbuilding/{viewingDevice.buildingId}/{viewingDevice.zoneId}/{viewingDevice.deviceId}/telemetry
                </span>
              </div>
            </div>

            <div className="border-t border-slate-100 pt-3">
              <h4 className="font-bold text-slate-900 mb-2">Current Telemetry Readings</h4>
              <div className="grid grid-cols-2 gap-2 text-slate-700">
                <div className="p-2 rounded bg-slate-50 font-mono">
                  Voltage: <span className="font-bold">{viewingDevice.currentVoltage || 0} V</span>
                </div>
                <div className="p-2 rounded bg-slate-50 font-mono">
                  Current: <span className="font-bold">{viewingDevice.currentAmperes || 0} A</span>
                </div>
                <div className="p-2 rounded bg-slate-50 font-mono text-emerald-700">
                  Power: <span className="font-bold">{viewingDevice.currentPowerWatts || 0} W</span>
                </div>
                <div className="p-2 rounded bg-slate-50 font-mono">
                  Total Energy: <span className="font-bold">{viewingDevice.totalEnergyKwh || 0} kWh</span>
                </div>
              </div>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default DeviceManagement;
