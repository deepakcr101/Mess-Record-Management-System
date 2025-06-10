// src/features/admin-dashboard/components/UserForm.tsx
import React, { useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import type { SubmitHandler } from 'react-hook-form';
import { TextField, Button, Select, MenuItem, FormControl, InputLabel, CircularProgress, FormHelperText } from '@mui/material';
import type { AdminCreateUserRequestDTO } from '../../../types/admin';
import { Role } from '../../../types/admin';

interface UserFormProps {
  onSubmit: SubmitHandler<AdminCreateUserRequestDTO>;
  initialData?: Partial<AdminCreateUserRequestDTO> | null;
  isEditMode: boolean;
  isLoading: boolean;
}

const UserForm: React.FC<UserFormProps> = ({ onSubmit, initialData, isEditMode, isLoading }) => {
  const { control, handleSubmit, reset, formState: { errors } } = useForm<AdminCreateUserRequestDTO>({
    defaultValues: initialData || {
      name: '',
      email: '',
      mobileNo: '',
      address: '',
      password: '',
      messProvidedUserId: '',
      role: Role.STUDENT,
    },
  });

  useEffect(() => {
    reset(initialData || { role: Role.STUDENT }); // Reset form when initialData changes
  }, [initialData, reset]);

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <Controller
          name="name"
          control={control}
          rules={{ required: 'Name is required' }}
          render={({ field }) => (
            <TextField {...field} label="Full Name" variant="outlined" margin="normal" fullWidth error={!!errors.name} helperText={errors.name?.message} />
          )}
        />
        <Controller
          name="email"
          control={control}
          rules={{ required: 'Email is required', pattern: { value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i, message: "Invalid email address" } }}
          render={({ field }) => (
            <TextField {...field} label="Email" type="email" variant="outlined" margin="normal" fullWidth error={!!errors.email} helperText={errors.email?.message} />
          )}
        />
         <Controller
          name="mobileNo"
          control={control}
          rules={{ required: 'Mobile number is required', pattern: { value: /^[0-9]{10,15}$/, message: "Invalid mobile number (10-15 digits)"} }}
          render={({ field }) => (
            <TextField {...field} label="Mobile Number" variant="outlined" margin="normal" fullWidth error={!!errors.mobileNo} helperText={errors.mobileNo?.message} />
          )}
        />
         <Controller
          name="address"
          control={control}
          rules={{ required: 'Address is required' }}
          render={({ field }) => (
            <TextField {...field} label="Address" multiline rows={3} variant="outlined" margin="normal" fullWidth error={!!errors.address} helperText={errors.address?.message} />
          )}
        />
        {!isEditMode && (
            <Controller
            name="password"
            control={control}
            rules={{ required: 'Password is required for new users', minLength: { value: 6, message: 'Password must be at least 6 characters' } }}
            render={({ field }) => (
                <TextField {...field} label="Password" type="password" variant="outlined" margin="normal" fullWidth error={!!errors.password} helperText={errors.password?.message} />
            )}
            />
        )}
        <Controller
          name="messProvidedUserId"
          control={control}
          render={({ field }) => (
            <TextField {...field} label="Mess Provided ID (Optional)" variant="outlined" margin="normal" fullWidth error={!!errors.messProvidedUserId} helperText={errors.messProvidedUserId?.message} />
          )}
        />
         <FormControl fullWidth margin="normal" error={!!errors.role}>
            <InputLabel id="role-select-label">Role</InputLabel>
            <Controller
                name="role"
                control={control}
                defaultValue={Role.STUDENT}
                rules={{ required: 'Role is required' }}
                render={({ field }) => (
                    <Select {...field} labelId="role-select-label" label="Role">
                        <MenuItem value={Role.STUDENT}>STUDENT</MenuItem>
                        <MenuItem value={Role.ADMIN}>ADMIN</MenuItem>
                    </Select>
                )}
            />
            {errors.role && <FormHelperText>{errors.role.message}</FormHelperText>}
        </FormControl>
        <Button type="submit" variant="contained" color="primary" disabled={isLoading} sx={{ mt: 2 }}>
            {isLoading ? <CircularProgress size={24} /> : (isEditMode ? 'Update User' : 'Create User')}
        </Button>
    </form>
  );
};

export default UserForm;