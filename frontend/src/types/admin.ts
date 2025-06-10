// src/types/admin.ts

// Matches Role enum from backend
export const Role = {
    STUDENT: 'STUDENT',
    ADMIN: 'ADMIN',
} as const;

export type Role = typeof Role[keyof typeof Role];

// Matches AdminCreateUserRequestDTO from backend
export interface AdminCreateUserRequestDTO {
  name: string;
  mobileNo: string;
  email: string;
  address: string;
  password?: string; // Password is required for create
  messProvidedUserId?: string;
  role: Role;
}

// Matches AdminUpdateUserRequestDTO from backend
export interface AdminUpdateUserRequestDTO {
  name?: string;
  mobileNo?: string;
  email?: string;
  address?: string;
  messProvidedUserId?: string;
  role?: Role;
  // Password is typically handled separately, so it's optional here
  password?: string; 
}