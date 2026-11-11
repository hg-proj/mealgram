// 식단 쿼리 키

export const mealKeys = {
  all: ['meals'] as const,
  samples: () => [...mealKeys.all, 'samples'] as const,
};
