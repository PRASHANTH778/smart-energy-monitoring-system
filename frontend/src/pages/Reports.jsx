import React, { useState, useEffect } from 'react';
import {
  FileText,
  Download,
  Calendar,
  Filter,
  DollarSign,
  TrendingUp,
  AlertTriangle,
  RefreshCw,
  CheckCircle,
} from 'lucide-react';
import { energyApi } from '../api/energyApi';
import StatCard from '../components/common/StatCard';
import Badge from '../components/common/Badge';

const Reports = () => {
  const [reportType, setReportType] = useState('DAILY');
  const [startDate, setStartDate] = useState(
    new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  );
  const [endDate, setEndDate] = useState(new Date().toISOString().split('T')[0]);
  const [reportData, setReportData] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchReport = async () => {
    setLoading(true);
    try {
      const data = await energyApi.getReport(reportType, startDate, endDate);
      setReportData(data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReport();
  }, [reportType, startDate, endDate]);

  const handleDownloadCsv = () => {
    const url = energyApi.getCsvExportUrl(reportType, startDate, endDate);
    const token = localStorage.getItem('smart_energy_token');

    // Create anchor with auth token if needed or fetch blob
    fetch(url, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
      .then((res) => res.blob())
      .then((blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = `energy_report_${reportType.toLowerCase()}_${new Date().toISOString().split('T')[0]}.csv`;
        document.body.appendChild(a);
        a.click();
        a.remove();
      })
      .catch((err) => alert('Export failed: ' + err.message));
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">Executive Energy Audit Reports</h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Aggregated audit summaries, tariff billing statements, and engineering CSV data export.
          </p>
        </div>

        <button
          onClick={handleDownloadCsv}
          className="px-4 py-2 rounded-xl text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white flex items-center gap-2 shadow-xs transition-colors self-start sm:self-auto cursor-pointer"
        >
          <Download className="w-4 h-4" />
          <span>Export Official CSV</span>
        </button>
      </div>

      {/* Report Controls & Date Filter */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 flex flex-wrap items-center justify-between gap-4 text-xs shadow-2xs">
        <div className="flex items-center gap-3 flex-wrap">
          <div className="flex items-center gap-1.5">
            <span className="font-semibold text-slate-700">Audit Type:</span>
            <select
              value={reportType}
              onChange={(e) => setReportType(e.target.value)}
              className="px-3 py-1.5 rounded-lg border border-slate-200 bg-slate-50 text-slate-700 font-medium focus:outline-none"
            >
              <option value="DAILY">Daily Time Series Consumption</option>
              <option value="ZONE">Zone-wise Comparative Breakdown</option>
              <option value="DEVICE">Device-wise Meter Breakdown</option>
            </select>
          </div>

          <div className="flex items-center gap-2">
            <Calendar className="w-3.5 h-3.5 text-slate-400" />
            <input
              type="date"
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
              className="px-2.5 py-1.5 rounded-lg border border-slate-200 text-slate-700 text-xs"
            />
            <span className="text-slate-400">to</span>
            <input
              type="date"
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              className="px-2.5 py-1.5 rounded-lg border border-slate-200 text-slate-700 text-xs"
            />
          </div>
        </div>

        <button
          onClick={fetchReport}
          className="px-3 py-1.5 rounded-lg border border-slate-200 text-slate-600 hover:bg-slate-50 flex items-center gap-1.5"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Generate Statement</span>
        </button>
      </div>

      {/* KPI Audit Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Period Energy Consumed"
          value={reportData?.totalEnergyKwh?.toFixed(2) || '0.00'}
          unit="kWh"
          icon={TrendingUp}
          color="emerald"
          subtitle="Integrated total"
        />

        <StatCard
          title="Total Estimated Cost"
          value={`$${reportData?.totalEstimatedCost || '0.00'}`}
          unit="USD"
          icon={DollarSign}
          color="teal"
          subtitle={`Rate: $${reportData?.averageTariffRate || '0.1650'}/kWh`}
        />

        <StatCard
          title="Target Violations"
          value={reportData?.targetViolationsCount || 0}
          unit="events"
          icon={AlertTriangle}
          color="amber"
          badge={reportData?.targetViolationsCount > 0 ? <Badge variant="high">OVER BUDGET</Badge> : <Badge variant="success">IN TARGET</Badge>}
        />

        <StatCard
          title="Total Alert Exceptions"
          value={reportData?.totalAlertsCount || 0}
          unit="occurrences"
          icon={FileText}
          color="indigo"
          subtitle="Lifetime system logs"
        />
      </div>

      {/* Tabular Statement */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-xs overflow-hidden">
        <div className="p-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="font-semibold text-slate-800 text-xs">
            Statement Line Items ({reportData?.items?.length || 0} entries)
          </div>
          <div className="text-[11px] text-slate-500 font-mono">
            Tariff Rate Applied: ${reportData?.averageTariffRate} / kWh
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-100/70 text-slate-600 uppercase tracking-wider text-[10px] border-b border-slate-200">
              <tr>
                <th className="py-3 px-4">Period / Entity</th>
                <th className="py-3 px-4">Energy Consumed</th>
                <th className="py-3 px-4">Mean Power</th>
                <th className="py-3 px-4">Estimated Cost</th>
                <th className="py-3 px-4">Target Limit</th>
                <th className="py-3 px-4">Compliance Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {(reportData?.items || []).map((item, idx) => (
                <tr key={idx} className="hover:bg-slate-50/80 transition-colors">
                  <td className="py-3 px-4 font-semibold text-slate-800">{item.periodOrEntity}</td>
                  <td className="py-3 px-4 font-mono font-bold text-slate-900">{item.energyKwh} kWh</td>
                  <td className="py-3 px-4 font-mono text-slate-600">{item.avgPowerWatts} W</td>
                  <td className="py-3 px-4 font-mono font-semibold text-emerald-700">${item.cost}</td>
                  <td className="py-3 px-4 font-mono text-slate-500">{item.targetKwh} kWh</td>
                  <td className="py-3 px-4">
                    {item.status === 'TARGET_EXCEEDED' ? (
                      <Badge variant="high">LIMIT EXCEEDED</Badge>
                    ) : (
                      <Badge variant="success">WITHIN LIMITS</Badge>
                    )}
                  </td>
                </tr>
              ))}
              {(!reportData?.items || reportData.items.length === 0) && (
                <tr>
                  <td colSpan="6" className="py-12 text-center text-xs text-slate-400">
                    No records found for specified date range.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Reports;
