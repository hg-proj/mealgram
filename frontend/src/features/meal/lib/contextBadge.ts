import type { MealContext } from '@/features/meal/types/meal';

// 추천 조건 배지 문구

export function contextBadge(
  context: MealContext | null,
  goal?: string | null,
  genre?: string | null,
): string | null {
  return context === null
    ? null
    : [goal ?? context.goal, `${context.style} 식단`, genre ?? context.genre]
        .filter(Boolean)
        .join(', ');
}
