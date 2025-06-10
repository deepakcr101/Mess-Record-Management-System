// src/features/mealentry/services/mealEntryService.ts
import apiClient from '../../../services/apiClient';
import type { MealEntryRequestData, MealEntryResponseData } from '../../../types/mealEntry';
import type { Page } from '../../../types/common.ts'; // We'll create this common Page type
import type { ApiErrorResponse } from '../../auth/services/authService';
import axios from 'axios';

const markMealEntry = async (data: MealEntryRequestData): Promise<MealEntryResponseData> => {
  // ... (existing implementation)
  try {
    const response = await apiClient.post<MealEntryResponseData>('/meal-entries/mark', data);
    return response.data;
  } catch (error: any) {
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data as ApiErrorResponse;
    }
    throw error;
  }
};

const getMyHistory = async (page: number, size: number): Promise<Page<MealEntryResponseData>> => {
  try {
    const response = await apiClient.get<Page<MealEntryResponseData>>('/meal-entries/my-history', {
      params: {
        page: page,
        size: size,
        // sort parameter can be added here if needed, e.g., 'entryDate,desc'
      }
    });
    return response.data;
  } catch (error: any) {
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data as ApiErrorResponse;
    }
    throw error;
  }
};

export const mealEntryService = {
  markMealEntry,
  getMyHistory, // Add the new function
};