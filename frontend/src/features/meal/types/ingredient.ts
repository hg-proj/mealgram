// 재료 응답 타입

export type Ingredient = { id: number; name: string };

export type IngredientCategory = { category: string; ingredients: Ingredient[] };

export type PickedIngredients = { requiredId: number | null; subIds: number[] };
