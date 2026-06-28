 import axios from 'axios';

 const apiClient = axios.create({
   baseURL: '/api/v1',
   timeout: 30000,
   headers: { 'Content-Type': 'application/json' },
 });

 apiClient.interceptors.request.use((config) => {
   const token = localStorage.getItem('token');
   if (token) config.headers.Authorization = `Bearer ${token}`;
   const tenantId = localStorage.getItem('tenantId');
   if (tenantId) config.headers['X-Tenant-Id'] = tenantId;
   return config;
 });

 apiClient.interceptors.response.use(
   (res) => res,
   (err) => {
     if (err.response?.status === 401) {
       localStorage.removeItem('token');
       window.location.href = '/login';
     }
     return Promise.reject(err);
   }
 );

 export default apiClient;

 // == 电池护照 API ==
 export const passportApi = {
   list: (params: any) => apiClient.get('/passports', { params }),
   get: (id: string) => apiClient.get(`/passports/${id}`),
   create: (data: any) => apiClient.post('/passports', data),
   update: (id: string, data: any) => apiClient.put(`/passports/${id}`, data),
   delete: (id: string) => apiClient.delete(`/passports/${id}`),
   search: (sn: string) => apiClient.get('/passports/search', { params: { serialNumber: sn } }),
 };

 export const tenantApi = {
   list: (params: any) => apiClient.get('/tenants', { params }),
   get: (id: number) => apiClient.get(`/tenants/${id}`),
   create: (data: any) => apiClient.post('/tenants', data),
 };
