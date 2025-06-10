// src/features/auth/services/authService.ts
import apiClient from '../../../services/apiClient';
import type { LoginResponseDTO } from '../../../types/auth';
import type { UserResponseDTO } from '../../../types/user';
import axios from 'axios';

interface LoginCredentials {
  email: string;
  password: string;
}

interface RegistrationData {
  name: string;
  mobileNo: string;
  email: string;
  address: string;
  password: string;
  messProvidedUserId?: string;
}

interface ApiErrorDetail {
  field: string;
  message: string;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: ApiErrorDetail[];
}

const login = async (credentials: LoginCredentials): Promise<LoginResponseDTO> => {
  try {
    const response = await apiClient.post<LoginResponseDTO>('/auth/login', credentials);
    return response.data;
  } catch (error: any) {
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data as ApiErrorResponse;
    }
    throw error;
  }
};

const register = async (userData: RegistrationData): Promise<UserResponseDTO> => {
  try {
    const response = await apiClient.post<UserResponseDTO>('/auth/register', userData);
    return response.data;
  } catch (error: any) {
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data as ApiErrorResponse;
    }
    throw error;
  }
};

const logout = async (): Promise<string> => {
  try {
    // The backend /logout endpoint expects a POST and will clear the HttpOnly refresh token cookie
    // It also uses the refresh token from the cookie to blacklist it.
    // The request might not need a body if the backend relies purely on the cookie.
    const response = await apiClient.post<string>('/auth/logout');
    return response.data; // e.g., "Logout successful..."
  } catch (error: any) {
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data as ApiErrorResponse;
    }
    // Even if backend logout fails, client-side logout should proceed.
    console.error("Backend logout failed, proceeding with client-side logout:", error);
    throw error; // Or return a specific error object/message
  }
};

export const authService = {
  login,
  register,
  logout, // Add logout
};