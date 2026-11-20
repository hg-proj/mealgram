import { http, HttpResponse } from 'msw';
import type { MealSample } from '@/features/meal/types/meal';

// MSW 기본 응답 핸들러

export const mealSamples: MealSample[] = [
  {
    goal: '다이어트',
    title: '서버 다이어트 식단',
    dishes: [
      {
        id: 1,
        name: '고등어조림',
        category: '메인 반찬',
        imageUrl: '/images/dishes/g01.webp',
        grams: 100,
        calorie: 100,
      },
      { id: 2, name: '현미밥', category: '밥', imageUrl: null, grams: 100, calorie: 100 },
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
    id: 'sample-keep',
    goal: '유지',
    title: '서버 유지 식단',
    dishes: [
      {
        id: 3,
        name: '제육보쌈',
        category: '메인 반찬',
        imageUrl: '/images/dishes/g03.webp',
        grams: 100,
        calorie: 100,
      },
      { id: 4, name: '미역국', category: '국', imageUrl: null, grams: 100, calorie: 100 },
      { id: 5, name: '잡곡밥', category: '밥', imageUrl: null, grams: 100, calorie: 100 },
    ],
    nutrition: null,
  },
  {
    goal: '벌크업',
    title: '서버 벌크업 식단',
    dishes: [
      {
        id: 6,
        name: '닭갈비',
        category: '메인 반찬',
        imageUrl: '/images/dishes/g06.webp',
        grams: 100,
        calorie: 100,
      },
    ],
    nutrition: null,
  },
];

export const handlers = [http.get('*/api/meals/samples', () => HttpResponse.json(mealSamples))];
