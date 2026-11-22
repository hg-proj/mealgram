import { useQuery } from '@tanstack/react-query';
import { getMyIngredients } from '@/features/meal/api/myIngredients';
import { memberKeys } from '@/features/member/api/keys';

// 내 재료 조회 훅

export function useMyIngredients() {
  return useQuery({
    queryKey: memberKeys.ingredients,
    queryFn: getMyIngredients,
  });
}
