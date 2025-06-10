// src/pages/AdminDashboardPage.tsx
import React from 'react';
import { useAuth } from '../context/AuthContext';
import UserManagementTable from '../features/admin-dashboard/components/UserManagementTable';
import MenuManagement from '../features/admin-dashboard/components/MenuManagement';
import WeeklyMenuSetup from '../features/admin-dashboard/components/WeeklyMenuSetup';
import ReportsView from '../features/admin-dashboard/components/ReportsView'; // Import

const AdminDashboardPage: React.FC = () => {
  const { user } = useAuth();

  return (
    <div>
      <h2>Admin Dashboard</h2>
      <p>Welcome, Admin {user?.name || user?.email}!</p>
      <hr />
      <ReportsView /> {/* Add the ReportsView component */}
      <hr />
      <UserManagementTable />
      <hr />
      <MenuManagement />
      <hr />
      <WeeklyMenuSetup />
      <hr />
    </div>
  );
};

export default AdminDashboardPage;