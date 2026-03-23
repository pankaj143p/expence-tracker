import axios from 'axios';

const api = axios.create({ baseURL: process.env.REACT_APP_API_BASE_URL });

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export const authAPI = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
};

export const expenseAPI = {
  processNL: (text) => api.post('/expenses/nl', { text }),
  addManual: (data) => api.post('/expenses', data),
  getAll: () => api.get('/expenses'),
  delete: (id) => api.delete(`/expenses/${id}`),
  getDashboard: (month, year) => api.get(`/expenses/dashboard?month=${month}&year=${year}`),
  exportCsv: (month, year) => api.get(`/expenses/export/csv?month=${month}&year=${year}`, { responseType: 'blob' }),
};

export const budgetAPI = {
  set: (data) => api.post('/budget', data),
  get: (month, year) => api.get(`/budget?month=${month}&year=${year}`),
};

export const notificationAPI = {
  getAll: () => api.get('/notifications'),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markRead: (id) => api.put(`/notifications/${id}/read`),
};

export default api;
