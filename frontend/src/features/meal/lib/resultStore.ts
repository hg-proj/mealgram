import type { MealContext, MealResult } from '@/features/meal/types/meal';

// 마지막 추천 결과 보관, 새로고침과 뒤로가기에도 유지

const KEY = 'mealResult';
const CONTEXT_KEY = 'mealContext';

// 예전 형식이나 깨진 값이 저장돼 있으면 화면이 깨지지 않게 버림
function isMealResult(value: unknown): value is MealResult {
  return (
    typeof value === 'object' &&
    value !== null &&
    Array.isArray((value as { candidates?: unknown }).candidates)
  );
}

export const resultStore = {
  save(result: MealResult) {
    try {
      sessionStorage.setItem(KEY, JSON.stringify(result));
    } catch {
      // 저장소 사용 불가
    }
  },
  read(): MealResult | null {
    try {
      const raw = sessionStorage.getItem(KEY);

      const parsed: unknown = raw === null ? null : JSON.parse(raw);

      return isMealResult(parsed) ? parsed : null;
    } catch {
      return null;
    }
  },
  saveContext(context: MealContext) {
    try {
      sessionStorage.setItem(CONTEXT_KEY, JSON.stringify(context));
    } catch {
      // 저장소 사용 불가
    }
  },
  readContext(): MealContext | null {
    try {
      const raw = sessionStorage.getItem(CONTEXT_KEY);

      const parsed: unknown = raw === null ? null : JSON.parse(raw);

      return typeof parsed === 'object' && parsed !== null ? (parsed as MealContext) : null;
    } catch {
      return null;
    }
  },
};
