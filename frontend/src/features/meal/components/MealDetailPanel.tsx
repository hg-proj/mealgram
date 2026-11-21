import type { ReactNode } from 'react';
import { representativeDish, sortDishes } from '@/features/meal/lib/dishOrder';
import type { MealCandidate } from '@/features/meal/types/meal';
import { percent } from '@/features/meal/lib/format';

// 식단 상세 위쪽, 사진과 식단 구성과 영양 비율

const SEGMENTS = [
  { label: '탄수화물', color: 'bg-green-d', key: 'carbohydrateRatio' },
  { label: '단백질', color: 'bg-[#5d9a58]', key: 'proteinRatio' },
  { label: '지방', color: 'bg-[#9cc497]', key: 'fatRatio' },
] as const;

type Props = {
  candidate: Pick<MealCandidate, 'style' | 'dishes' | 'nutrition'>;
  badge: string | null;
  action: ReactNode;
};

export default function MealDetailPanel({ candidate, badge, action }: Props) {
  const { nutrition } = candidate;
  const photo = representativeDish(candidate.dishes);

  return (
    <section className="grid grid-cols-[250px_1fr_210px] border border-line max-[980px]:grid-cols-1">
      {photo ? (
        <img
          src={photo.imageUrl ?? undefined}
          alt={photo.name}
          className="h-full min-h-[220px] w-full object-cover max-[980px]:h-[190px] max-[980px]:min-h-0"
        />
      ) : (
        <div className="min-h-[220px] bg-track max-[980px]:h-[190px] max-[980px]:min-h-0" />
      )}
      <div className="border-x border-line px-6 py-5 max-[980px]:border-x-0 max-[980px]:border-y max-[480px]:px-4">
        <h1 className="font-heading text-xl font-extrabold tracking-[-0.03em]">
          {candidate.style}
        </h1>
        {badge !== null && <p className="mt-0.5 mb-3 text-[13px] text-sub">{badge}</p>}
        <ul className="grid grid-cols-2 gap-x-7 max-[480px]:gap-x-4">
          {sortDishes(candidate.dishes).map((dish) => (
            <li key={dish.id} className="flex items-baseline justify-between gap-2 py-[7px]">
              <b className="text-[15px]">{dish.name}</b>
              <span className="text-[12.5px] text-sub tabular-nums">{Math.round(dish.grams)}g</span>
            </li>
          ))}
        </ul>
      </div>
      <div className="flex flex-col px-[22px] py-5 max-[480px]:px-4">
        <span className="text-[12.5px] text-sub">한 끼 열량</span>
        <b className="text-4xl leading-[1.15] font-extrabold tracking-[-0.03em] text-green-d tabular-nums">
          {nutrition.calorie == null ? '-' : Math.round(nutrition.calorie)}
          <small className="ml-0.5 text-[0.55em] font-medium">kcal</small>
        </b>
        <div className="mt-4 mb-3 flex h-2 gap-0.5">
          {SEGMENTS.map((segment) => (
            <i
              key={segment.key}
              className={`block h-full ${segment.color}`}
              style={{ width: `${nutrition[segment.key] ?? 0}%` }}
            />
          ))}
        </div>
        <ul className="mb-5 grid gap-1.5">
          {SEGMENTS.map((segment) => (
            <li key={segment.key} className="flex items-center gap-2 text-[13px]">
              <i className={`block size-2 ${segment.color}`} />
              <span className="flex-1 text-sub">{segment.label}</span>
              <b className="tabular-nums">{percent(nutrition[segment.key])}</b>
            </li>
          ))}
        </ul>
        <div className="mt-auto">{action}</div>
      </div>
    </section>
  );
}
