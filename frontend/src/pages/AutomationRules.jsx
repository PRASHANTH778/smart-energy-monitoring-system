import React, { useState, useEffect } from 'react';
import {
  Zap,
  Plus,
  Edit2,
  Trash2,
  CheckCircle2,
  AlertCircle,
  ToggleLeft,
  ToggleRight,
  Shield,
  Activity,
  Layers,
  Clock,
} from 'lucide-react';
import { energyApi } from '../api/energyApi';
import Modal from '../components/common/Modal';
import Badge from '../components/common/Badge';

const AutomationRules = () => {
  const [rules, setRules] = useState([]);
  const [executions, setExecutions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [ruleModalOpen, setRuleModalOpen] = useState(false);
  const [editingRule, setEditingRule] = useState(null);

  const [ruleForm, setRuleForm] = useState({
    name: '',
    description: '',
    targetType: 'ALL',
    targetName: 'Entire Facility',
    conditionType: 'POWER_THRESHOLD_EXCEEDED',
    thresholdValue: 3000.0,
    cooldownMinutes: 15,
    actionType: 'SIMULATED_LOAD_SHEDDING',
    enabled: true,
  });

  const [feedback, setFeedback] = useState({ type: '', text: '' });

  const loadData = async () => {
    try {
      const [rData, eData] = await Promise.all([
        energyApi.getAutomationRules(),
        energyApi.getAutomationExecutions(),
      ]);
      setRules(rData || []);
      setExecutions(eData || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 10000);
    return () => clearInterval(interval);
  }, []);

  const showFeedback = (type, text) => {
    setFeedback({ type, text });
    setTimeout(() => setFeedback({ type: '', text: '' }), 3500);
  };

  const handleOpenModal = (r = null) => {
    if (r) {
      setEditingRule(r);
      setRuleForm({
        name: r.name,
        description: r.description || '',
        targetType: r.targetType || 'ALL',
        targetName: r.targetName || 'Entire Facility',
        conditionType: r.conditionType,
        thresholdValue: r.thresholdValue,
        cooldownMinutes: r.cooldownMinutes || 15,
        actionType: r.actionType || 'SIMULATED_LOAD_SHEDDING',
        enabled: r.enabled,
      });
    } else {
      setEditingRule(null);
      setRuleForm({
        name: '',
        description: '',
        targetType: 'ALL',
        targetName: 'Entire Facility',
        conditionType: 'POWER_THRESHOLD_EXCEEDED',
        thresholdValue: 3000.0,
        cooldownMinutes: 15,
        actionType: 'SIMULATED_LOAD_SHEDDING',
        enabled: true,
      });
    }
    setRuleModalOpen(true);
  };

  const handleSaveRule = async (e) => {
    e.preventDefault();
    try {
      if (editingRule) {
        await energyApi.updateAutomationRule(editingRule.id, ruleForm);
        showFeedback('success', 'Automation rule updated');
      } else {
        await energyApi.createAutomationRule(ruleForm);
        showFeedback('success', 'Automation rule registered');
      }
      setRuleModalOpen(false);
      loadData();
    } catch (err) {
      showFeedback('error', err.response?.data?.message || err.message);
    }
  };

  const handleToggleRule = async (rule) => {
    try {
      await energyApi.updateAutomationRule(rule.id, {
        ...rule,
        enabled: !rule.enabled,
      });
      loadData();
    } catch (err) {
      showFeedback('error', err.response?.data?.message || err.message);
    }
  };

  const handleDeleteRule = async (id) => {
    if (!window.confirm('Delete this automation rule?')) return;
    try {
      await energyApi.deleteAutomationRule(id);
      showFeedback('success', 'Rule deleted');
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
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">Automated Energy Rules Engine</h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Rule-based demand limiting, peak curtailment, automated setpoint policies, and fail-safe safety isolation.
          </p>
        </div>

        <button
          onClick={() => handleOpenModal()}
          className="px-3 py-1.5 rounded-lg text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white flex items-center gap-1.5 shadow-xs transition-colors self-start sm:self-auto"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>Add Automation Rule</span>
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

      {/* Rules Grid Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {rules.map((rule) => (
          <div
            key={rule.id}
            className={`bg-white rounded-xl border p-5 shadow-xs flex flex-col justify-between transition-all ${
              rule.enabled ? 'border-slate-200' : 'border-slate-200 bg-slate-50/50 opacity-70'
            }`}
          >
            <div>
              <div className="flex items-start justify-between gap-2">
                <div className="flex items-center gap-2.5">
                  <div className={`p-2 rounded-lg ${rule.enabled ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-100 text-slate-400'}`}>
                    <Zap className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="font-bold text-slate-900 text-sm">{rule.name}</h3>
                    <span className="text-[11px] text-slate-400 font-medium">Target: {rule.targetName || 'Facility Wide'}</span>
                  </div>
                </div>

                <div className="flex items-center gap-2">
                  <button
                    onClick={() => handleToggleRule(rule)}
                    className="text-slate-400 hover:text-emerald-600 transition-colors"
                    title={rule.enabled ? 'Disable Rule' : 'Enable Rule'}
                  >
                    {rule.enabled ? (
                      <ToggleRight className="w-7 h-7 text-emerald-600" />
                    ) : (
                      <ToggleLeft className="w-7 h-7 text-slate-400" />
                    )}
                  </button>
                  <button
                    onClick={() => handleOpenModal(rule)}
                    className="p-1 rounded text-slate-400 hover:text-slate-700"
                  >
                    <Edit2 className="w-3.5 h-3.5" />
                  </button>
                  <button
                    onClick={() => handleDeleteRule(rule.id)}
                    className="p-1 rounded text-slate-400 hover:text-rose-600"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              <p className="text-xs text-slate-600 mt-2.5">{rule.description}</p>

              <div className="mt-4 pt-3 border-t border-slate-100 space-y-1.5 text-xs">
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Condition:</span>
                  <Badge variant="default" className="font-mono text-[10px]">{rule.conditionType}</Badge>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Threshold:</span>
                  <span className="font-mono font-bold text-slate-800">{rule.thresholdValue}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Simulated Action:</span>
                  <span className="font-semibold text-emerald-700">{rule.actionType}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-slate-500">Cooldown Window:</span>
                  <span className="text-slate-700">{rule.cooldownMinutes} minutes</span>
                </div>
              </div>
            </div>

            <div className="mt-4 pt-3 border-t border-slate-50 flex items-center justify-between text-[11px] text-slate-400">
              <span>Evaluated: {rule.lastEvaluatedAt ? new Date(rule.lastEvaluatedAt).toLocaleTimeString() : 'Pending'}</span>
              {rule.lastTriggeredAt && (
                <span className="text-amber-600 font-medium">
                  Last fired: {new Date(rule.lastTriggeredAt).toLocaleTimeString()}
                </span>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Execution Audit Log Section */}
      <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h2 className="text-sm font-semibold text-slate-900">Automation Trigger Audit Log</h2>
            <p className="text-xs text-slate-500">Historical records of simulated automated actions and curtailments</p>
          </div>
          <Badge variant="info">DEMO SAFETY ISOLATED</Badge>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 text-slate-500 uppercase tracking-wider text-[10px] border-y border-slate-100">
              <tr>
                <th className="py-2.5 px-3">Rule</th>
                <th className="py-2.5 px-3">Trigger Reason</th>
                <th className="py-2.5 px-3">Action Executed</th>
                <th className="py-2.5 px-3">Status</th>
                <th className="py-2.5 px-3">Timestamp</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {executions.map((e) => (
                <tr key={e.id} className="hover:bg-slate-50">
                  <td className="py-2.5 px-3 font-semibold text-slate-800">{e.ruleName}</td>
                  <td className="py-2.5 px-3 text-slate-600">{e.triggerReason}</td>
                  <td className="py-2.5 px-3 text-slate-700">{e.actionTaken}</td>
                  <td className="py-2.5 px-3">
                    <Badge variant="success" className="text-[10px] font-mono">{e.status}</Badge>
                  </td>
                  <td className="py-2.5 px-3 text-slate-500 text-[11px] font-mono">
                    {new Date(e.executedAt).toLocaleString()}
                  </td>
                </tr>
              ))}
              {executions.length === 0 && (
                <tr>
                  <td colSpan="5" className="py-8 text-center text-xs text-slate-400">
                    No automated actions triggered yet. System operating within all threshold bounds.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Rule Create/Edit Modal */}
      <Modal
        isOpen={ruleModalOpen}
        onClose={() => setRuleModalOpen(false)}
        title={editingRule ? 'Edit Automation Rule' : 'Create Automation Rule'}
      >
        <form onSubmit={handleSaveRule} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Rule Name</label>
            <input
              type="text"
              required
              value={ruleForm.name}
              onChange={(e) => setRuleForm({ ...ruleForm, name: e.target.value })}
              className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              placeholder="e.g. Server Room Thermal / Power Safeguard"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Condition Type</label>
            <select
              value={ruleForm.conditionType}
              onChange={(e) => setRuleForm({ ...ruleForm, conditionType: e.target.value })}
              className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
            >
              <option value="POWER_THRESHOLD_EXCEEDED">Instant Power Spike (Watts &gt; Threshold)</option>
              <option value="DAILY_TARGET_EXCEEDED">Daily Energy Budget Exceeded (kWh &gt; Target)</option>
              <option value="DEVICE_OFFLINE">Node Offline Count (&gt;= Threshold)</option>
              <option value="ABNORMAL_CONSUMPTION">Abnormal 10-Minute Average Surge</option>
            </select>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Threshold Value</label>
              <input
                type="number"
                step="0.1"
                required
                value={ruleForm.thresholdValue}
                onChange={(e) => setRuleForm({ ...ruleForm, thresholdValue: parseFloat(e.target.value) })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-700 mb-1">Cooldown (Minutes)</label>
              <input
                type="number"
                min="1"
                required
                value={ruleForm.cooldownMinutes}
                onChange={(e) => setRuleForm({ ...ruleForm, cooldownMinutes: parseInt(e.target.value) })}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Automated Action (Simulated Safety Protocol)</label>
            <select
              value={ruleForm.actionType}
              onChange={(e) => setRuleForm({ ...ruleForm, actionType: e.target.value })}
              className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
            >
              <option value="SIMULATED_LOAD_SHEDDING">Simulated Load Shedding (Flag non-essential circuits)</option>
              <option value="SET_ECO_MODE_SIMULATED">Apply HVAC Eco-Setback Profile (+1.5 C offset)</option>
              <option value="AUTO_DIM_LIGHTING_SIMULATED">Simulated Lighting Dimming Command (Dim 40%)</option>
              <option value="ALERT_ONLY">Dispatch High-Priority Alert Notice Only</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Rule Description</label>
            <textarea
              rows="3"
              value={ruleForm.description}
              onChange={(e) => setRuleForm({ ...ruleForm, description: e.target.value })}
              className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              placeholder="Explain rationale for this automation..."
            />
          </div>

          <button
            type="submit"
            className="w-full py-2.5 px-4 rounded-xl text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 transition-colors"
          >
            {editingRule ? 'Save Rule Configuration' : 'Create Automation Rule'}
          </button>
        </form>
      </Modal>
    </div>
  );
};

export default AutomationRules;
