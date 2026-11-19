import type { BodyInfoRequest } from '@/features/meal/schemas/bodyInfoSchema';
import type { MealResult } from '@/features/meal/types/meal';
import { api } from '@/lib/axios';

// 식단 추천 요청

export type RecommendRequest = BodyInfoRequest & {
  requiredId?: number;
  subIds: number[];
  goal: string;
  genre?: string;
  style?: string;
};

export async function recommendMeals(request: RecommendRequest): Promise<MealResult> {
  const response = await api.post<MealResult>('/meals', request);

  return response.data;
}
