import { useState } from 'react';
import { messages } from '@/config/messages';
import { useDeleteSavedMeal } from '@/features/savedMeal/api/useDeleteSavedMeal';
import type { SavedMeal } from '@/features/savedMeal/types/savedMeal';
import { errorMessage } from '@/lib/apiError';

// 삭제 확인 창의 대상

export function useDeleteMealDialog(onDeleted?: () => void) {
  const remove = useDeleteSavedMeal();
  const [target, setTarget] = useState<SavedMeal | null>(null);
  const [error, setError] = useState('');

  const close = () => {
    setTarget(null);
    setError('');
  };

  const confirm = async () => {
    if (target === null) {
      return;
    }
    setError('');

    try {
      await remove.mutateAsync(target.id);
      close();
      onDeleted?.();
    } catch (e) {
      setError(errorMessage(e, messages.deleteMealFailed));
    }
  };

  return { target, error, pending: remove.isPending, ask: setTarget, close, confirm };
}
