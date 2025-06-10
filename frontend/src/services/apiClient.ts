// src/services/apiClient.ts
import axios from 'axios';

// Define the base URL for your Spring Boot backend
// This should come from an environment variable in a real application
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    // You can add other default headers here if needed
  },
});

// Optional: You can add interceptors for request and response handling here later.
// For example, to automatically add the JWT token to requests or handle global errors.
// We will add JWT interceptors later as per the plan.

export default apiClient;