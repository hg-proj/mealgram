import type { MealContext } from '@/features/meal/types/meal';
import { api } from '@/lib/axios';

// 추천받은 식단 저장 요청

export type SaveMealRequest = {
  mealId: string;
  candidateIndex: number;
  context?: MealContext | null;
};

export async function saveMeal(request: SaveMealRequest): Promise<{ id: number }> {
  const response = await api.post<{ id: number }>('/saved-meals', request);

  return response.data;
}
