// src/types/user.ts
// Matches UserResponseDTO from backend
export interface UserResponseDTO {
  userId: number; // Assuming Long is number
  name: string;
  mobileNo: string;
  email: string;
  address: string;
  role: string; // e.g., "STUDENT", "ADMIN"
  messProvidedUserId?: string;
  createdAt: string; // Assuming LocalDateTime is string (ISO format)
  updatedAt: string; // Assuming LocalDateTime is string (ISO format)
}