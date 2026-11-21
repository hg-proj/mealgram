import { Link } from 'react-router';
import { paths } from '@/config/paths';
import { useSaveCandidate } from '@/features/meal/api/useSaveCandidate';
import { contextBadge } from '@/features/meal/lib/contextBadge';
import { dishNames, representativeDish } from '@/features/meal/lib/dishOrder';
import type { MealCandidate, MealContext } from '@/features/meal/types/meal';
import { percent } from '@/features/meal/lib/format';

// 추천 식단 한 개 카드

type Props = {
  candidate: MealCandidate;
  index: number;
  mealId: string | null;
  context: MealContext | null;
};

export default function MealCandidateCard({ candidate, index, mealId, context }: Props) {
  const { nutrition } = candidate;
  const photo = representativeDish(candidate.dishes);
  const save = useSaveCandidate(mealId, index, candidate.goal, candidate.genre);
  const badge = contextBadge(context, candidate.goal, candidate.genre);

  return (
    <article className="flex flex-col border border-line bg-white max-[620px]:grid max-[620px]:grid-cols-[132px_1fr]">
      <div className="relative max-[620px]:min-h-[150px]">
        {photo ? (
          <img
            src={photo.imageUrl ?? undefined}
            alt={photo.name}
            className="aspect-[4/3] w-full object-cover max-[620px]:absolute max-[620px]:inset-0 max-[620px]:h-full max-[620px]:aspect-auto"
          />
        ) : (
          <div className="aspect-[4/3] w-full bg-track max-[620px]:absolute max-[620px]:inset-0 max-[620px]:h-full max-[620px]:aspect-auto" />
        )}
        {badge !== null && (
          <span className="absolute top-1.5 left-1.5 rounded-[3px] bg-green px-2.5 py-[5px] text-[11.5px] font-bold whitespace-nowrap text-white max-[620px]:hidden">
            {badge}
          </span>
        )}
        <div className="absolute right-0 bottom-0 border-t border-l border-line bg-white px-[11px] py-1.5 text-[17px] leading-[1.1] font-extrabold tracking-[-0.03em] whitespace-nowrap text-green-d tabular-nums max-[620px]:px-2 max-[620px]:py-1 max-[620px]:text-[15px]">
          {nutrition.calorie == null ? '-' : Math.round(nutrition.calorie)}
          <small className="ml-0.5 text-[0.55em] font-medium">kcal</small>
        </div>
      </div>
      <div className="flex flex-1 flex-col px-[18px] pt-4 max-[620px]:px-3.5 max-[620px]:pt-3">
        <h2 className="font-heading text-lg font-extrabold tracking-[-0.03em] max-[620px]:text-base">
          {candidate.style}
        </h2>
        {badge !== null && (
          <p className="mt-0.5 hidden text-xs text-sub max-[620px]:block">{badge}</p>
        )}
        <p className="mt-1.5 mb-2 text-[13px] leading-[1.65] max-[620px]:text-[12.5px] max-[620px]:leading-[1.55]">
          {dishNames(candidate.dishes)}
        </p>
        <p className="mb-3.5 flex flex-wrap gap-x-4 gap-y-0.5 text-[13px] text-sub tabular-nums max-[620px]:mb-3 max-[620px]:gap-x-3 max-[620px]:text-xs">
          <span>탄수화물 {percent(nutrition.carbohydrateRatio)}</span>
          <span>단백질 {percent(nutrition.proteinRatio)}</span>
          <span>지방 {percent(nutrition.fatRatio)}</span>
        </p>
      </div>
      <div className="px-[18px] pb-[18px] max-[620px]:col-span-2 max-[620px]:px-3.5 max-[620px]:pt-3.5 max-[620px]:pb-3.5">
        <div className="grid grid-cols-2 gap-2">
          <Link
            to={paths.mealDetail.replace(':index', String(index))}
            className="inline-flex h-11 items-center justify-center rounded-[3px] border border-green bg-green text-sm font-bold text-white max-[620px]:h-10"
          >
            상세, 장보기 목록
          </Link>
          <button
            type="button"
            onClick={save.onSave}
            disabled={!save.canSave || save.saved || save.pending}
            className="h-11 rounded-[3px] border border-green bg-white text-sm font-bold text-green disabled:border-rule disabled:text-disabled max-[620px]:h-10"
          >
            {save.saved ? '식단 저장 완료' : '내 식단에 저장'}
          </button>
        </div>
        {save.error !== '' && (
          <p role="alert" className="mt-2 text-xs text-danger">
            {save.error}
          </p>
        )}
      </div>
    </article>
  );
}
