import { useState } from 'react';
import { messages } from '@/config/messages';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { useSaveMeal } from '@/features/meal/api/useSaveMeal';
import { resultStore } from '@/features/meal/lib/resultStore';
import { errorMessage } from '@/lib/apiError';

// 추천 식단 한 개 저장 상태

export function useSaveCandidate(
  mealId: string | null,
  index: number,
  goal?: string | null,
  genre?: string | null,
) {
  const { isLoggedIn } = useAuth();
  const save = useSaveMeal();
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState('');

  const onSave = async () => {
    if (mealId === null) {
      return;
    }
    setError('');

    try {
      const context = resultStore.readContext();
      await save.mutateAsync({
        mealId,
        candidateIndex: index,
        context:
          context === null
            ? null
            : { ...context, goal: goal ?? context.goal, genre: genre ?? context.genre },
      });
      setSaved(true);
    } catch (e) {
      const message = errorMessage(e, messages.saveMealFailed);
      if (message.includes('이미 저장')) {
        setSaved(true);
      } else {
        setError(message);
      }
    }
  };

  return {
    saved,
    error,
    pending: save.isPending,
    canSave: isLoggedIn && mealId !== null,
    onSave,
  };
}
