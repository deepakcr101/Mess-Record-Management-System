// src/components/shared/Navbar.tsx
import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext'; // Import useAuth

const Navbar: React.FC = () => {
  const { isAuthenticated, user, logout } = useAuth(); // Get auth state and logout function
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout(); // Call the logout from context
    navigate('/login'); // Redirect to login page after logout
  };

  // Basic inline styles for demonstration
  const navStyle: React.CSSProperties = { padding: '1rem', background: '#333', color: 'white', display: 'flex', justifyContent: 'space-between', alignItems: 'center' };
  const linkStyle: React.CSSProperties = { color: 'white', marginRight: '1rem', textDecoration: 'none' };
  const buttonStyle: React.CSSProperties = { background: 'transparent', border: '1px solid white', color: 'white', padding: '0.5rem 1rem', cursor: 'pointer', borderRadius: '4px' };
  const userInfoStyle: React.CSSProperties = { marginRight: '1rem' };


  return (
    <nav style={navStyle}>
      <div>
        <Link to="/" style={linkStyle}>MessApp</Link>
        {/* Example of a link that might only show for authenticated users */}
        {isAuthenticated && (
          <Link to="/my-profile" style={linkStyle}>My Profile</Link> 
        )}
         {isAuthenticated && user?.role === 'ADMIN' && (
          <Link to="/admin/dashboard" style={linkStyle}>Admin Dashboard</Link>
        )}
         {isAuthenticated && user?.role === 'STUDENT' && (
          <Link to="/student/dashboard" style={linkStyle}>Student Dashboard</Link>
        )}
      </div>
      <div>
        {isAuthenticated ? (
          <>
            <span style={userInfoStyle}>Welcome, {user?.name || user?.email}! ({user?.role})</span>
            <button onClick={handleLogout} style={buttonStyle}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login" style={linkStyle}>Login</Link>
            <Link to="/register" style={linkStyle}>Register</Link>
          </>
        )}
      </div>
    </nav>
  );
};

export default Navbar;