import { api } from '@/lib/axios';

export type MyIngredient = { id: number; ingredientId: number; name: string };

export async function getMyIngredients(): Promise<MyIngredient[]> {
  const response = await api.get<MyIngredient[]>('/members/me/ingredients', {
    params: { size: 100 },
  });

  return response.data;
}

export async function addMyIngredient(ingredientId: number): Promise<void> {
  await api.post('/members/me/ingredients', { ingredientId });
}

export async function deleteMyIngredient(id: number): Promise<void> {
  await api.delete(`/members/me/ingredients/${id}`);
}
