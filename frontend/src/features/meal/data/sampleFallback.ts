import type { MealRecipe, MealSample, NutritionSummary } from '@/features/meal/types/meal';

// 랜덤 식단을 불러오지 못할 때 쓰는 예시 식단

function nutrition(
  calorie: number,
  carbohydrate: number,
  protein: number,
  fat: number,
): NutritionSummary {
  return {
    calorie,
    carbohydrate: null,
    protein: null,
    fat: null,
    carbohydrateRatio: carbohydrate,
    proteinRatio: protein,
    fatRatio: fat,
    targetCalorie: calorie,
    verified: true,
    passed: true,
    issues: [],
  };
}

function recipes(mainId: number, mainName: string, others: [string, string][]): MealRecipe[] {
  const main: MealRecipe = {
    id: mainId,
    name: mainName,
    category: '반찬',
    imageUrl: `/images/recipes/${mainId}.webp`,
    portion: 1,
  };

  return [
    main,
    ...others.map(([name, category], i) => ({
      id: -(mainId * 10 + i),
      name,
      category,
      imageUrl: null,
      portion: 1,
    })),
  ];
}

export const sampleFallback: MealSample[] = [
  {
    goal: '다이어트',
    title: '가볍게 한 그릇 식단',
    recipes: recipes(350, '월남쌈', [
      ['밥', '밥'],
      ['된장국', '국&찌개'],
      ['닭가슴살볶음', '반찬'],
      ['시금치나물', '반찬'],
    ]),
    nutrition: nutrition(658, 62, 18, 20),
  },
  {
    goal: '유지',
    title: '든든한 한 끼 식단',
    recipes: recipes(407, '돼지불고기', [
      ['잡곡밥', '밥'],
      ['김치찌개', '국&찌개'],
      ['브로콜리무침', '반찬'],
      ['계란찜', '반찬'],
    ]),
    nutrition: nutrition(742, 55, 25, 20),
  },
  {
    goal: '벌크업',
    title: '근육을 위한 식단',
    recipes: recipes(346, '안심스테이크', [
      ['현미밥', '밥'],
      ['우삼겹국', '국&찌개'],
      ['숙주나물', '반찬'],
      ['두부조림', '반찬'],
    ]),
    nutrition: nutrition(612, 58, 20, 22),
  },
];
