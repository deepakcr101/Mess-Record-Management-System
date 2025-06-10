// src/pages/LoginPage.tsx
import React from 'react';
import LoginForm from '../features/auth/components/LoginForm'; // Import the LoginForm

const LoginPage: React.FC = () => {
  return (
    <div style={{ textAlign: 'center' }}>
      <h2>Login</h2>
      <LoginForm />
    </div>
  );
};

export default LoginPage;