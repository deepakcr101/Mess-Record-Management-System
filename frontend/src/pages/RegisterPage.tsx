// src/pages/RegisterPage.tsx
import React from 'react';
import RegistrationForm from '../features/auth/components/RegistrationForm'; // Import the RegistrationForm

const RegisterPage: React.FC = () => {
  return (
    <div style={{ textAlign: 'center' }}>
      <h2>Create Your Account</h2>
      <RegistrationForm />
    </div>
  );
};

export default RegisterPage;