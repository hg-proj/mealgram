import { sortDishes } from '@/features/meal/lib/dishOrder';
import type { MealDish } from '@/features/meal/types/meal';

// 장보기 목록 복사 문구, 집에 있는 재료는 뺌

export function buildShoppingText(dishes: MealDish[], ownedNames: string[]): string {
  const owned = new Set(ownedNames);
  const lines = sortDishes(dishes)
    .map((dish) => ({
      name: dish.name,
      toBuy: (dish.ingredients ?? []).filter((ingredient) => !owned.has(ingredient)),
    }))
    .filter((dish) => dish.toBuy.length > 0)
    .map((dish) => `${dish.name}: ${dish.toBuy.join(', ')}`);

  return ['장보기 목록', '', ...lines].join('\n');
}
