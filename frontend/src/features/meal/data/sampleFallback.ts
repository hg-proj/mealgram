import type { MealDish, MealSample, NutritionSummary } from '@/features/meal/types/meal';

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

function dishes(items: [string, string, number, number][]): MealDish[] {
  return items.map(([name, category, grams, calorie], i) => ({
    id: -(i + 1),
    name,
    category,
    imageUrl: null,
    grams,
    calorie,
  }));
}

export const sampleFallback: MealSample[] = [
  {
    goal: '다이어트',
    title: '가볍게 한 그릇 식단',
    dishes: dishes([
      ['현미밥', '밥', 180, 270],
      ['미역국', '국', 200, 45],
      ['닭가슴살구이', '메인 반찬', 130, 170],
      ['시금치나물', '곁들임 반찬', 60, 35],
      ['애호박볶음', '곁들임 반찬', 70, 45],
      ['배추김치', '김치와 절임', 40, 10],
    ]),
    nutrition: nutrition(658, 62, 18, 20),
  },
  {
    goal: '유지',
    title: '든든한 한 끼 식단',
    dishes: dishes([
      ['잡곡밥', '밥', 210, 320],
      ['콩나물국', '국', 200, 40],
      ['제육볶음', '메인 반찬', 150, 250],
      ['계란찜', '곁들임 반찬', 100, 90],
      ['브로콜리무침', '곁들임 반찬', 60, 35],
      ['배추김치', '김치와 절임', 40, 10],
    ]),
    nutrition: nutrition(742, 55, 25, 20),
  },
  {
    goal: '벌크업',
    title: '근육을 위한 식단',
    dishes: dishes([
      ['쌀밥', '밥', 250, 380],
      ['소고기무국', '국', 220, 80],
      ['닭가슴살스테이크', '메인 반찬', 180, 240],
      ['두부조림', '곁들임 반찬', 90, 110],
      ['숙주나물', '곁들임 반찬', 60, 30],
      ['깍두기', '김치와 절임', 40, 12],
    ]),
    nutrition: nutrition(612, 58, 20, 22),
  },
];
