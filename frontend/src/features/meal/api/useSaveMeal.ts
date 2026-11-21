import { useMutation } from '@tanstack/react-query';
import { saveMeal } from '@/features/meal/api/saveMeal';

// 식단 저장 훅

export function useSaveMeal() {
  return useMutation({ mutationFn: saveMeal });
}
