// src/features/admin-dashboard/services/reportService.ts
import apiClient from '../../../services/apiClient';
import type { ApiErrorResponse } from '../../auth/services/authService';
import axios from 'axios';

// Define types for the expected API responses
interface CountResponse {
    [key: string]: number;
}

interface SalesSummaryResponse {
    startDate: string;
    endDate: string;
    totalSales: number;
}

const getTotalStudentCount = async (): Promise<CountResponse> => {
    try {
        const response = await apiClient.get<CountResponse>('/admin/reports/students/count');
        return response.data;
    } catch (error: any) {
        if (axios.isAxiosError(error) && error.response) {
          throw error.response.data as ApiErrorResponse;
        }
        throw error;
    }
};

const getActiveSubscriptionCount = async (): Promise<CountResponse> => {
    try {
        const response = await apiClient.get<CountResponse>('/admin/reports/subscriptions/active-count');
        return response.data;
    } catch (error: any) {
         if (axios.isAxiosError(error) && error.response) {
          throw error.response.data as ApiErrorResponse;
        }
        throw error;
    }
};

const getSalesSummary = async (startDate: string, endDate: string): Promise<SalesSummaryResponse> => {
    try {
        const response = await apiClient.get<SalesSummaryResponse>('/admin/reports/sales/summary', {
            params: { startDate, endDate }
        });
        return response.data;
    } catch (error: any) {
        if (axios.isAxiosError(error) && error.response) {
          throw error.response.data as ApiErrorResponse;
        }
        throw error;
    }
};

// Add other report fetching functions here as needed

export const reportService = {
    getTotalStudentCount,
    getActiveSubscriptionCount,
    getSalesSummary,
};