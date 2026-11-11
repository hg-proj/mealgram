import { http, HttpResponse } from 'msw';
import type { MealSample } from '@/features/meal/types/meal';

// MSW 기본 응답 핸들러

export const mealSamples: MealSample[] = [
  {
    goal: '다이어트',
    title: '서버 다이어트 식단',
    recipes: [
      {
        id: 1,
        name: '고등어조림',
        category: '반찬',
        imageUrl: '/images/recipes/1.webp',
        portion: 1,
      },
      { id: 2, name: '현미밥', category: '밥', imageUrl: null, portion: 1 },
    ],
    nutrition: {
      calorie: 601.4,
      carbohydrate: 80,
      protein: 30,
      fat: 15,
      carbohydrateRatio: 58.2,
      proteinRatio: 21.1,
      fatRatio: 20.7,
      targetCalorie: 600,
      verified: true,
      passed: true,
      issues: [],
    },
  },
  {
    goal: '유지',
    title: '서버 유지 식단',
    recipes: [
      { id: 3, name: '제육보쌈', category: '반찬', imageUrl: '/images/recipes/3.webp', portion: 1 },
      { id: 4, name: '미역국', category: '국&찌개', imageUrl: null, portion: 1 },
      { id: 5, name: '잡곡밥', category: '밥', imageUrl: null, portion: 1 },
    ],
    nutrition: null,
  },
  {
    goal: '벌크업',
    title: '서버 벌크업 식단',
    recipes: [
      { id: 6, name: '닭갈비', category: '반찬', imageUrl: '/images/recipes/6.webp', portion: 1 },
    ],
    nutrition: null,
  },
];

export const handlers = [http.get('*/api/meals/samples', () => HttpResponse.json(mealSamples))];
