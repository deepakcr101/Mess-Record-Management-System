// src/context/AuthContext.tsx
import React, { createContext, useState, useContext, useEffect } from 'react';
import type { ReactNode } from 'react';
import type { LoginResponseDTO } from '../types/auth'; // From our types definition
import type { UserResponseDTO } from '../types/user'; // If we store more detailed user info
import apiClient from '../services/apiClient'; // Our configured Axios instance
import { authService } from '../features/auth/services/authService';

interface AuthContextType {
  isAuthenticated: boolean;
  user: UserResponseDTO | null; // Or a simplified user object
  accessToken: string | null;
  login: (authResponse: LoginResponseDTO) => void;
  logout: () => void;
  isLoading: boolean; // To handle initial auth check if any
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [user, setUser] = useState<UserResponseDTO | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true); // Initially true

  // Effect to check for existing token or user session on app load (optional for now)
  useEffect(() => {
    // For now, we assume user is not authenticated on load.
    // Later, we could try to validate an existing token or use a refresh token flow here.
    // For simplicity, we'll just set loading to false.
    // const token = localStorage.getItem('accessToken'); // Example, not recommended for JWT access tokens
    // if (token) {
    //   // TODO: Validate token with backend /me endpoint
    //   // setAccessToken(token);
    //   // apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    //   // setIsAuthenticated(true);
    //   // Fetch user profile
    // }
    setIsLoading(false);
  }, []);

  const login = (authResponse: LoginResponseDTO) => {
    console.log("AuthContext: Login called with token:", authResponse.accessToken);
    setAccessToken(authResponse.accessToken);
    setIsAuthenticated(true);
    // Store basic user info. Ideally, you'd fetch full profile from a /me endpoint.
    // For now, we can derive some info from LoginResponseDTO if it contains it,
    // or fetch it separately. Let's assume LoginResponseDTO has some user details.
    setUser({
        userId: authResponse.userId,
        email: authResponse.email,
        role: authResponse.role,
        // Fill other fields from a /me endpoint or if available in loginResponse
        name: 'User', // Placeholder
        mobileNo: '', // Placeholder
        address: '', // Placeholder
        messProvidedUserId: undefined, // Placeholder
        createdAt: new Date().toISOString(), // Placeholder
        updatedAt: new Date().toISOString(), // Placeholder
    });

    // Set the Authorization header for all subsequent apiClient requests
    apiClient.defaults.headers.common['Authorization'] = `Bearer ${authResponse.accessToken}`;

    // Storing JWT access token in localStorage is NOT recommended for production due to XSS risks.
    // It's better to keep it in memory (React state). The refresh token is an HttpOnly cookie.
    // localStorage.setItem('accessToken', authResponse.accessToken);
  };

  const performLogout = async () => { // Renamed to avoid conflict with context's logout
    try {
      await authService.logout(); // Call backend logout
      console.log("Backend logout successful or initiated.");
    } catch (error) {
      console.error("Error during backend logout:", error);
      // Client-side logout should proceed even if backend call fails
    } finally {
      setAccessToken(null);
      setUser(null);
      setIsAuthenticated(false);
      delete apiClient.defaults.headers.common['Authorization'];
      // Any other client-side cleanup
      console.log("Client-side logout completed.");
    }
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, accessToken, login, logout: performLogout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};