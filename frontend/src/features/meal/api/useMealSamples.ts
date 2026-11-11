import { useQuery } from '@tanstack/react-query';
import { getMealSamples } from '@/features/meal/api/getMealSamples';
import { mealKeys } from '@/features/meal/api/keys';

// 랜덤 식단 조회 훅

export function useMealSamples() {
  return useQuery({
    queryKey: mealKeys.samples(),
    queryFn: getMealSamples,
    staleTime: 0,
    gcTime: 0,
  });
}
