import { describe, expect, it } from 'vitest';
import { buildShoppingText } from '@/features/meal/lib/shoppingText';
import type { MealDish } from '@/features/meal/types/meal';

function dish(name: string, category: string, ingredients: string[]): MealDish {
  return { id: 1, name, category, imageUrl: null, grams: 100, calorie: 100, ingredients };
}

describe('buildShoppingText', () => {
  it('요리별 재료를 한 줄씩 적고 집에 있는 재료는 뺀다.', () => {
    const text = buildShoppingText(
      [
        dish('오리주물럭', '메인 반찬', ['오리고기', '양파', '깻잎']),
        dish('혼합미밥', '밥', ['쌀', '잡곡']),
      ],
      ['쌀', '양파'],
    );

    expect(text).toBe('장보기 목록\n\n혼합미밥: 잡곡\n오리주물럭: 오리고기, 깻잎');
  });

  it('재료가 모두 집에 있는 요리는 줄에서 뺀다.', () => {
    const text = buildShoppingText([dish('콩나물국', '국', ['콩나물'])], ['콩나물']);

    expect(text).toBe('장보기 목록\n');
  });

  it('재료 정보가 없는 요리는 줄에서 뺀다.', () => {
    const text = buildShoppingText([{ ...dish('한라봉', '후식', []), ingredients: undefined }], []);

    expect(text).toBe('장보기 목록\n');
  });
});
