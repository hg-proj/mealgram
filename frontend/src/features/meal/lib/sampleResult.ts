import { DEFAULT_STYLE } from '@/features/meal/data/options';
import { resultStore } from '@/features/meal/lib/resultStore';
import type { MealSample, NutritionSummary } from '@/features/meal/types/meal';

const EMPTY_NUTRITION: NutritionSummary = {
  calorie: null,
  carbohydrate: null,
  protein: null,
  fat: null,
  carbohydrateRatio: null,
  proteinRatio: null,
  fatRatio: null,
  targetCalorie: 0,
  verified: false,
  passed: false,
  issues: [],
};

export function saveSampleResult(sample: MealSample) {
  resultStore.save({
    id: sample.id ?? null,
    relaxed: false,
    sample: true,
    candidates: [
      {
        style: sample.title,
        dishes: sample.dishes,
        nutrition: sample.nutrition ?? EMPTY_NUTRITION,
        goal: sample.goal,
        genre: sample.genre,
      },
    ],
  });
  resultStore.saveContext({
    goal: sample.goal,
    style: DEFAULT_STYLE,
    genre: null,
    requiredName: null,
    ownedNames: [],
  });
}
