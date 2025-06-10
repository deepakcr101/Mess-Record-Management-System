// src/types/auth.ts
// Matches LoginSuccessBodyResponseDTO from backend for what's in the body
// and LoginResponseDTO for the full payload including refresh token if we were getting it in body
export interface LoginResponseDTO {
  accessToken: string;
  refreshToken?: string; // Keep optional if it's only in cookie
  tokenType: string;
  userId: number; // Assuming Long is number in TS
  email: string;
  role: string; // Assuming Role enum is string
}

// You might have other auth related types here