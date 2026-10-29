import React, { useState, useEffect } from 'react';
import {
  Settings as SettingsIcon,
  DollarSign,
  Radio,
  Cpu,
  CheckCircle2,
  AlertCircle,
  ToggleLeft,
  ToggleRight,
  Save,
  Server,
  Layers,
} from 'lucide-react';
import { energyApi } from '../api/energyApi';
import Badge from '../components/common/Badge';

const Settings = () => {
  const [settings, setSettings] = useState(null);
  const [activeTariff, setActiveTariff] = useState(null);
  const [simulationEnabled, setSimulationEnabled] = useState(true);
  const [loading, setLoading] = useState(true);

  // Tariff form
  const [tariffForm, setTariffForm] = useState({
    name: '',
    ratePerKwh: 0.1650,
    effectiveFrom: '',
    baselineTargetKwh: 600.0,
    currency: '$',
  });

  const [feedback, setFeedback] = useState({ type: '', text: '' });

  const loadData = async () => {
    try {
      const [sData, tData] = await Promise.all([
        energyApi.getSettings(),
        energyApi.getActiveTariff(),
      ]);
      setSettings(sData);
      setSimulationEnabled(sData.simulationEnabled);
      setActiveTariff(tData);
      if (tData) {
        setTariffForm({
          name: tData.name,
          ratePerKwh: tData.ratePerKwh,
          effectiveFrom: tData.effectiveFrom,
          baselineTargetKwh: tData.baselineTargetKwh,
          currency: tData.currency || '$',
        });
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const showFeedback = (type, text) => {
    setFeedback({ type, text });
    setTimeout(() => setFeedback({ type: '', text: '' }), 3500);
  };

  const handleToggleSimulation = async () => {
    try {
      const nextState = !simulationEnabled;
      const res = await energyApi.toggleSimulation(nextState);
      setSimulationEnabled(res.simulationEnabled);
      showFeedback('success', res.message);
    } catch (err) {
      showFeedback('error', err.response?.data?.message || err.message);
    }
  };

  const handleSaveTariff = async (e) => {
    e.preventDefault();
    try {
      if (activeTariff) {
        await energyApi.updateTariff(activeTariff.id, {
          ...activeTariff,
          ...tariffForm,
        });
      } else {
        await energyApi.createTariff({
          ...tariffForm,
          active: true,
        });
      }
      showFeedback('success', 'Tariff configuration updated successfully');
      loadData();
    } catch (err) {
      showFeedback('error', err.response?.data?.message || err.message);
    }
  };

  return (
    <div className="space-y-6 max-w-5xl">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">System Configuration & Parameters</h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Configure utility tariff schedules, MQTT broker connectivity, and edge simulation engines.
          </p>
        </div>
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

      {/* Simulator Control Card */}
      <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
        <div className="flex items-start justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className={`p-2.5 rounded-xl border ${simulationEnabled ? 'bg-amber-50 text-amber-600 border-amber-200' : 'bg-slate-100 text-slate-400 border-slate-200'}`}>
              <Radio className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-sm font-bold text-slate-900">Virtual IoT Simulation Engine</h2>
                {simulationEnabled ? (
                  <Badge variant="simulation">ACTIVE DEMO MODE</Badge>
                ) : (
                  <Badge variant="default">DISABLED</Badge>
                )}
              </div>
              <p className="text-xs text-slate-500 mt-1 max-w-2xl">
                When enabled, the backend autonomously generates realistic fluctuating electrical waveforms for testing load shedding, peak alarms, and dashboard trends without requiring physical microcontrollers or external brokers.
              </p>
            </div>
          </div>

          <button
            onClick={handleToggleSimulation}
            className="text-slate-400 hover:text-emerald-600 transition-colors shrink-0"
            title={simulationEnabled ? 'Disable Simulation' : 'Enable Simulation'}
          >
            {simulationEnabled ? (
              <ToggleRight className="w-9 h-9 text-amber-500" />
            ) : (
              <ToggleLeft className="w-9 h-9 text-slate-300" />
            )}
          </button>
        </div>
      </div>

      {/* Electricity Tariff Configuration Card */}
      <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
        <div className="flex items-center gap-2.5 mb-4">
          <div className="p-2 rounded-lg bg-emerald-50 text-emerald-600">
            <DollarSign className="w-4 h-4" />
          </div>
          <div>
            <h2 className="text-sm font-bold text-slate-900">Configurable Electricity Tariff & Baseline</h2>
            <p className="text-xs text-slate-500">
              Formulas: Estimated Cost = Energy (kWh) &times; Tariff per kWh. BigDecimals are strictly used for financial accuracy.
            </p>
          </div>
        </div>

        <form onSubmit={handleSaveTariff} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Tariff Schedule Name</label>
              <input
                type="text"
                required
                value={tariffForm.name}
                onChange={(e) => setTariffForm({ ...tariffForm, name: e.target.value })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
                placeholder="e.g. Commercial Time-of-Use"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Rate per Kilowatt-Hour ($/kWh)</label>
              <input
                type="number"
                step="0.0001"
                min="0.0001"
                required
                value={tariffForm.ratePerKwh}
                onChange={(e) => setTariffForm({ ...tariffForm, ratePerKwh: parseFloat(e.target.value) })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg font-mono focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Effective Date</label>
              <input
                type="date"
                required
                value={tariffForm.effectiveFrom}
                onChange={(e) => setTariffForm({ ...tariffForm, effectiveFrom: e.target.value })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Monthly Baseline Target (kWh)</label>
              <input
                type="number"
                step="1"
                required
                value={tariffForm.baselineTargetKwh}
                onChange={(e) => setTariffForm({ ...tariffForm, baselineTargetKwh: parseFloat(e.target.value) })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg font-mono focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>
          </div>

          <button
            type="submit"
            className="py-2 px-4 rounded-xl text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 flex items-center gap-1.5 transition-colors cursor-pointer"
          >
            <Save className="w-3.5 h-3.5" />
            <span>Save Tariff Rate</span>
          </button>
        </form>
      </div>

      {/* MQTT Broker Settings Card */}
      <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
        <div className="flex items-center gap-2.5 mb-4">
          <div className="p-2 rounded-lg bg-indigo-50 text-indigo-600">
            <Server className="w-4 h-4" />
          </div>
          <div>
            <h2 className="text-sm font-bold text-slate-900">MQTT Broker Connectivity</h2>
            <p className="text-xs text-slate-500">External message broker settings for physical ESP32 nodes</p>
          </div>
        </div>

        <div className="space-y-3 text-xs">
          <div className="p-3 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-between">
            <span className="text-slate-600">MQTT Broker Status:</span>
            <Badge variant={settings?.mqttConnected ? 'online' : 'default'}>
              {settings?.mqttConnected ? 'CONNECTED' : 'STANDBY (FALLBACK SIMULATOR ACTIVE)'}
            </Badge>
          </div>
          <div className="p-3 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-between">
            <span className="text-slate-600">Broker URL:</span>
            <span className="font-mono text-slate-800">{settings?.mqttBrokerUrl || 'tcp://localhost:1883'}</span>
          </div>
          <div className="p-3 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-between">
            <span className="text-slate-600">Topic Filter:</span>
            <span className="font-mono text-emerald-700">{settings?.mqttTopicPattern || 'smartbuilding/#'}</span>
          </div>
          <div className="p-3 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-between">
            <span className="text-slate-600">Node Heartbeat Timeout:</span>
            <span className="font-mono text-slate-800">{settings?.offlineThresholdSeconds || 60} seconds</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Settings;
