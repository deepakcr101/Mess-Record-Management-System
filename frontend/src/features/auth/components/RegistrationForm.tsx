// src/features/auth/components/RegistrationForm.tsx
import React from 'react';
import { useForm } from 'react-hook-form';
import type { SubmitHandler } from 'react-hook-form'; // Import as a type
import { Link ,useNavigate} from 'react-router-dom'; // For a link to the login page
import { authService } from '../services/authService'; 
import type { ApiErrorResponse } from '../services/authService';

// Define the type for our form data, matching UserRegistrationRequestDTO from backend
interface RegistrationFormInputs {
  name: string;
  mobileNo: string;
  email: string;
  address: string;
  password: string;
  confirmPassword: string;
  messProvidedUserId?: string; // Optional, as per our DTO design
}

const RegistrationForm: React.FC = () => {
  const { register, handleSubmit, watch, formState: { errors, isSubmitting }, setError } = useForm<RegistrationFormInputs>({ mode: "onBlur" });
  const passwordValue = watch("password");
  const navigate = useNavigate(); // For redirection

  const onSubmit: SubmitHandler<RegistrationFormInputs> = async (data) => {
    const { confirmPassword, ...registrationData } = data;
    try {
      const registeredUser = await authService.register(registrationData);
      console.log('Registration Successful:', registeredUser);
      alert(`Registration successful for ${registeredUser.name}! Please login.`);
      navigate('/login'); // Redirect to login page
    } catch (error: any) {
      // ... (error handling as before) ...
      console.error('Registration Failed:', error);
       if (error && typeof error === 'object' && 'message' in error) {
        const apiError = error as ApiErrorResponse;
        if (apiError.details && apiError.details.length > 0) {
          apiError.details.forEach(detail => {
            setError(detail.field as keyof RegistrationFormInputs, { type: "server", message: detail.message });
          });
        }
        setError("root.serverError", { type: "manual", message: apiError.message || "Registration failed." });
      } else {
        setError("root.serverError", { type: "manual", message: "Registration Failed: An unexpected error occurred." });
      }
    }
  };

  // Basic inline styles (reuse or adapt from LoginForm for consistency)
  const inputStyle = { display: 'block', marginBottom: '10px', padding: '8px', width: 'calc(100% - 18px)', border: '1px solid #ccc', borderRadius: '4px' };
  const labelStyle = { marginBottom: '5px', display: 'block', fontWeight: 'bold' };
  const errorStyle = { color: 'red', fontSize: '0.9em', marginBottom: '10px' };
  const buttonStyle = { padding: '10px 15px', backgroundColor: '#28a745', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', marginTop: '10px' };
  const formContainerStyle = { maxWidth: '450px', margin: '20px auto', padding: '20px', border: '1px solid #ddd', borderRadius: '8px', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' };


  return (
    <div style={formContainerStyle}>
      <form onSubmit={handleSubmit(onSubmit)}>
        {/* Name Input */}
        <div>
          <label htmlFor="name" style={labelStyle}>Full Name</label>
          <input id="name" type="text" style={inputStyle} {...register("name", { required: "Full name is required" })} />
          {errors.name && <p style={errorStyle}>{errors.name.message}</p>}
        </div>
        {/* MobileNo Input */}
        <div>
          <label htmlFor="mobileNo" style={labelStyle}>Mobile Number</label>
          <input id="mobileNo" type="tel" style={inputStyle} {...register("mobileNo", { required: "Mobile number is required", pattern: { value: /^[0-9]{10,15}$/, message: "Invalid mobile number (10-15 digits)" }})} />
          {errors.mobileNo && <p style={errorStyle}>{errors.mobileNo.message}</p>}
        </div>
        {/* Email Input */}
        <div>
          <label htmlFor="email" style={labelStyle}>Email Address</label>
          <input id="email" type="email" style={inputStyle} {...register("email", { required: "Email is required", pattern: { value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i, message: "Invalid email address" }})} />
          {errors.email && <p style={errorStyle}>{errors.email.message}</p>}
        </div>
        {/* Address Input */}
        <div>
          <label htmlFor="address" style={labelStyle}>Address</label>
          <textarea id="address" style={{...inputStyle, height: '60px', resize: 'vertical' }} {...register("address", { required: "Address is required" })} />
          {errors.address && <p style={errorStyle}>{errors.address.message}</p>}
        </div>
        {/* Password Input */}
        <div>
          <label htmlFor="password" style={labelStyle}>Password</label>
          <input id="password" type="password" style={inputStyle} {...register("password", { required: "Password is required", minLength: { value: 6, message: "Password must be at least 6 characters" }})} />
          {errors.password && <p style={errorStyle}>{errors.password.message}</p>}
        </div>
        {/* Confirm Password Input */}
        <div>
          <label htmlFor="confirmPassword" style={labelStyle}>Confirm Password</label>
          <input id="confirmPassword" type="password" style={inputStyle} {...register("confirmPassword", { required: "Please confirm your password", validate: value => value === passwordValue || "Passwords do not match" })} />
          {errors.confirmPassword && <p style={errorStyle}>{errors.confirmPassword.message}</p>}
        </div>
        {/* Mess Provided User ID Input */}
        <div>
          <label htmlFor="messProvidedUserId" style={labelStyle}>Mess Provided User ID (Optional)</label>
          <input id="messProvidedUserId" type="text" style={inputStyle} {...register("messProvidedUserId")} />
          {errors.messProvidedUserId && <p style={errorStyle}>{errors.messProvidedUserId.message}</p>}
        </div>
        {/* Server Error Display */}
        {errors.root?.serverError && <p style={errorStyle}>{errors.root.serverError.message}</p>}

        <button type="submit" style={buttonStyle} disabled={isSubmitting}>
          {isSubmitting ? 'Registering...' : 'Register'}
        </button>
      </form>
      <div style={{ marginTop: '15px', textAlign: 'center' }}>
        <p>Already have an account? <Link to="/login">Login here</Link></p>
      </div>
    </div>
  );
};

export default RegistrationForm;