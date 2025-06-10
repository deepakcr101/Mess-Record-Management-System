// src/features/purchase/services/purchaseService.ts
import apiClient from '../../../services/apiClient';
import type { PurchaseResponseDTO } from '../../../types/purchase'; // We'll create this type
import type { Page } from '../../../types/common'; // Re-use common Page type
import type { ApiErrorResponse } from '../../auth/services/authService';
import axios from 'axios';

// We can also move the DishPurchaseRequestData interface here if we want
interface DishPurchaseRequestData {
  itemId: number;
  quantity: number;
}

const initiateDishPurchase = async (data: DishPurchaseRequestData): Promise<string> => {
  // This is the function we'll use for the "Buy Now" button later
  try {
    const response = await apiClient.post<string>('/purchases', data);
    return response.data; // Stripe Session ID
  } catch (error: any) {
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data as ApiErrorResponse;
    }
    throw error;
  }
};

const getMyPurchaseHistory = async (page: number, size: number): Promise<Page<PurchaseResponseDTO>> => {
  try {
    const response = await apiClient.get<Page<PurchaseResponseDTO>>('/purchases/my-history', {
      params: { page, size }
    });
    return response.data;
  } catch (error: any) {
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data as ApiErrorResponse;
    }
    throw error;
  }
};

export const purchaseService = {
  initiateDishPurchase,
  getMyPurchaseHistory,
};