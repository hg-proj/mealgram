import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteSavedMeal } from '@/features/savedMeal/api/deleteSavedMeal';
import { savedMealKeys } from '@/features/savedMeal/api/keys';

export function useDeleteSavedMeal() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: deleteSavedMeal,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: savedMealKeys.all }),
  });
}
