import axiosClient from './axiosClient';

export const energyService = {
  // Dashboard & Analytics
  getDashboardSummary: async () => {
    const res = await axiosClient.get('/dashboard/summary');
    return res.data;
  },
  getAnalytics: async (range = 'today') => {
    const res = await axiosClient.get(`/analytics?range=${range}`);
    return res.data;
  },

  // Sensor Readings
  getReadings: async (page = 0, size = 20, sortBy = 'timestamp', direction = 'desc') => {
    const res = await axiosClient.get(`/readings?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`);
    return res.data;
  },
  getRecentReadings: async () => {
    const res = await axiosClient.get('/readings/recent');
    return res.data;
  },
  ingestReading: async (payload) => {
    const res = await axiosClient.post('/readings/ingest', payload);
    return res.data;
  },

  // Buildings
  getBuildings: async () => {
    const res = await axiosClient.get('/buildings');
    return res.data;
  },
  getBuilding: async (id) => {
    const res = await axiosClient.get(`/buildings/${id}`);
    return res.data;
  },
  createBuilding: async (data) => {
    const res = await axiosClient.post('/buildings', data);
    return res.data;
  },
  updateBuilding: async (id, data) => {
    const res = await axiosClient.put(`/buildings/${id}`, data);
    return res.data;
  },
  deleteBuilding: async (id) => {
    const res = await axiosClient.delete(`/buildings/${id}`);
    return res.data;
  },

  // Zones
  getZones: async (buildingId = null) => {
    const url = buildingId ? `/zones?buildingId=${buildingId}` : '/zones';
    const res = await axiosClient.get(url);
    return res.data;
  },
  getZone: async (id) => {
    const res = await axiosClient.get(`/zones/${id}`);
    return res.data;
  },
  createZone: async (data) => {
    const res = await axiosClient.post('/zones', data);
    return res.data;
  },
  updateZone: async (id, data) => {
    const res = await axiosClient.put(`/zones/${id}`, data);
    return res.data;
  },
  deleteZone: async (id) => {
    const res = await axiosClient.delete(`/zones/${id}`);
    return res.data;
  },

  // IoT Devices
  getDevices: async (zoneId = null) => {
    const url = zoneId ? `/devices?zoneId=${zoneId}` : '/devices';
    const res = await axiosClient.get(url);
    return res.data;
  },
  getDevice: async (id) => {
    const res = await axiosClient.get(`/devices/${id}`);
    return res.data;
  },
  createDevice: async (data) => {
    const res = await axiosClient.post('/devices', data);
    return res.data;
  },
  updateDevice: async (id, data) => {
    const res = await axiosClient.put(`/devices/${id}`, data);
    return res.data;
  },
  deleteDevice: async (id) => {
    const res = await axiosClient.delete(`/devices/${id}`);
    return res.data;
  },

  // Alerts
  getAlerts: async (severity = '', isResolved = null, page = 0, size = 20) => {
    let url = `/alerts?page=${page}&size=${size}`;
    if (severity) url += `&severity=${severity}`;
    if (isResolved !== null) url += `&isResolved=${isResolved}`;
    const res = await axiosClient.get(url);
    return res.data;
  },
  getRecentAlerts: async () => {
    const res = await axiosClient.get('/alerts/recent');
    return res.data;
  },
  processAlertAction: async (id, action, notes = '') => {
    const res = await axiosClient.post(`/alerts/${id}/action`, { action, notes });
    return res.data;
  },

  // Automation
  getAutomationRules: async () => {
    const res = await axiosClient.get('/automation/rules');
    return res.data;
  },
  getAutomationRule: async (id) => {
    const res = await axiosClient.get(`/automation/rules/${id}`);
    return res.data;
  },
  createAutomationRule: async (data) => {
    const res = await axiosClient.post('/automation/rules', data);
    return res.data;
  },
  updateAutomationRule: async (id, data) => {
    const res = await axiosClient.put(`/automation/rules/${id}`, data);
    return res.data;
  },
  deleteAutomationRule: async (id) => {
    const res = await axiosClient.delete(`/automation/rules/${id}`);
    return res.data;
  },
  getAutomationExecutions: async () => {
    const res = await axiosClient.get('/automation/executions');
    return res.data;
  },

  // Tariffs
  getTariffs: async () => {
    const res = await axiosClient.get('/tariffs');
    return res.data;
  },
  getActiveTariff: async () => {
    const res = await axiosClient.get('/tariffs/active');
    return res.data;
  },
  createTariff: async (data) => {
    const res = await axiosClient.post('/tariffs', data);
    return res.data;
  },
  updateTariff: async (id, data) => {
    const res = await axiosClient.put(`/tariffs/${id}`, data);
    return res.data;
  },
  deleteTariff: async (id) => {
    const res = await axiosClient.delete(`/tariffs/${id}`);
    return res.data;
  },

  // Reports
  getReport: async (type = 'DAILY', startDate = '', endDate = '') => {
    let url = `/reports?type=${type}`;
    if (startDate) url += `&startDate=${startDate}`;
    if (endDate) url += `&endDate=${endDate}`;
    const res = await axiosClient.get(url);
    return res.data;
  },
  getCsvExportUrl: (type = 'DAILY', startDate = '', endDate = '') => {
    let url = `/api/reports/export/csv?type=${type}`;
    if (startDate) url += `&startDate=${startDate}`;
    if (endDate) url += `&endDate=${endDate}`;
    return url;
  },

  // Settings & IoT Controls
  getSettings: async () => {
    const res = await axiosClient.get('/settings');
    return res.data;
  },
  toggleSimulation: async (enabled) => {
    const res = await axiosClient.post('/settings/simulation/toggle', { enabled });
    return res.data;
  },
  triggerSpike: async (deviceId) => {
    const res = await axiosClient.post(`/iot/spike/${deviceId}`);
    return res.data;
  },
};

export default energyService;

