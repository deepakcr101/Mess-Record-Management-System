// src/App.tsx
import { Routes, Route } from 'react-router-dom'; // Removed BrowserRouter from here
import './App.css';

import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import MainLayout from './layouts/MainLayout';
import ProtectedRoute from './components/routes/ProtectedRoute'; 
import RoleBasedRoute from './components/routes/RoleBasedRoute';

import MyProfilePage from './pages/MyProfilePage';
import StudentDashboardPage from './pages/StudentDashboardPage'; // Import
import AdminDashboardPage from './pages/AdminDashboardPage'; 

function App() {
  return (
    <Routes>
      <Route element={<MainLayout />}>
        {/* Public Routes */}
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />

        {/* Protected Routes for any authenticated user */}
        <Route element={<ProtectedRoute />}>
          <Route path="/my-profile" element={<MyProfilePage />} />
          {/* Add other generic protected routes here */}
        </Route>

        {/* Role-Based Protected Routes */}
        <Route element={<RoleBasedRoute allowedRoles={['STUDENT']} />}>
          <Route path="/student/dashboard" element={<StudentDashboardPage />} />
          {/* Add other student-specific routes here */}
        </Route>

        <Route element={<RoleBasedRoute allowedRoles={['ADMIN']} />}>
          <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
          {/* Add other admin-specific routes here */}
        </Route>

        {/* Optional: Fallback for unmatched routes within MainLayout */}
        {/* <Route path="*" element={<NotFoundPage />} /> */}
      </Route>

      {/* Routes without MainLayout (e.g., a dedicated full-page NotFound) */}
      {/* <Route path="*" element={<NotFoundPage />} /> */}
    </Routes>
  );
}


export default App;