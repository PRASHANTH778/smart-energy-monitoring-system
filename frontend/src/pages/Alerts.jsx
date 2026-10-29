import React, { useState, useEffect } from 'react';
import {
  Bell,
  CheckCircle,
  AlertTriangle,
  ShieldAlert,
  Clock,
  Filter,
  CheckCheck,
  Building2,
  Cpu,
  RefreshCw,
} from 'lucide-react';
import { energyApi } from '../api/energyApi';
import Badge from '../components/common/Badge';

const Alerts = () => {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [severityFilter, setSeverityFilter] = useState('');
  const [resolvedFilter, setResolvedFilter] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const fetchAlerts = async (sev = severityFilter, res = resolvedFilter, pg = page) => {
    setLoading(true);
    try {
      const isRes = res === '' ? null : res === 'true';
      const data = await energyApi.getAlerts(sev, isRes, pg, 15);
      setAlerts(data.content || []);
      setTotalPages(data.totalPages || 1);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAlerts(severityFilter, resolvedFilter, page);
  }, [severityFilter, resolvedFilter, page]);

  const handleAction = async (id, action) => {
    try {
      await energyApi.processAlertAction(id, action, 'Processed from web console');
      fetchAlerts(severityFilter, resolvedFilter, page);
    } catch (err) {
      alert('Error: ' + (err.response?.data?.message || err.message));
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">Facility Alerts & Anomaly Feed</h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Operational exceptions, automated load threshold triggers, power factor violations, and offline node events.
          </p>
        </div>

        <button
          onClick={() => fetchAlerts(severityFilter, resolvedFilter, page)}
          className="p-1.5 rounded-lg border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 self-start sm:self-auto"
          title="Refresh Feed"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {/* Filter Bar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 flex flex-wrap items-center justify-between gap-3 text-xs shadow-2xs">
        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-slate-400" />
          <span className="font-semibold text-slate-700">Filter Alerts:</span>
        </div>

        <div className="flex items-center gap-3 flex-wrap">
          <div className="flex items-center gap-1.5">
            <span className="text-slate-500">Severity:</span>
            <select
              value={severityFilter}
              onChange={(e) => { setSeverityFilter(e.target.value); setPage(0); }}
              className="px-2.5 py-1.5 rounded-lg border border-slate-200 bg-slate-50 text-slate-700 focus:outline-none"
            >
              <option value="">All Severities</option>
              <option value="CRITICAL">Critical</option>
              <option value="HIGH">High</option>
              <option value="MEDIUM">Medium</option>
              <option value="LOW">Low</option>
            </select>
          </div>

          <div className="flex items-center gap-1.5">
            <span className="text-slate-500">Status:</span>
            <select
              value={resolvedFilter}
              onChange={(e) => { setResolvedFilter(e.target.value); setPage(0); }}
              className="px-2.5 py-1.5 rounded-lg border border-slate-200 bg-slate-50 text-slate-700 focus:outline-none"
            >
              <option value="">All Events</option>
              <option value="false">Unresolved Only</option>
              <option value="true">Resolved History</option>
            </select>
          </div>
        </div>
      </div>

      {/* Alerts Stream List */}
      <div className="space-y-3">
        {alerts.map((alert) => (
          <div
            key={alert.id}
            className={`p-4 rounded-xl border transition-all ${
              alert.resolved
                ? 'bg-slate-50/70 border-slate-200 opacity-75'
                : alert.severity === 'CRITICAL' || alert.severity === 'HIGH'
                ? 'bg-rose-50/30 border-rose-200 shadow-xs'
                : 'bg-white border-slate-200 shadow-xs'
            }`}
          >
            <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-3">
              <div className="space-y-1.5 flex-1">
                <div className="flex items-center gap-2 flex-wrap">
                  <Badge variant={alert.severity.toLowerCase()}>{alert.severity}</Badge>
                  <span className="text-xs font-bold text-slate-900">{alert.title}</span>
                  <Badge variant="default" className="text-[10px] font-mono">{alert.alertType}</Badge>
                </div>

                <p className="text-xs text-slate-600">{alert.description}</p>

                <div className="flex items-center gap-4 text-[11px] text-slate-400 pt-1 flex-wrap">
                  <span className="flex items-center gap-1">
                    <Clock className="w-3 h-3" />
                    {new Date(alert.createdAt).toLocaleString()}
                  </span>
                  {alert.zoneName && (
                    <span className="flex items-center gap-1 text-slate-600">
                      <Building2 className="w-3 h-3 text-slate-400" />
                      {alert.zoneName}
                    </span>
                  )}
                  {alert.deviceName && (
                    <span className="flex items-center gap-1 font-mono text-slate-600">
                      <Cpu className="w-3 h-3 text-slate-400" />
                      {alert.deviceName} ({alert.deviceIdentifier})
                    </span>
                  )}
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex items-center gap-2 self-end sm:self-center shrink-0">
                {!alert.acknowledged && !alert.resolved && (
                  <button
                    onClick={() => handleAction(alert.id, 'ACKNOWLEDGE')}
                    className="px-2.5 py-1 rounded-lg border border-slate-300 text-slate-700 hover:bg-slate-100 text-xs font-medium flex items-center gap-1 transition-colors"
                  >
                    <CheckCircle className="w-3.5 h-3.5 text-slate-500" /> Acknowledge
                  </button>
                )}

                {!alert.resolved ? (
                  <button
                    onClick={() => handleAction(alert.id, 'RESOLVE')}
                    className="px-2.5 py-1 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold flex items-center gap-1 transition-colors"
                  >
                    <CheckCheck className="w-3.5 h-3.5" /> Resolve Event
                  </button>
                ) : (
                  <div className="text-[11px] text-emerald-700 font-semibold flex items-center gap-1 bg-emerald-50 px-2 py-1 rounded border border-emerald-200">
                    <CheckCheck className="w-3.5 h-3.5 text-emerald-600" /> Resolved by {alert.resolvedBy || 'Operator'}
                  </div>
                )}
              </div>
            </div>
          </div>
        ))}

        {alerts.length === 0 && (
          <div className="py-16 text-center text-xs text-slate-400 bg-white rounded-xl border border-dashed border-slate-300">
            No alerts found matching current filter criteria.
          </div>
        )}
      </div>
    </div>
  );
};

export default Alerts;
