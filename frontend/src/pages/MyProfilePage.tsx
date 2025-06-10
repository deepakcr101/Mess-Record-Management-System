// src/pages/MyProfilePage.tsx
import React from 'react';
import { useAuth } from '../context/AuthContext';

const MyProfilePage: React.FC = () => {
  const { user } = useAuth();
  return (
    <div>
      <h2>My Profile</h2>
      {user ? (
        <ul>
          <li>Name: {user.name}</li>
          <li>Email: {user.email}</li>
          <li>Role: {user.role}</li>
          {/* Display other user details */}
        </ul>
      ) : (
        <p>Loading profile...</p>
      )}
    </div>
  );
};
export default MyProfilePage;