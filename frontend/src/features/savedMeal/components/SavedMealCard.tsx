import { Link } from 'react-router';
import Highlight from '@/components/ui/Highlight';
import { paths } from '@/config/paths';
import { contextBadge } from '@/features/meal/lib/contextBadge';
import { dishNames, representativeDish } from '@/features/meal/lib/dishOrder';
import { percent } from '@/features/meal/lib/format';
import { formatSavedDate } from '@/features/savedMeal/lib/savedMealText';
import type { SavedMeal } from '@/features/savedMeal/types/savedMeal';

// 내 식단 카드

const miniButton =
  'inline-flex h-[30px] items-center justify-center rounded-[3px] border px-3 text-[12.5px] whitespace-nowrap max-[620px]:h-7 max-[620px]:px-2 max-[620px]:text-xs';

type RatioProps = { label: string; short: string; value: number | null };

// 좁은 화면에서는 탄, 단, 지로 줄여 버튼과 한 줄에 둠
function Ratio({ label, short, value }: RatioProps) {
  return (
    <span>
      <span className="max-[620px]:hidden">{label} </span>
      <span className="hidden max-[620px]:inline">{short}</span>
      {percent(value)}
    </span>
  );
}

type Props = {
  meal: SavedMeal;
  keyword: string;
  onDelete: (meal: SavedMeal) => void;
};

export default function SavedMealCard({ meal, keyword, onDelete }: Props) {
  const { nutrition } = meal;
  const photo = representativeDish(meal.dishes);
  const badge = contextBadge(meal.context);
  const date = formatSavedDate(meal.createdAt);
  const link = paths.myMealDetail.replace(':id', String(meal.id));
  const photoFit =
    'w-full object-cover max-[900px]:absolute max-[900px]:inset-0 max-[900px]:aspect-auto max-[900px]:size-full';

  return (
    <article className="relative flex flex-col border border-line bg-white max-[900px]:grid max-[900px]:min-h-[clamp(150px,20vw,176px)] max-[900px]:grid-cols-[clamp(116px,26vw,230px)_1fr]">
      <div className="relative">
        {photo ? (
          <img
            src={photo.imageUrl ?? undefined}
            alt={photo.name}
            className={`aspect-[4/3] ${photoFit}`}
          />
        ) : (
          <div className={`aspect-[4/3] bg-track ${photoFit}`} />
        )}
        {badge !== null && (
          <span className="absolute top-1.5 left-1.5 rounded-[3px] bg-green px-2.5 py-[5px] text-[11.5px] font-bold whitespace-nowrap text-white max-[900px]:hidden">
            {badge}
          </span>
        )}
        <div className="absolute right-0 bottom-0 border-t border-l border-line bg-white px-[11px] py-1.5 text-[17px] leading-[1.1] font-extrabold tracking-[-0.03em] whitespace-nowrap text-green-d tabular-nums max-[620px]:px-2 max-[620px]:py-1 max-[620px]:text-sm">
          {nutrition.calorie == null ? '-' : Math.round(nutrition.calorie)}
          <small className="ml-0.5 text-[0.55em] font-medium">kcal</small>
        </div>
      </div>
      <div className="flex min-w-0 flex-1 flex-col px-4 py-3.5 max-[620px]:px-3 max-[620px]:py-2.5">
        <div className="flex items-baseline justify-between gap-2.5">
          <h3 className="font-heading text-base font-extrabold tracking-[-0.03em] max-[620px]:text-[15.5px]">
            <Link to={link} className="after:absolute after:inset-0">
              <Highlight text={meal.style} keyword={keyword} />
            </Link>
          </h3>
          <span className="hidden shrink-0 text-[12.5px] whitespace-nowrap text-sub max-[900px]:inline">
            <span className="max-[620px]:hidden">{date.slice(0, 5)}</span>
            {date.slice(5)} 저장
          </span>
        </div>
        {badge !== null && (
          <p className="mt-0.5 hidden text-xs text-sub max-[900px]:block">{badge}</p>
        )}
        <p className="mt-1.5 mb-1.5 text-[13px] leading-[1.6]">
          <Highlight text={dishNames(meal.dishes)} keyword={keyword} />
        </p>
        <div className="mt-auto grid grid-cols-[1fr_auto] items-center gap-x-2 gap-y-3 max-[900px]:gap-y-0">
          <p className="col-span-2 flex flex-wrap gap-x-3.5 text-[12.5px] text-sub tabular-nums max-[900px]:col-span-1 max-[620px]:flex-nowrap max-[620px]:gap-x-1.5 max-[620px]:text-xs max-[620px]:whitespace-nowrap">
            <Ratio label="탄수화물" short="탄" value={nutrition.carbohydrateRatio} />
            <Ratio label="단백질" short="단" value={nutrition.proteinRatio} />
            <Ratio label="지방" short="지" value={nutrition.fatRatio} />
          </p>
          <span className="text-[12.5px] text-sub max-[900px]:hidden">{date} 저장</span>
          <span className="relative z-10 col-start-2 row-start-2 flex gap-1.5 max-[900px]:row-start-1 max-[620px]:gap-1">
            <Link
              to={link}
              aria-label={`${meal.style} 상세`}
              className={`${miniButton} border-green font-bold text-green`}
            >
              상세
            </Link>
            <button
              type="button"
              onClick={() => onDelete(meal)}
              aria-label={`${meal.style} 삭제`}
              className={`${miniButton} border-rule bg-white text-ink`}
            >
              삭제
            </button>
          </span>
        </div>
      </div>
    </article>
  );
}
