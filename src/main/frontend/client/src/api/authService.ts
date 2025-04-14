import api from './axios';

interface LoginCredentials {
  email: string;
  password: string;
}

interface RegisterData {
  name: string;
  email: string;
  password: string;
}

// Authentication service
const authService = {
  // Login user
  login: async (credentials: LoginCredentials) => {
    const response = await api.post('/auth/login', credentials);
    return response.data;
  },

  // Register user
  register: async (userData: RegisterData) => {
    const response = await api.post('/auth/register', userData);
    return response.data;
  },

  // Get current user
  getCurrentUser: async () => {
    const token = localStorage.getItem('token');
    if (token) {
      const response = await api.get('/auth/current-user');
      return response.data;
    }
    return null;
  },

  // Logout user
  logout: () => {
    localStorage.removeItem('token');
  },
};

export default authService; 