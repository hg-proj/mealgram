import { useEffect, useState } from 'react';
import { sortDishes } from '@/features/meal/lib/dishOrder';
import { buildShoppingText } from '@/features/meal/lib/shoppingText';
import type { MealDish } from '@/features/meal/types/meal';

// 요리별 장보기 목록 상자

type Props = { dishes: MealDish[]; requiredName: string | null; ownedNames: string[] };

export default function ShoppingBox({ dishes, requiredName, ownedNames }: Props) {
  const [copied, setCopied] = useState(false);
  const owned = new Set(ownedNames);

  useEffect(() => {
    if (!copied) {
      return;
    }
    const timer = setTimeout(() => setCopied(false), 2000);

    return () => clearTimeout(timer);
  }, [copied]);

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(buildShoppingText(dishes, ownedNames));
      setCopied(true);
    } catch {
      // 클립보드 사용 불가
    }
  };

  return (
    <section className="mt-7 border border-line">
      <header className="flex items-center justify-between border-b border-line bg-green-pale px-5 py-3 max-[480px]:px-4">
        <h2 className="font-heading text-base font-extrabold tracking-[-0.03em]">장보기 목록</h2>
        <button
          type="button"
          onClick={copy}
          className="h-[38px] w-[136px] rounded-[3px] border border-green bg-white text-[13.5px] font-bold text-green"
        >
          {copied ? '목록 복사 완료' : '장보기 목록 복사'}
        </button>
      </header>
      <ul className="grid grid-cols-2 max-[980px]:grid-cols-1">
        {sortDishes(dishes).map((dish) => (
          <li
            key={dish.id}
            className="grid grid-cols-[120px_1fr] items-baseline gap-2.5 border-b border-line px-5 py-[13px] max-[480px]:grid-cols-1 max-[480px]:gap-0.5 max-[480px]:px-4 max-[480px]:py-2.5 min-[981px]:odd:border-r"
          >
            <b className="text-[14.5px]">{dish.name}</b>
            <span className="text-sm leading-[1.6]">
              {(dish.ingredients ?? []).map((ingredient, i) => (
                <span key={ingredient}>
                  {i > 0 && ', '}
                  <span
                    className={[
                      owned.has(ingredient) ? 'text-disabled line-through' : '',
                      ingredient === requiredName ? 'font-extrabold' : '',
                    ].join(' ')}
                  >
                    {ingredient}
                  </span>
                </span>
              ))}
            </span>
          </li>
        ))}
      </ul>
      {ownedNames.length > 0 && (
        <p className="px-5 py-3 text-[13px] text-disabled max-[480px]:px-4">
          집에 있는 재료: {ownedNames.join(', ')}
        </p>
      )}
    </section>
  );
}
