// src/features/admin-dashboard/components/MenuAssignmentModal.tsx
import React, { useEffect, useState } from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Checkbox, List, ListItem, ListItemButton, ListItemText, CircularProgress, Alert } from '@mui/material';
import type { MenuItemDTO, DayOfWeekString, WeeklyMenuItemEntryDTO } from '../../../types/menu';
import { MealType } from '../../../types/menu';
import { menuService } from '../../menu/services/menuService';

interface MenuAssignmentModalProps {
  open: boolean;
  onClose: () => void;
  day: DayOfWeekString;
  mealType: MealType;
  effectiveDate: string; // YYYY-MM-DD
  assignedItemIds: number[]; // IDs of items already assigned to this slot
  onSave: (entries: WeeklyMenuItemEntryDTO[]) => Promise<void>;
}

const MenuAssignmentModal: React.FC<MenuAssignmentModalProps> = ({ open, onClose, day, mealType, effectiveDate, assignedItemIds, onSave }) => {
  const [allItems, setAllItems] = useState<MenuItemDTO[]>([]);
  const [selectedItemIds, setSelectedItemIds] = useState<Set<number>>(new Set(assignedItemIds));
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (open) {
      // Fetch all menu items to populate the selection list
      const fetchAllItems = async () => {
        setIsLoading(true);
        try {
          // Fetch all items - assuming we don't have more than a few hundred.
          // For very large item lists, a searchable/paginated select would be better.
          const itemsPage = await menuService.getAllMenuItems(0, 200); // Fetch up to 200 items
          setAllItems(itemsPage.content);
        } catch (err) {
          setError("Failed to load available menu items.");
        } finally {
          setIsLoading(false);
        }
      };
      fetchAllItems();
      setSelectedItemIds(new Set(assignedItemIds)); // Reset selection on open
    }
  }, [open, assignedItemIds]);

  const handleToggle = (itemId: number) => {
    const newSelection = new Set(selectedItemIds);
    if (newSelection.has(itemId)) {
      newSelection.delete(itemId);
    } else {
      newSelection.add(itemId);
    }
    setSelectedItemIds(newSelection);
  };

  const handleSave = () => {
    const entries: WeeklyMenuItemEntryDTO[] = Array.from(selectedItemIds).map(itemId => ({
      itemId,
      dayOfWeek: day,
      mealType,
      effectiveDateStart: effectiveDate,
    }));
    onSave(entries);
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Assign Menu for {day} - {mealType}</DialogTitle>
      <DialogContent>
        {isLoading && <CircularProgress />}
        {error && <Alert severity="error">{error}</Alert>}
        {!isLoading && !error && (
          <List>
            {allItems.map(item => (
              <ListItem key={item.itemId} disablePadding>
                <ListItemButton onClick={() => handleToggle(item.itemId)}>
                  <Checkbox
                    edge="start"
                    checked={selectedItemIds.has(item.itemId)}
                    tabIndex={-1}
                    disableRipple
                  />
                  <ListItemText primary={item.name} secondary={`₹${item.price}`} />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button onClick={handleSave} variant="contained">Save Changes</Button>
      </DialogActions>
    </Dialog>
  );
};

export default MenuAssignmentModal;