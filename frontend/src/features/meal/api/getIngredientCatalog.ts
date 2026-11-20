import type { IngredientCategory } from '@/features/meal/types/ingredient';
import { api } from '@/lib/axios';

// 분류별 재료 목록 조회 요청

export async function getIngredientCatalog(): Promise<IngredientCategory[]> {
  const response = await api.get<IngredientCategory[]>('/ingredients/catalog');

  return response.data;
}
