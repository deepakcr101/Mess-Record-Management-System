// src/features/mealentry/components/MealEntryMarker.tsx
import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import type { SubmitHandler } from 'react-hook-form';
import { MealType } from '../../../types/menu';
import { mealEntryService } from '../services/mealEntryService';
import type { ApiErrorResponse } from '../../auth/services/authService';

interface MealEntryFormInputs {
  messProvidedUserId: string;
  mealType: MealType;
}

const MealEntryMarker: React.FC = () => {
  const { register, handleSubmit, formState: { errors, isSubmitting }, reset } = useForm<MealEntryFormInputs>();
  const [apiError, setApiError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const onSubmit: SubmitHandler<MealEntryFormInputs> = async (data) => {
    setApiError(null);
    setSuccessMessage(null);
    try {
      const response = await mealEntryService.markMealEntry(data);
      setSuccessMessage(`Successfully marked ${response.mealType} for ${response.entryDate} at ${response.entryTime}.`);
      reset(); // Clear the form on successful submission
    } catch (error: any) {
      const apiError = error as ApiErrorResponse;
      setApiError(apiError.message || "Failed to mark meal entry.");
      console.error("Meal entry marking failed:", error);
    }
  };

  // Basic Styling
  const panelStyle: React.CSSProperties = { border: '1px solid #28a745', padding: '15px', margin: '20px 0', borderRadius: '5px', backgroundColor: '#f0fff4' };
  const inputStyle: React.CSSProperties = { display: 'block', marginBottom: '10px', padding: '8px', width: 'calc(100% - 18px)', border: '1px solid #ccc', borderRadius: '4px' };
  const selectStyle: React.CSSProperties = { display: 'block', marginBottom: '10px', padding: '8px', width: '100%', border: '1px solid #ccc', borderRadius: '4px' };
  const labelStyle: React.CSSProperties = { marginBottom: '5px', display: 'block', fontWeight: 'bold' };
  const errorStyle: React.CSSProperties = { color: 'red', fontSize: '0.9em', marginTop: '5px' };
  const successStyle: React.CSSProperties = { color: 'green', fontWeight: 'bold', marginTop: '10px' };
  const buttonStyle: React.CSSProperties = { padding: '10px 15px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', marginTop: '10px',  opacity: isSubmitting ? 0.7 : 1 };


  return (
    <div style={panelStyle}>
      <h4>Mark Today's Meal Entry</h4>
      <form onSubmit={handleSubmit(onSubmit)}>
        <div>
          <label htmlFor="messProvidedUserId" style={labelStyle}>Your Mess ID</label>
          <input
            id="messProvidedUserId"
            type="text"
            style={inputStyle}
            {...register("messProvidedUserId", { required: "Your Mess ID is required" })}
          />
          {errors.messProvidedUserId && <p style={errorStyle}>{errors.messProvidedUserId.message}</p>}
        </div>

        <div>
          <label htmlFor="mealType" style={labelStyle}>Select Meal</label>
          <select
            id="mealType"
            style={selectStyle}
            {...register("mealType", { required: "Please select a meal type" })}
          >
            <option value="">-- Select a Meal --</option>
            <option value={MealType.BREAKFAST}>Breakfast</option>
            <option value={MealType.LUNCH}>Lunch</option>
            <option value={MealType.DINNER}>Dinner</option>
          </select>
          {errors.mealType && <p style={errorStyle}>{errors.mealType.message}</p>}
        </div>

        {apiError && <p style={errorStyle}>Error: {apiError}</p>}
        {successMessage && <p style={successStyle}>{successMessage}</p>}

        <button type="submit" style={buttonStyle} disabled={isSubmitting}>
          {isSubmitting ? 'Marking...' : 'Mark My Meal'}
        </button>
      </form>
    </div>
  );
};

export default MealEntryMarker;