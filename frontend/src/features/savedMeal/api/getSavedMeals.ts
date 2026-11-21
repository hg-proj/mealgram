import type { SavedMeal } from '@/features/savedMeal/types/savedMeal';
import { api } from '@/lib/axios';

export async function getSavedMeals(): Promise<SavedMeal[]> {
  const response = await api.get<SavedMeal[]>('/saved-meals', { params: { size: 100 } });

  return response.data;
}
