import { api } from '@/lib/axios';
import type { MealSample } from '@/features/meal/types/meal';

// 랜덤 식단 조회 요청

export async function getMealSamples(): Promise<MealSample[]> {
  const response = await api.get<MealSample[]>('/meals/samples');

  return response.data;
}
