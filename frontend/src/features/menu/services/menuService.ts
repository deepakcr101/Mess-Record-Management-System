// src/features/menu/services/menuService.ts
import apiClient from '../../../services/apiClient';
import type { WeeklyMenuResponseDTO, MenuItemDTO, WeeklyMenuSetupRequestDTO } from '../../../types/menu';
import type { Page } from '../../../types/common';
import type { ApiErrorResponse } from '../../auth/services/authService';
import axios from 'axios';

// ... (getWeeklyMenu, getAllMenuItems, createMenuItem, updateMenuItem, deleteMenuItem functions are here) ...

interface GetWeeklyMenuParams {
  date?: string; 
  startDate?: string;
  endDate?: string;
}

const getWeeklyMenu = async (params?: GetWeeklyMenuParams): Promise<WeeklyMenuResponseDTO> => {
  try {
    const response = await apiClient.get<WeeklyMenuResponseDTO>('/weekly-menu', { params });
    return response.data;
  } catch (error: any) {
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data as ApiErrorResponse;
    }
    throw error;
  }
};
const getAllMenuItems = async (page: number, size: number): Promise<Page<MenuItemDTO>> => {
  try {
    const response = await apiClient.get<Page<MenuItemDTO>>('/menu-items', {
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
const createMenuItem = async (menuItemData: Partial<MenuItemDTO>): Promise<MenuItemDTO> => {
    try {
        const response = await apiClient.post<MenuItemDTO>('/menu-items', menuItemData);
        return response.data;
    } catch (error: any) {
        if (axios.isAxiosError(error) && error.response) {
          throw error.response.data as ApiErrorResponse;
        }
        throw error;
    }
};
const updateMenuItem = async (itemId: number, menuItemData: Partial<MenuItemDTO>): Promise<MenuItemDTO> => {
    try {
        const response = await apiClient.put<MenuItemDTO>(`/menu-items/${itemId}`, menuItemData);
        return response.data;
    } catch (error: any) {
        if (axios.isAxiosError(error) && error.response) {
          throw error.response.data as ApiErrorResponse;
        }
        throw error;
    }
};
const deleteMenuItem = async (itemId: number): Promise<void> => {
    try {
        await apiClient.delete(`/menu-items/${itemId}`);
    } catch (error: any) {
        if (axios.isAxiosError(error) && error.response) {
          throw error.response.data as ApiErrorResponse;
        }
        throw error;
    }
};


// --- THIS IS THE FUNCTION WE NEED ---
const setupWeeklyMenu = async (data: WeeklyMenuSetupRequestDTO): Promise<string> => {
    try {
        const response = await apiClient.post<string>('/weekly-menu', data);
        return response.data;
    } catch (error: any) {
        if (axios.isAxiosError(error) && error.response) {
          throw error.response.data as ApiErrorResponse;
        }
        throw error;
    }
};

// --- AND THIS IS THE CRUCIAL EXPORT PART ---
export const menuService = {
  getWeeklyMenu,
  getAllMenuItems,
  createMenuItem,
  updateMenuItem,
  deleteMenuItem,
  setupWeeklyMenu, // <-- Make 100% sure this line is here!
};