import { useMutation } from '@tanstack/react-query';
import { recommendMeals } from '@/features/meal/api/recommendMeals';

// 식단 추천 훅

export function useRecommendMeals() {
  return useMutation({ mutationFn: recommendMeals });
}
