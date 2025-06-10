// src/types/menu.ts

// Matches MenuCategory enum from backend
export enum MenuCategory {
  BREAKFAST = 'BREAKFAST',
  LUNCH = 'LUNCH',
  DINNER = 'DINNER',
  SPECIAL = 'SPECIAL',
}

// Matches MenuItemDTO from backend
export interface MenuItemDTO {
  itemId: number; // Assuming Long is number
  name: string;
  description?: string;
  price: number; // Assuming BigDecimal is number
  category: MenuCategory;
  imageUrl?: string;
  isAvailable: boolean;
  createdAt?: string; // Assuming LocalDateTime is string
  updatedAt?: string; // Assuming LocalDateTime is string
}

// Matches MealType enum from backend
export enum MealType {
  BREAKFAST = 'BREAKFAST',
  LUNCH = 'LUNCH',
  DINNER = 'DINNER',
}

// Matches DayOfWeek enum from backend (java.time.DayOfWeek)
// In JS, 0 is Sunday, 1 is Monday... but java.time.DayOfWeek is 1 (Mon) to 7 (Sun)
// For simplicity, we'll use string names as backend likely serializes them as strings.
export type DayOfWeekString = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';


// Matches WeeklyMenuDayMealDTO from backend
export interface WeeklyMenuDayMealDTO {
  items: MenuItemDTO[];
}

export interface WeeklyMenuSetupRequestDTO {
    menuEntries: WeeklyMenuItemEntryDTO[];
}

export interface WeeklyMenuItemEntryDTO {
    itemId: number;
    dayOfWeek: DayOfWeekString;
    mealType: MealType;
    effectiveDateStart: string; // YYYY-MM-DD
    effectiveDateEnd?: string; // Optional
}
// Matches WeeklyMenuResponseDTO from backend
export interface WeeklyMenuResponseDTO {
  // The keys of dailyMenus will be DayOfWeekString
  dailyMenus: Partial<Record<DayOfWeekString, Partial<Record<MealType, WeeklyMenuDayMealDTO>>>>;
  startDate: string; // LocalDate will be string
  endDate: string;   // LocalDate will be string
}