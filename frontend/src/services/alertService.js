import axiosClient from './axiosClient';

export const alertService = {
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
  getAlertCount: async () => {
    const res = await axiosClient.get('/alerts/count');
    return res.data;
  },
  processAlertAction: async (id, action, notes = '') => {
    const res = await axiosClient.post(`/alerts/${id}/action`, { action, notes });
    return res.data;
  },
  acknowledgeAlert: async (id) => {
    const res = await axiosClient.put(`/alerts/${id}/acknowledge`);
    return res.data;
  },
  resolveAlert: async (id) => {
    const res = await axiosClient.put(`/alerts/${id}/resolve`);
    return res.data;
  },
};

export default alertService;

