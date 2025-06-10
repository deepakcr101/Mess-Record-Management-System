// src/types/purchase.ts
import { MenuItemDTO } from './menu'; // Re-use MenuItemDTO

// Matches PurchaseResponseDTO from backend
export interface PurchaseResponseDTO {
  purchaseId: number;
  userId: number;
  userEmail: string;
  menuItem: MenuItemDTO; // Nested item details
  quantity: number;
  totalAmount: number;
  purchaseDate: string; // LocalDateTime will be string
  paymentTransactionId?: string;
}