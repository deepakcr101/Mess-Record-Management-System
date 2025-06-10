// src/features/subscriptions/services/subscriptionService.ts
import apiClient from '../../../services/apiClient';
import type { MySubscriptionStatusDTO, SubscriptionPurchaseRequestData } from '../../../types/subscription';
import type { ApiErrorResponse } from '../../auth/services/authService'; // Re-use error type
import axios from 'axios'; // For isAxiosError

const getMySubscriptionStatus = async (): Promise<MySubscriptionStatusDTO | null> => { // CHANGE HERE
  try {
    const response = await apiClient.get<MySubscriptionStatusDTO>('/subscriptions/my-status'); // CHANGE HERE
    return response.data;
  }  catch (error: any) {
    if (axios.isAxiosError(error) && error.response) {
      if (error.response.status === 404) { // Backend returns 404 if no subscription
        return null;
      }
      throw error.response.data as ApiErrorResponse;
    }
    throw error;
  }
}; 

const purchaseSubscription = async (data?: SubscriptionPurchaseRequestData): Promise<string> => {
  // The backend expects a POST, body can be empty or match SubscriptionPurchaseRequestDTO
  // The response is expected to be the Stripe Checkout Session ID (string)
  try {
    // Send the data object which should contain stripePriceId
    const response = await apiClient.post<string>('/subscriptions/purchase', data);
    return response.data; // Stripe Session ID
  } catch (error: any) {
    if (axios.isAxiosError(error) && error.response) {
      throw error.response.data as ApiErrorResponse;
    }
    throw error;
  }
};

export const subscriptionService = {
  getMySubscriptionStatus,
  purchaseSubscription,
};