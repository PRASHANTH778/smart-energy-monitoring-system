import axiosClient from './axiosClient';

export const authService = {
  login: async (credentials) => {
    const response = await axiosClient.post('/auth/login', credentials);
    return response.data;
  },
  register: async (userData) => {
    const response = await axiosClient.post('/auth/register', userData);
    return response.data;
  },
  getProfile: async () => {
    const response = await axiosClient.get('/auth/profile');
    return response.data;
  },
  updateProfile: async (data) => {
    const response = await axiosClient.put('/auth/profile', data);
    return response.data;
  },
  changePassword: async (passwordData) => {
    const response = await axiosClient.post('/auth/change-password', passwordData);
    return response.data;
  },
};

export default authService;

