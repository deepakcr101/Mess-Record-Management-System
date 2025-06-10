// src/features/menu/components/WeeklyMenuDisplay.tsx
import React, { useEffect, useState } from 'react';
import { menuService } from '../services/menuService';
import type { WeeklyMenuResponseDTO, MenuItemDTO, DayOfWeekString } from '../../../types/menu';
import { MealType } from '../../../types/menu';

// Define the order of days and meal types for display
const DISPLAY_DAYS_ORDER: DayOfWeekString[] = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
const DISPLAY_MEAL_TYPES_ORDER: MealType[] = [MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER];

const WeeklyMenuDisplay: React.FC = () => {
  const [menuData, setMenuData] = useState<WeeklyMenuResponseDTO | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchMenu = async () => {
      setIsLoading(true);
      setError(null);
      try {
        // Fetch for the current date's week by default
        const today = new Date().toISOString().split('T')[0]; // YYYY-MM-DD format
        const data = await menuService.getWeeklyMenu({ date: today });
        setMenuData(data);
      } catch (err: any) {
        setError(err.message || 'Failed to load weekly menu.');
        console.error("Error fetching weekly menu:", err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchMenu();
  }, []); // Empty dependency array means this runs once on component mount

  if (isLoading) {
    return <p>Loading weekly menu...</p>;
  }

  if (error) {
    return <p style={{ color: 'red' }}>Error: {error}</p>;
  }

  if (!menuData || !menuData.dailyMenus) {
    return <p>No menu data available for the selected period.</p>;
  }

  // Basic styling (replace with better CSS or UI library later)
  const dayStyle: React.CSSProperties = { border: '1px solid #eee', padding: '10px', marginBottom: '15px', borderRadius: '5px' };
  const mealTypeStyle: React.CSSProperties = { marginTop: '10px', paddingLeft: '15px' };
  const itemStyle: React.CSSProperties = { border: '1px dashed #ddd', padding: '8px', margin: '5px 0', borderRadius: '4px', background: '#f9f9f9' };

  return (
    <div>
      <h3>Weekly Menu ({menuData.startDate} to {menuData.endDate})</h3>
      {DISPLAY_DAYS_ORDER.map(day => (
        <div key={day} style={dayStyle}>
          <h4>{day.charAt(0).toUpperCase() + day.slice(1).toLowerCase()}</h4>
          {menuData.dailyMenus[day] ? (
            DISPLAY_MEAL_TYPES_ORDER.map(mealType => (
              menuData.dailyMenus[day]?.[mealType] && menuData.dailyMenus[day]![mealType]!.items.length > 0 ? (
                <div key={mealType} style={mealTypeStyle}>
                  <h5>{mealType.charAt(0).toUpperCase() + mealType.slice(1).toLowerCase()}</h5>
                  {menuData.dailyMenus[day]![mealType]!.items.map((item: MenuItemDTO) => (
                    <div key={item.itemId} style={itemStyle}>
                      <strong>{item.name}</strong> (₹{item.price})
                      {item.description && <p style={{ fontSize: '0.9em', margin: '4px 0 0' }}><em>{item.description}</em></p>}
                      {/* TODO: Add "Buy Now" button for individual purchase later */}
                    </div>
                  ))}
                </div>
              ) : null // Don't render meal type if no items
            ))
          ) : (
            <p>No menu set for this day.</p>
          )}
        </div>
      ))}
    </div>
  );
};

export default WeeklyMenuDisplay;