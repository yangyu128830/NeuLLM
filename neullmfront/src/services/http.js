import axios from 'axios';
import { getToken, clearAuth } from '../stores/auth';

const baseURL = import.meta.env.VITE_API_BASE || (import.meta.env.DEV ? '' : 'http://localhost:8082');

const http = axios.create({
  baseURL,
  timeout: 120000,
});

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  if (!config.headers['Content-Type'] && !(config.data instanceof FormData)) {
    config.headers['Content-Type'] = 'application/json';
  }
  return config;
});

http.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      clearAuth();
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(err);
  }
);

export default http;
