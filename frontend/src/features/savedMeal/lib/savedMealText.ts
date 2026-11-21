import { dishNames } from '@/features/meal/lib/dishOrder';
import type { SavedMeal } from '@/features/savedMeal/types/savedMeal';

// 저장한 식단 날짜 표시와 검색

export function formatSavedDate(createdAt: string): string {
  return createdAt.slice(0, 10).replaceAll('-', '.');
}

export function matchesKeyword(meal: SavedMeal, keyword: string): boolean {
  const key = keyword.trim().toLowerCase();

  return key === '' || `${meal.style} ${dishNames(meal.dishes)}`.toLowerCase().includes(key);
}
