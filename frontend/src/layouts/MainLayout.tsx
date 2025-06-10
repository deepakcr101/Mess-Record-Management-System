// src/layouts/MainLayout.tsx
import React from 'react';
import { Outlet } from 'react-router-dom';
import Navbar from '../components/shared/Navbar';
import Footer from '../components/shared/Footer';


const MainLayout: React.FC = () => {
  return (
    <div className="main-layout">
      <Navbar />
      <main style={{ minHeight: 'calc(100vh - 150px)', padding: '0 20px' }}> {/* Basic styling for content area */}
        <Outlet /> {/* Child routes will render here */}
      </main>
      <Footer />
    </div>
  );
};

export default MainLayout;