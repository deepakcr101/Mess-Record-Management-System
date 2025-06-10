// src/features/auth/components/LoginForm.tsx
import React from 'react';
import { useForm } from 'react-hook-form';
import type { SubmitHandler } from 'react-hook-form'; // Import as a type
import { Link ,useNavigate,useLocation} from 'react-router-dom'; // For "Forgot Password" link
import { authService } from '../services/authService';
import type { ApiErrorResponse } from '../services/authService';
import { useAuth } from '../../../context/AuthContext';

// Define the type for our form data
interface LoginFormInputs {
  email: string;
  password: string;
}

const LoginForm: React.FC = () => {
  const { register, handleSubmit, formState: { errors, isSubmitting }, setError } = useForm<LoginFormInputs>();
  const { login } = useAuth(); // Get the login function from context
  const navigate = useNavigate(); // For redirection
  const location = useLocation();

  const onSubmit: SubmitHandler<LoginFormInputs> = async (data) => {
    try {
      const loginResponse = await authService.login(data);
      login(loginResponse);
      alert('Login successful!'); // Or use a more sophisticated notification
      navigate('/'); // Redirect to home page or dashboard after login
    } catch (error: any) {
      // ... (error handling remains the same) ...
      console.error('Login Failed:', error);
      if (error && typeof error === 'object' && 'message' in error) {
        const apiError = error as ApiErrorResponse;
        setError("root.serverError", { type: "manual", message: apiError.message || "Login failed. Please check your credentials." });
      } else {
        setError("root.serverError", { type: "manual", message: "Login Failed: An unexpected error occurred." });
      }
    }
  };

  // Basic inline styles for demonstration (replace with proper CSS/UI library later)
  const inputStyle = {
    display: 'block',
    marginBottom: '10px',
    padding: '8px',
    width: 'calc(100% - 18px)', // Adjust for padding and border
    border: '1px solid #ccc',
    borderRadius: '4px',
  };

  const labelStyle = {
    marginBottom: '5px',
    display: 'block',
    fontWeight: 'bold',
  };

  const errorStyle = {
    color: 'red',
    fontSize: '0.9em',
    marginBottom: '10px',
  };

  const buttonStyle = {
    padding: '10px 15px',
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
  };

  const formContainerStyle = {
    maxWidth: '400px',
    margin: '20px auto',
    padding: '20px',
    border: '1px solid #ddd',
    borderRadius: '8px',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
  };

  return (
    <div style={formContainerStyle}>
      <form onSubmit={handleSubmit(onSubmit)}>
        <div>
          <label htmlFor="email" style={labelStyle}>Email Address</label>
          <input
            id="email"
            type="email"
            style={inputStyle}
            {...register("email", {
              required: "Email is required",
              pattern: {
                value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                message: "Invalid email address"
              }
            })}
          />
          {errors.email && <p style={errorStyle}>{errors.email.message}</p>}
        </div>

        <div>
          <label htmlFor="password" style={labelStyle}>Password</label>
          <input
            id="password"
            type="password"
            style={inputStyle}
            {...register("password", {
              required: "Password is required",
              minLength: {
                value: 6, // As per plan (or adjust if different for login vs register)
                message: "Password must be at least 6 characters"
              }
            })}
          />
          {errors.password && <p style={errorStyle}>{errors.password.message}</p>}
        </div>

        <button type="submit" style={buttonStyle}>Login</button>
      </form>
      <div style={{ marginTop: '15px', textAlign: 'center' }}>
        {/* The plan mentions a "Forgot Password" link [cite: 164] */}
        <Link to="/forgot-password">Forgot Password?</Link>
      </div>
    </div>
  );
};

export default LoginForm;