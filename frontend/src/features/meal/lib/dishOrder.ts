import type { MealDish } from '@/features/meal/types/meal';

const ROLE_ORDER = ['일품', '밥', '국', '메인 반찬', '곁들임 반찬', '김치와 절임', '후식'];

function rank(role: string): number {
  const index = ROLE_ORDER.indexOf(role);

  return index === -1 ? ROLE_ORDER.length : index;
}

export function sortDishes(dishes: MealDish[]): MealDish[] {
  return [...dishes].sort((a, b) => rank(a.category) - rank(b.category));
}

export function dishNames(dishes: MealDish[]): string {
  return sortDishes(dishes)
    .map((dish) => dish.name)
    .join(', ');
}

export function representativeDish(dishes: MealDish[]): MealDish | undefined {
  return dishes.find((dish) => dish.imageUrl !== null);
}
