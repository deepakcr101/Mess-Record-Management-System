// src/components/routes/RoleBasedRoute.tsx
import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

interface RoleBasedRouteProps {
  allowedRoles: string[]; // e.g., ['ADMIN'] or ['STUDENT']
}

const RoleBasedRoute: React.FC<RoleBasedRouteProps> = ({ allowedRoles }) => {
  const { isAuthenticated, user, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return <div>Loading authentication status...</div>; // Or a spinner component
  }

  if (!isAuthenticated) {
    // Not authenticated, redirect to login
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (user && !allowedRoles.includes(user.role)) {
    // Authenticated but does not have the required role
    // Redirect to an unauthorized page or homepage
    // For simplicity, we can redirect to home or show an inline message
    // In a real app, you might have a dedicated "/unauthorized" page.
    console.warn(`Access denied for role: ${user.role}. Allowed roles: ${allowedRoles.join(', ')} for path: ${location.pathname}`);
    alert('Access Denied: You do not have the required permissions to view this page.');
    return <Navigate to="/" replace />; // Redirect to homepage
  }

  // Authenticated and has the required role, render the child routes/component
  return <Outlet />;
};

export default RoleBasedRoute;