// src/features/menu/components/MenuItemForm.tsx
import React, { useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import type { SubmitHandler } from 'react-hook-form';
import { TextField, Button, Select, MenuItem, FormControl, InputLabel, FormHelperText, CircularProgress, Switch, FormControlLabel } from '@mui/material';
import type { MenuItemDTO } from '../../../types/menu';
import { MenuCategory } from '../../../types/menu';

interface MenuItemFormProps {
  onSubmit: SubmitHandler<MenuItemDTO>;
  initialData?: Partial<MenuItemDTO> | null;
  isEditMode: boolean;
  isLoading: boolean;
}

const MenuItemForm: React.FC<MenuItemFormProps> = ({ onSubmit, initialData, isEditMode, isLoading }) => {
  const { control, handleSubmit, reset, formState: { errors } } = useForm<MenuItemDTO>({
    defaultValues: initialData || {
      name: '',
      description: '',
      price: 0,
      category: MenuCategory.LUNCH,
      isAvailable: true,
      imageUrl: '',
    },
  });

  useEffect(() => {
    reset(initialData || { name: '', description: '', price: 0, category: MenuCategory.LUNCH, isAvailable: true, imageUrl: '' });
  }, [initialData, reset]);

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate>
      <Controller
        name="name"
        control={control}
        rules={{ required: 'Name is required' }}
        render={({ field }) => (
          <TextField {...field} label="Item Name" variant="outlined" margin="normal" fullWidth error={!!errors.name} helperText={errors.name?.message} />
        )}
      />
      <Controller
        name="description"
        control={control}
        render={({ field }) => (
          <TextField {...field} label="Description" multiline rows={3} variant="outlined" margin="normal" fullWidth />
        )}
      />
      <Controller
        name="price"
        control={control}
        rules={{ required: 'Price is required', min: { value: 0, message: 'Price cannot be negative' } }}
        render={({ field }) => (
          <TextField {...field} label="Price" type="number" variant="outlined" margin="normal" fullWidth error={!!errors.price} helperText={errors.price?.message} />
        )}
      />
      <FormControl fullWidth margin="normal" error={!!errors.category}>
        <InputLabel id="category-select-label">Category</InputLabel>
        <Controller
          name="category"
          control={control}
          rules={{ required: 'Category is required' }}
          render={({ field }) => (
            <Select {...field} labelId="category-select-label" label="Category">
              <MenuItem value={MenuCategory.BREAKFAST}>BREAKFAST</MenuItem>
              <MenuItem value={MenuCategory.LUNCH}>LUNCH</MenuItem>
              <MenuItem value={MenuCategory.DINNER}>DINNER</MenuItem>
              <MenuItem value={MenuCategory.SPECIAL}>SPECIAL</MenuItem>
            </Select>
          )}
        />
        {errors.category && <FormHelperText>{errors.category.message}</FormHelperText>}
      </FormControl>
      <Controller
        name="imageUrl"
        control={control}
        render={({ field }) => (
          <TextField {...field} label="Image URL (Optional)" variant="outlined" margin="normal" fullWidth />
        )}
      />
      <FormControlLabel
        control={
          <Controller
            name="isAvailable"
            control={control}
            render={({ field }) => <Switch {...field} checked={field.value} />}
          />
        }
        label="Is Available"
      />
      <Button type="submit" variant="contained" color="primary" disabled={isLoading} sx={{ mt: 2 }}>
        {isLoading ? <CircularProgress size={24} /> : (isEditMode ? 'Update Item' : 'Create Item')}
      </Button>
    </form>
  );
};

export default MenuItemForm;