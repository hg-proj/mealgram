import { api } from '@/lib/axios';

// 저장한 식단 삭제 요청

export async function deleteSavedMeal(id: number): Promise<void> {
  await api.delete(`/saved-meals/${id}`);
}
