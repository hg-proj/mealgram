import type { MealContext, MealDish, NutritionSummary } from '@/features/meal/types/meal';

// 저장한 식단 응답 타입

export type SavedMeal = {
  id: number;
  style: string;
  dishes: MealDish[];
  nutrition: NutritionSummary;
  context: MealContext | null;
  createdAt: string;
};
