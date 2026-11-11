// 식단 응답 타입

export type Goal = '다이어트' | '유지' | '벌크업';

export type NutritionSummary = {
  calorie: number | null;
  carbohydrate: number | null;
  protein: number | null;
  fat: number | null;
  carbohydrateRatio: number | null;
  proteinRatio: number | null;
  fatRatio: number | null;
  targetCalorie: number;
  verified: boolean;
  passed: boolean;
  issues: string[];
};

export type MealRecipe = {
  id: number;
  name: string;
  category: string;
  imageUrl: string | null;
  portion: number;
};

export type MealSample = {
  goal: Goal;
  title: string;
  recipes: MealRecipe[];
  nutrition: NutritionSummary | null;
};
