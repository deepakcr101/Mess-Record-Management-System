// src/pages/StudentDashboardPage.tsx
import React from 'react';
import { useAuth } from '../context/AuthContext';
import WeeklyMenuDisplay from '../features/menu/components/WeeklyMenuDisplay';
import SubscriptionPanel from '../features/subscriptions/components/SubscriptionPanel';
import MealEntryMarker from '../features/mealentry/components/MealEntryMarker';
import MealEntryHistory from '../features/mealentry/components/MealEntryHistory';
import PurchaseHistory from '../features/purchase/components/PurchaseHistory'; // Import

const StudentDashboardPage: React.FC = () => {
  const { user } = useAuth();

  return (
    <div>
      <h2>Student Dashboard</h2>
      <p>Welcome, {user?.name || user?.email}!</p>
      <hr />
      <MealEntryMarker />
      <hr />
      <SubscriptionPanel />
      <hr />
      <MealEntryHistory />
      <hr />
      <PurchaseHistory /> 
      <hr />
      <WeeklyMenuDisplay />
      <hr />
    </div>
  );
};

export default StudentDashboardPage;