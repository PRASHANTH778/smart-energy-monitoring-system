import React, { useState, useEffect } from 'react';
import {
  Building2,
  Layers,
  Plus,
  Edit2,
  Trash2,
  CheckCircle2,
  AlertCircle,
  TrendingUp,
  MapPin,
  RefreshCw,
} from 'lucide-react';
import { energyApi } from '../api/energyApi';
import Modal from '../components/common/Modal';
import Badge from '../components/common/Badge';

const BuildingsZones = () => {
  const [buildings, setBuildings] = useState([]);
  const [zones, setZones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedBuildingId, setSelectedBuildingId] = useState(null);

  // Modals
  const [buildingModalOpen, setBuildingModalOpen] = useState(false);
  const [zoneModalOpen, setZoneModalOpen] = useState(false);
  const [editingBuilding, setEditingBuilding] = useState(null);
  const [editingZone, setEditingZone] = useState(null);

  // Form states
  const [buildingForm, setBuildingForm] = useState({
    name: '',
    description: '',
    location: '',
    buildingType: 'ACADEMIC',
    active: true,
  });

  const [zoneForm, setZoneForm] = useState({
    name: '',
    description: '',
    buildingId: '',
    dailyEnergyTargetKwh: 100.0,
    active: true,
  });

  const [feedbackMsg, setFeedbackMsg] = useState({ type: '', text: '' });

  const loadData = async () => {
    try {
      const [bData, zData] = await Promise.all([
        energyApi.getBuildings(),
        energyApi.getZones(),
      ]);
      setBuildings(bData || []);
      setZones(zData || []);
      if (bData && bData.length > 0 && !selectedBuildingId) {
        setSelectedBuildingId(bData[0].id);
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
    setFeedbackMsg({ type, text });
    setTimeout(() => setFeedbackMsg({ type: '', text: '' }), 3500);
  };

  // Building Actions
  const handleOpenBuildingModal = (b = null) => {
    if (b) {
      setEditingBuilding(b);
      setBuildingForm({
        name: b.name,
        description: b.description || '',
        location: b.location || '',
        buildingType: b.buildingType || 'ACADEMIC',
        active: b.active,
      });
    } else {
      setEditingBuilding(null);
      setBuildingForm({
        name: '',
        description: '',
        location: '',
        buildingType: 'ACADEMIC',
        active: true,
      });
    }
    setBuildingModalOpen(true);
  };

  const handleSaveBuilding = async (e) => {
    e.preventDefault();
    try {
      if (editingBuilding) {
        await energyApi.updateBuilding(editingBuilding.id, buildingForm);
        showFeedback('success', 'Building updated successfully');
      } else {
        await energyApi.createBuilding(buildingForm);
        showFeedback('success', 'New building created successfully');
      }
      setBuildingModalOpen(false);
      loadData();
    } catch (err) {
      showFeedback('error', err.response?.data?.message || err.message);
    }
  };

  const handleDeleteBuilding = async (id) => {
    if (!window.confirm('Are you sure you want to delete this building? Ensure no zones are assigned.')) return;
    try {
      await energyApi.deleteBuilding(id);
      showFeedback('success', 'Building deleted');
      loadData();
    } catch (err) {
      showFeedback('error', err.response?.data?.message || err.message);
    }
  };

  // Zone Actions
  const handleOpenZoneModal = (z = null) => {
    if (z) {
      setEditingZone(z);
      setZoneForm({
        name: z.name,
        description: z.description || '',
        buildingId: z.buildingId,
        dailyEnergyTargetKwh: z.dailyEnergyTargetKwh,
        active: z.active,
      });
    } else {
      setEditingZone(null);
      setZoneForm({
        name: '',
        description: '',
        buildingId: selectedBuildingId || (buildings[0]?.id || ''),
        dailyEnergyTargetKwh: 100.0,
        active: true,
      });
    }
    setZoneModalOpen(true);
  };

  const handleSaveZone = async (e) => {
    e.preventDefault();
    try {
      if (editingZone) {
        await energyApi.updateZone(editingZone.id, zoneForm);
        showFeedback('success', 'Zone updated successfully');
      } else {
        await energyApi.createZone(zoneForm);
        showFeedback('success', 'New zone added successfully');
      }
      setZoneModalOpen(false);
      loadData();
    } catch (err) {
      showFeedback('error', err.response?.data?.message || err.message);
    }
  };

  const handleDeleteZone = async (id) => {
    if (!window.confirm('Are you sure you want to delete this zone? Ensure no IoT devices are assigned.')) return;
    try {
      await energyApi.deleteZone(id);
      showFeedback('success', 'Zone deleted');
      loadData();
    } catch (err) {
      showFeedback('error', err.response?.data?.message || err.message);
    }
  };

  const filteredZones = selectedBuildingId
    ? zones.filter((z) => z.buildingId === selectedBuildingId)
    : zones;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-slate-200">
        <div>
          <h1 className="text-xl font-bold text-slate-900 tracking-tight">Facility Infrastructure Management</h1>
          <p className="text-xs text-slate-500 mt-0.5">
            Configure buildings, zones/rooms, sub-meter hierarchies, and daily energy consumption caps.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => handleOpenBuildingModal()}
            className="px-3 py-1.5 rounded-lg text-xs font-semibold bg-slate-900 text-white hover:bg-slate-800 flex items-center gap-1.5 transition-colors"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>New Building</span>
          </button>
          <button
            onClick={() => handleOpenZoneModal()}
            className="px-3 py-1.5 rounded-lg text-xs font-semibold bg-emerald-600 text-white hover:bg-emerald-700 flex items-center gap-1.5 transition-colors"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Add Zone / Room</span>
          </button>
        </div>
      </div>

      {feedbackMsg.text && (
        <div
          className={`p-3 rounded-xl text-xs flex items-center gap-2 ${
            feedbackMsg.type === 'success'
              ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
              : 'bg-rose-50 text-rose-800 border border-rose-200'
          }`}
        >
          {feedbackMsg.type === 'success' ? <CheckCircle2 className="w-4 h-4" /> : <AlertCircle className="w-4 h-4" />}
          <span>{feedbackMsg.text}</span>
        </div>
      )}

      {/* Building Tabs & Selector */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1">
        {buildings.map((b) => (
          <button
            key={b.id}
            onClick={() => setSelectedBuildingId(b.id)}
            className={`px-4 py-2.5 rounded-xl text-xs font-semibold border flex items-center gap-2 whitespace-nowrap transition-all ${
              selectedBuildingId === b.id
                ? 'bg-emerald-50 text-emerald-800 border-emerald-300 shadow-2xs'
                : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'
            }`}
          >
            <Building2 className="w-4 h-4 text-emerald-600" />
            <span>{b.name}</span>
            <span className="px-1.5 py-0.5 rounded-full bg-slate-100 text-[10px] text-slate-500 font-mono">
              {b.zoneCount} zones
            </span>
          </button>
        ))}
      </div>

      {/* Selected Building Details Card */}
      {selectedBuildingId && (
        <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
          {(() => {
            const b = buildings.find((x) => x.id === selectedBuildingId);
            if (!b) return null;
            return (
              <>
                <div className="space-y-1">
                  <div className="flex items-center gap-2.5">
                    <h2 className="text-base font-bold text-slate-900">{b.name}</h2>
                    <Badge variant="default">{b.buildingType}</Badge>
                    <Badge variant={b.active ? 'online' : 'offline'}>{b.active ? 'ACTIVE' : 'INACTIVE'}</Badge>
                  </div>
                  <p className="text-xs text-slate-500">{b.description || 'No description provided'}</p>
                  <div className="flex items-center gap-1.5 text-xs text-slate-400 pt-1">
                    <MapPin className="w-3.5 h-3.5" />
                    <span>{b.location || 'Location unspecified'}</span>
                  </div>
                </div>

                <div className="flex items-center gap-2 shrink-0">
                  <button
                    onClick={() => handleOpenBuildingModal(b)}
                    className="p-2 rounded-lg border border-slate-200 text-slate-600 hover:bg-slate-50 text-xs flex items-center gap-1.5"
                  >
                    <Edit2 className="w-3.5 h-3.5" /> Edit Building
                  </button>
                  <button
                    onClick={() => handleDeleteBuilding(b.id)}
                    className="p-2 rounded-lg border border-rose-200 text-rose-600 hover:bg-rose-50 text-xs flex items-center gap-1.5"
                  >
                    <Trash2 className="w-3.5 h-3.5" /> Delete
                  </button>
                </div>
              </>
            );
          })()}
        </div>
      )}

      {/* Zone Grid Cards */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-sm font-semibold text-slate-900">
            Zones & Monitoring Sub-meters in Selected Facility ({filteredZones.length})
          </h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {filteredZones.map((z) => {
            const consumed = z.currentConsumptionTodayKwh || 0;
            const target = z.dailyEnergyTargetKwh || 100.0;
            const pct = Math.min(100, Math.round((consumed / target) * 100));
            const isExceeded = consumed > target;

            return (
              <div key={z.id} className="bg-white rounded-xl border border-slate-200 p-5 shadow-xs flex flex-col justify-between hover:border-slate-300 transition-all">
                <div>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <div className="p-2 rounded-lg bg-emerald-50 text-emerald-600">
                        <Layers className="w-4 h-4" />
                      </div>
                      <h3 className="font-bold text-slate-900 text-sm">{z.name}</h3>
                    </div>
                    <div className="flex items-center gap-1">
                      <button
                        onClick={() => handleOpenZoneModal(z)}
                        className="p-1.5 rounded-md text-slate-400 hover:text-slate-700 hover:bg-slate-100"
                        title="Edit Zone"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => handleDeleteZone(z.id)}
                        className="p-1.5 rounded-md text-slate-400 hover:text-rose-600 hover:bg-rose-50"
                        title="Delete Zone"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>

                  <p className="text-xs text-slate-500 mt-2 line-clamp-2">{z.description || 'General zone'}</p>

                  <div className="mt-4 pt-3 border-t border-slate-100 space-y-2">
                    <div className="flex items-center justify-between text-xs">
                      <span className="text-slate-500">Today's Consumption:</span>
                      <span className="font-bold text-slate-900 font-mono">{consumed} kWh</span>
                    </div>
                    <div className="flex items-center justify-between text-xs">
                      <span className="text-slate-500">Configured Daily Target:</span>
                      <span className="font-semibold text-slate-700 font-mono">{target} kWh</span>
                    </div>

                    {/* Target Progress Bar */}
                    <div className="mt-2">
                      <div className="flex justify-between text-[11px] mb-1">
                        <span className="text-slate-400">Budget Consumed</span>
                        <span className={`font-mono font-semibold ${isExceeded ? 'text-rose-600 font-bold' : 'text-slate-700'}`}>
                          {pct}%
                        </span>
                      </div>
                      <div className="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                        <div
                          className={`h-2 rounded-full transition-all duration-500 ${
                            isExceeded ? 'bg-rose-500' : pct > 80 ? 'bg-amber-500' : 'bg-emerald-500'
                          }`}
                          style={{ width: `${pct}%` }}
                        />
                      </div>
                    </div>
                  </div>
                </div>

                <div className="mt-4 pt-3 border-t border-slate-50 flex items-center justify-between text-xs text-slate-500">
                  <span>Assigned Devices: {z.deviceCount}</span>
                  {isExceeded ? (
                    <Badge variant="high">CAP EXCEEDED</Badge>
                  ) : (
                    <Badge variant="success">IN BUDGET</Badge>
                  )}
                </div>
              </div>
            );
          })}

          {filteredZones.length === 0 && (
            <div className="col-span-full py-12 text-center text-xs text-slate-400 bg-white rounded-xl border border-dashed border-slate-300">
              No zones registered for this building yet. Click "Add Zone / Room" to create one.
            </div>
          )}
        </div>
      </div>

      {/* Building Create/Edit Modal */}
      <Modal
        isOpen={buildingModalOpen}
        onClose={() => setBuildingModalOpen(false)}
        title={editingBuilding ? 'Edit Building Facility' : 'Create New Building Facility'}
      >
        <form onSubmit={handleSaveBuilding} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Building Name</label>
            <input
              type="text"
              required
              value={buildingForm.name}
              onChange={(e) => setBuildingForm({ ...buildingForm, name: e.target.value })}
              className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              placeholder="e.g. Science & Technology Block"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Building Type</label>
            <select
              value={buildingForm.buildingType}
              onChange={(e) => setBuildingForm({ ...buildingForm, buildingType: e.target.value })}
              className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
            >
              <option value="ACADEMIC">Academic / Engineering</option>
              <option value="COMMERCIAL">Commercial Office</option>
              <option value="INDUSTRIAL">Industrial / Plant</option>
              <option value="RESIDENTIAL">Residential / Dormitory</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Physical Location</label>
            <input
              type="text"
              value={buildingForm.location}
              onChange={(e) => setBuildingForm({ ...buildingForm, location: e.target.value })}
              className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              placeholder="e.g. North Campus, Sector 4"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Description</label>
            <textarea
              rows="3"
              value={buildingForm.description}
              onChange={(e) => setBuildingForm({ ...buildingForm, description: e.target.value })}
              className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              placeholder="Facility description and purpose..."
            />
          </div>

          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="bActive"
              checked={buildingForm.active}
              onChange={(e) => setBuildingForm({ ...buildingForm, active: e.target.checked })}
              className="rounded text-emerald-600 focus:ring-emerald-500"
            />
            <label htmlFor="bActive" className="text-xs text-slate-700 font-medium">Facility is Active</label>
          </div>

          <button
            type="submit"
            className="w-full py-2 px-4 rounded-xl text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 transition-colors"
          >
            {editingBuilding ? 'Update Building' : 'Create Building'}
          </button>
        </form>
      </Modal>

      {/* Zone Create/Edit Modal */}
      <Modal
        isOpen={zoneModalOpen}
        onClose={() => setZoneModalOpen(false)}
        title={editingZone ? 'Edit Zone / Room' : 'Add New Zone / Room'}
      >
        <form onSubmit={handleSaveZone} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Zone / Room Name</label>
            <input
              type="text"
              required
              value={zoneForm.name}
              onChange={(e) => setZoneForm({ ...zoneForm, name: e.target.value })}
              className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              placeholder="e.g. Central Chiller Plant Room"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Parent Building</label>
            <select
              required
              value={zoneForm.buildingId}
              onChange={(e) => setZoneForm({ ...zoneForm, buildingId: parseInt(e.target.value) })}
              className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
            >
              {buildings.map((b) => (
                <option key={b.id} value={b.id}>{b.name}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Daily Energy Consumption Target (kWh)</label>
            <input
              type="number"
              step="1"
              required
              value={zoneForm.dailyEnergyTargetKwh}
              onChange={(e) => setZoneForm({ ...zoneForm, dailyEnergyTargetKwh: parseFloat(e.target.value) })}
              className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              placeholder="100.0"
            />
            <span className="text-[10px] text-slate-400">Automated alerts trigger when zone consumption exceeds this target.</span>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-700 mb-1">Description</label>
            <textarea
              rows="3"
              value={zoneForm.description}
              onChange={(e) => setZoneForm({ ...zoneForm, description: e.target.value })}
              className="w-full px-3 py-2 text-xs border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/50"
              placeholder="Description of appliances and circuits in this zone..."
            />
          </div>

          <button
            type="submit"
            className="w-full py-2 px-4 rounded-xl text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 transition-colors"
          >
            {editingZone ? 'Update Zone' : 'Add Zone'}
          </button>
        </form>
      </Modal>
    </div>
  );
};

export default BuildingsZones;
