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

export type MealDish = {
  id: number;
  name: string;
  category: string;
  imageUrl: string | null;
  grams: number;
  calorie: number;
  ingredients?: string[];
  seasonings?: string[];
};

export type MealCandidate = {
  style: string;
  dishes: MealDish[];
  nutrition: NutritionSummary;
  goal?: string | null;
  genre?: string | null;
};

export type MealResult = {
  id: string | null;
  relaxed: boolean;
  candidates: MealCandidate[];
  sample?: boolean;
};

export type MealSample = {
  id?: string | null;
  goal: Goal;
  genre?: string | null;
  title: string;
  dishes: MealDish[];
  nutrition: NutritionSummary | null;
};

export type MealContext = {
  goal: string;
  style: string;
  genre: string | null;
  requiredName: string | null;
  ownedNames: string[];
};
