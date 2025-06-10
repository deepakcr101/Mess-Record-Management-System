// src/features/admin-dashboard/components/WeeklyMenuSetup.tsx
import React, { useState, useEffect } from 'react';
import { Box, CircularProgress, Alert, Paper, Grid, Typography, IconButton } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import { menuService } from '../../menu/services/menuService';
import type { WeeklyMenuResponseDTO, DayOfWeekString } from '../../../types/menu';
import { MealType } from '../../../types/menu';
import MenuAssignmentModal from './MenuAssignmentModal';

const DISPLAY_DAYS_ORDER: DayOfWeekString[] = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
const DISPLAY_MEAL_TYPES_ORDER: MealType[] = [MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER];

const WeeklyMenuSetup: React.FC = () => {
    const [weekStartDate, setWeekStartDate] = useState(new Date());
    const [menuData, setMenuData] = useState<WeeklyMenuResponseDTO | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Modal State
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalData, setModalData] = useState<{ day: DayOfWeekString, mealType: MealType, assignedItemIds: number[] } | null>(null);

    const fetchWeeklyMenu = async (date: Date) => {
        setIsLoading(true);
        try {
            const data = await menuService.getWeeklyMenu({ date: date.toISOString().split('T')[0] });
            setMenuData(data);
        } catch (err: any) {
            setError(err.message || "Failed to fetch menu data.");
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchWeeklyMenu(weekStartDate);
    }, [weekStartDate]);

    const handleOpenModal = (day: DayOfWeekString, mealType: MealType) => {
        const assignedItems = menuData?.dailyMenus[day]?.[mealType]?.items || [];
        setModalData({
            day,
            mealType,
            assignedItemIds: assignedItems.map(item => item.itemId),
        });
        setIsModalOpen(true);
    };

    const handleSaveMenuAssignment = async (entries: any[]) => {
        // Note: This replaces all items for the given slots with the new selection.
        // A more complex UI might allow adding/removing single items.
        try {
            // Here we would construct the WeeklyMenuSetupRequestDTO
            // A more robust implementation might first delete existing entries for these slots before saving new ones.
            await menuService.setupWeeklyMenu({ menuEntries: entries });
            alert('Menu updated successfully!');
            setIsModalOpen(false);
            fetchWeeklyMenu(weekStartDate); // Refresh the view
        } catch (err: any) {
            alert(`Error saving menu: ${err.message}`);
        }
    };

    return (
        <Box sx={{ width: '100%', marginTop: 4 }}>
            <h3>Weekly Menu Setup</h3>
            {/* TODO: Add a date picker to select the week */}
            <Typography variant="h6" gutterBottom>
                Week of: {menuData?.startDate}
            </Typography>

            {isLoading && <CircularProgress />}
            {error && <Alert severity="error">{error}</Alert>}

            <Grid container spacing={2}>
                {DISPLAY_DAYS_ORDER.map(day => (
                    <Grid item xs={12} md={4} lg={3} key={day}>
                        <Paper sx={{ p: 2 }}>
                            <Typography variant="h6" align="center">{day}</Typography>
                            {DISPLAY_MEAL_TYPES_ORDER.map(mealType => (
                                <Box key={mealType} sx={{ mt: 2, p: 1, border: '1px solid #e0e0e0', borderRadius: 1 }}>
                                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <Typography variant="subtitle1">{mealType}</Typography>
                                        <IconButton size="small" onClick={() => handleOpenModal(day, mealType)}>
                                            <EditIcon />
                                        </IconButton>
                                    </Box>
                                    <Box sx={{ pl: 1, fontSize: '0.9em' }}>
                                        {(menuData?.dailyMenus[day]?.[mealType]?.items || []).map(item => (
                                            <div key={item.itemId}>- {item.name}</div>
                                        ))}
                                    </Box>
                                </Box>
                            ))}
                        </Paper>
                    </Grid>
                ))}
            </Grid>

            {modalData && (
                <MenuAssignmentModal
                    open={isModalOpen}
                    onClose={() => setIsModalOpen(false)}
                    day={modalData.day}
                    mealType={modalData.mealType}
                    assignedItemIds={modalData.assignedItemIds}
                    effectiveDate={menuData?.startDate || new Date().toISOString().split('T')[0]} // Use the start date of the week
                    onSave={handleSaveMenuAssignment}
                />
            )}
        </Box>
    );
};

export default WeeklyMenuSetup;