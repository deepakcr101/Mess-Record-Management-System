// src/features/admin-dashboard/services/userService.ts
import apiClient from '../../../services/apiClient';
import type { Page } from '../../../types/common';
import type { UserResponseDTO } from '../../../types/user';
import type { AdminCreateUserRequestDTO, AdminUpdateUserRequestDTO } from '../../../types/admin'; // We'll create these types
import type { ApiErrorResponse } from '../../auth/services/authService';
import axios from 'axios';

const getAllUsers = async (page: number, size: number): Promise<Page<UserResponseDTO>> => {
  try {
    const response = await apiClient.get<Page<UserResponseDTO>>('/users', {
      params: { page, size, sort: 'name,asc' }
    });
    return response.data;
  } catch (error: any) {
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data as ApiErrorResponse;
    }
    throw error;
  }
};

const getUserById = async (userId: number): Promise<UserResponseDTO> => {
    try {
        const response = await apiClient.get<UserResponseDTO>(`/users/${userId}`);
        return response.data;
    } catch (error: any) {
         if (axios.isAxiosError(error) && error.response) {
          throw error.response.data as ApiErrorResponse;
        }
        throw error;
    }
};

const createUser = async (userData: AdminCreateUserRequestDTO): Promise<UserResponseDTO> => {
    try {
        const response = await apiClient.post<UserResponseDTO>('/users', userData);
        return response.data;
    } catch (error: any) {
        if (axios.isAxiosError(error) && error.response) {
          throw error.response.data as ApiErrorResponse;
        }
        throw error;
    }
};

const updateUser = async (userId: number, userData: AdminUpdateUserRequestDTO): Promise<UserResponseDTO> => {
    try {
        const response = await apiClient.put<UserResponseDTO>(`/users/${userId}`, userData);
        return response.data;
    } catch (error: any) {
        if (axios.isAxiosError(error) && error.response) {
          throw error.response.data as ApiErrorResponse;
        }
        throw error;
    }
};

const deleteUser = async (userId: number): Promise<void> => {
    try {
        await apiClient.delete(`/users/${userId}`);
    } catch (error: any) {
        if (axios.isAxiosError(error) && error.response) {
          throw error.response.data as ApiErrorResponse;
        }
        throw error;
    }
};


export const adminUserService = {
  getAllUsers,
  getUserById,
  createUser,
  updateUser,
  deleteUser,
};