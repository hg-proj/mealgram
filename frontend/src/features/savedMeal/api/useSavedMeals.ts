import { useQuery } from '@tanstack/react-query';
import { getSavedMeals } from '@/features/savedMeal/api/getSavedMeals';
import { savedMealKeys } from '@/features/savedMeal/api/keys';

// 저장한 식단 목록 조회 훅

export function useSavedMeals() {
  return useQuery({ queryKey: savedMealKeys.all, queryFn: getSavedMeals });
}
