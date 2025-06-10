// src/types/mealEntry.ts
import { MealType } from './menu'; // Re-use MealType enum from menu types

// Matches MealEntryRequestDTO from backend
export interface MealEntryRequestData {
  messProvidedUserId: string;
  mealType: MealType;
}

// Matches MealEntryResponseDTO from backend
export interface MealEntryResponseData {
  entryId: number;
  userId: number;
  userEmail: string;
  userName: string;
  mealType: MealType;
  entryDate: string; // LocalDate will be string
  entryTime: string; // LocalTime will be string
  verifiedByAdminId?: number;
  verifiedByAdminName?: string;
}