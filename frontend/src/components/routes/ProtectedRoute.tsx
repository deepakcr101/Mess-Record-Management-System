// src/components/routes/ProtectedRoute.tsx
import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

interface ProtectedRouteProps {
  // We can add props here if needed, e.g., allowedRoles for role-based protected routes
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = () => {
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation(); // To remember where the user was trying to go

  if (isLoading) {
    // Show a loading spinner or a blank page while auth state is being determined
    // This is important if you have async logic in AuthProvider's useEffect for initial auth check
    return <div>Loading authentication status...</div>;
  }

  if (!isAuthenticated) {
    // If not authenticated, redirect to the login page
    // Pass the current location in state so we can redirect back after login
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // If authenticated, render the child routes/component
  // <Outlet /> is used when this ProtectedRoute wraps nested routes in App.tsx
  return <Outlet />;
};

export default ProtectedRoute;