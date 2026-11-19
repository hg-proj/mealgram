import type { Goal } from '@/features/meal/types/meal';

// 식단 추천 선택지

export const goals: Goal[] = ['다이어트', '유지', '벌크업'];

export const genres = ['한식', '양식'] as const;

export const styles = ['균형', '고단백', '저탄수', '저지방'] as const;

export const DEFAULT_STYLE = '균형';

export const RANDOM_GOAL = '랜덤';

export const RANDOM_GENRE = '랜덤';

export const activityOptions = [
  { value: 'LOW', label: '비활동적', description: '주로 앉아서 생활' },
  { value: 'LIGHT', label: '저활동적', description: '일상적인 걷기 정도' },
  { value: 'MODERATE', label: '활동적', description: '주 3~5회 운동' },
  { value: 'HIGH', label: '매우 활동적', description: '거의 매일 운동하거나 육체노동' },
];
