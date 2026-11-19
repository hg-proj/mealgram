import { useQuery } from '@tanstack/react-query';
import { getIngredientCatalog } from '@/features/meal/api/getIngredientCatalog';
import { mealKeys } from '@/features/meal/api/keys';

// 분류별 재료 목록 조회 훅

export function useIngredientCatalog() {
  return useQuery({
    queryKey: mealKeys.catalog(),
    queryFn: getIngredientCatalog,
    staleTime: Infinity,
  });
}
