import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  addMyIngredient,
  deleteMyIngredient,
  type MyIngredient,
} from '@/features/meal/api/myIngredients';
import { memberKeys } from '@/features/member/api/keys';

type Input = { saved: MyIngredient[]; picked: number[] };

export function useSaveMyIngredients() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ saved, picked }: Input) => {
      const savedIds = new Set(saved.map((item) => item.ingredientId));
      const added = picked.filter((id) => !savedIds.has(id));
      const removed = saved.filter((item) => !picked.includes(item.ingredientId));

      await Promise.all([
        ...added.map(addMyIngredient),
        ...removed.map((item) => deleteMyIngredient(item.id)),
      ]);
    },
    onSettled: () => queryClient.invalidateQueries({ queryKey: memberKeys.ingredients }),
  });
}
