import axiosClient from './axiosClient';
import { authService } from './authService';
import { deviceService } from './deviceService';
import { energyService } from './energyService';
import { alertService } from './alertService';

export {
  axiosClient,
  authService,
  deviceService,
  energyService,
  alertService,
};

export default {
  axios: axiosClient,
  auth: authService,
  devices: deviceService,
  energy: energyService,
  alerts: alertService,
};

