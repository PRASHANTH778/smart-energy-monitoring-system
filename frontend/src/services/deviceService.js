import axiosClient from './axiosClient';

export const deviceService = {
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
  toggleDevice: async (id) => {
    const res = await axiosClient.post(`/devices/${id}/toggle`);
    return res.data;
  },
};

export default deviceService;

