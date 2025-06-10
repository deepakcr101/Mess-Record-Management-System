// src/components/shared/Footer.tsx
import React from 'react';

const Footer: React.FC = () => {
  return (
    <footer style={{ padding: '1rem', background: '#f0f0f0', textAlign: 'center', marginTop: '2rem', borderTop: '1px solid #ddd' }}>
      <p>&copy; {new Date().getFullYear()} Mess Management System. All rights reserved.</p>
    </footer>
  );
};

export default Footer;