// src/types/subscription.ts

// Matches SubscriptionStatus enum from backend
export enum SubscriptionStatus {
    // Statuses from the backend database
    ACTIVE = 'ACTIVE',
    PENDING_PAYMENT = 'PENDING_PAYMENT',
    EXPIRED = 'EXPIRED',
    CANCELLED = 'CANCELLED',
    // Special status derived by the service layer
    NO_SUBSCRIPTION_HISTORY = 'NO_SUBSCRIPTION_HISTORY'
}

// Add this interface to match the Java MySubscriptionStatusDTO
export interface MySubscriptionStatusDTO {
  subscriptionId: number | null;
  stripeSubscriptionId: string | null;
  status: SubscriptionStatus | 'NO_SUBSCRIPTION_HISTORY'; // Use the enum for type safety
  startDate: string | null;
  endDate: string | null;
  planName: string | null;
  amountPaid: number | null;
  createdAt: string | null;
}

// Matches SubscriptionResponseDTO from backend
export interface SubscriptionResponseDTO {
  subscriptionId: number; // Assuming Long is number
  userId: number;
  userEmail: string;
  startDate: string; // LocalDate will be string (YYYY-MM-DD)
  endDate: string;   // LocalDate will be string (YYYY-MM-DD)
  status: SubscriptionStatus;
  amountPaid: number; // BigDecimal will be number
  paymentTransactionId?: string;
  stripeSubscriptionId?: string;
  createdAt: string;  // LocalDateTime will be string
  updatedAt: string;  // LocalDateTime will be string
}

// We might also need a type for the purchase request if it's not empty
// For now, our backend takes an optional, potentially empty SubscriptionPurchaseRequestDTO
export interface SubscriptionPurchaseRequestData {
    stripePriceId?: string; // Example, if we distinguish plans
}