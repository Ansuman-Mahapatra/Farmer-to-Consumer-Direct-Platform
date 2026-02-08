import axios from 'axios';

const API_BASE_URL = 'http://localhost:8082/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Handle response errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth APIs
export const authAPI = {
  register: (userData) => api.post('/auth/register', userData),
  login: (email, password) => api.post('/auth/login', { email, password }),
};

// Farmer APIs
export const farmerAPI = {
  addProduct: (formData) => api.post('/farmer/products', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
  updateProduct: (productId, productDataObj, imageFile) => {
    const formData = new FormData();
    const productData = productDataObj.productData || productDataObj;
    
    // Add product fields as form data
    if (productData.name) formData.append('name', productData.name);
    if (productData.category) formData.append('category', productData.category);
    if (productData.pricePerKg !== undefined) formData.append('pricePerKg', productData.pricePerKg);
    if (productData.availableQuantity !== undefined) formData.append('availableQuantity', productData.availableQuantity);
    if (productData.description) formData.append('description', productData.description);
    if (productData.language) formData.append('language', productData.language);
    
    if (imageFile) {
      formData.append('image', imageFile);
    }
    
    // Note: Backend expects @RequestBody for product data, but we're sending form data
    // This may require backend adjustment to use @ModelAttribute instead
    return api.put(`/farmer/products/${productId}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  getMyProducts: () => api.get('/farmer/products'),
  getProduct: (productId) => api.get(`/farmer/products/${productId}`),
  updateInventory: (productId, quantity) => 
    api.patch(`/farmer/products/${productId}/inventory`, { quantity }),
};

// Consumer APIs
export const consumerAPI = {
  browseProducts: (params) => api.get('/consumer/products', { params }),
  getCategories: () => api.get('/consumer/products/categories'),
  placeOrder: (orderData) => api.post('/consumer/orders', orderData),
  confirmPayment: (orderId, paymentData) => 
    api.post(`/consumer/orders/${orderId}/confirm-payment`, paymentData),
  getMyOrders: () => api.get('/consumer/orders'),
  getOrder: (orderId) => api.get(`/consumer/orders/${orderId}`),
  getPaymentDetails: (orderId) => api.get(`/consumer/orders/${orderId}/payment-details`),
};

export default api;

