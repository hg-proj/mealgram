import { Link } from 'react-router';
import ArrowIcon from '@/components/ui/ArrowIcon';
import { paths } from '@/config/paths';
import { saveSampleResult } from '@/features/meal/lib/sampleResult';
import type { MealSample } from '@/features/meal/types/meal';
import { percent } from '@/features/meal/lib/format';

// 랜덤 식단 카드

const CATEGORY_ORDER = ['일품', '밥', '국', '메인 반찬', '곁들임 반찬', '김치와 절임', '후식'];

function rank(category: string): number {
  const index = CATEGORY_ORDER.indexOf(category);

  return index === -1 ? CATEGORY_ORDER.length : index;
}

function sortedNames(sample: MealSample): string {
  return sample.dishes
    .map((dish, order) => ({ dish, order }))
    .sort((a, b) => rank(a.dish.category) - rank(b.dish.category) || a.order - b.order)
    .map(({ dish }) => dish.name)
    .join(', ');
}

const styles = {
  featured: {
    frame:
      'relative row-span-2 flex flex-col border border-line bg-white max-[860px]:row-auto max-[860px]:grid max-[860px]:grid-cols-[clamp(110px,40%,170px)_minmax(0,1fr)] max-[860px]:grid-rows-[1fr_auto] max-[860px]:gap-x-[18px] max-[860px]:p-3.5 max-[360px]:grid-cols-1 max-[360px]:grid-rows-none',
    image:
      'min-h-0 w-full flex-1 object-cover max-[860px]:row-span-2 max-[860px]:aspect-square max-[860px]:flex-none max-[860px]:self-center max-[360px]:row-span-1',
    text: 'absolute bottom-[74px] left-0 max-w-[78%] bg-white pt-[18px] pr-7 pb-4 pl-[22px] max-[860px]:static max-[860px]:max-w-none max-[860px]:bg-transparent max-[860px]:p-0',
    heading:
      'mb-1.5 font-heading text-[clamp(20px,2vw,28px)] font-extrabold tracking-[-0.03em] max-[860px]:text-[17px] max-[860px]:font-bold max-[860px]:tracking-[-0.03em]',
    names: 'text-[12.5px] text-sub max-[860px]:text-xs max-[860px]:leading-[1.6]',
    footer:
      'flex items-baseline gap-3.5 px-4 pt-3.5 pb-4 text-[11.5px] text-sub max-[860px]:flex-wrap max-[860px]:p-0 max-[860px]:pt-3 max-[860px]:pb-0.5',
  },
  normal: {
    frame:
      'relative grid grid-cols-[clamp(110px,40%,170px)_minmax(0,1fr)] grid-rows-[1fr_auto] gap-x-[18px] border border-line bg-white p-3.5 max-[360px]:grid-cols-1 max-[360px]:grid-rows-none',
    image: 'row-span-2 aspect-square w-full self-center object-cover max-[360px]:row-span-1',
    text: '',
    heading: 'mb-1.5 font-heading text-[17px] font-bold tracking-[-0.03em]',
    names: 'text-xs leading-[1.6] text-sub',
    footer: 'flex flex-wrap items-baseline gap-3.5 pt-3 pb-0.5 text-[11.5px] text-sub',
  },
};

type Props = { sample: MealSample; featured?: boolean };

export default function SampleMealCard({ sample, featured = false }: Props) {
  const photo = sample.dishes.find((dish) => dish.imageUrl)?.imageUrl ?? null;
  const nutrition = sample.nutrition;

  const style = styles[featured ? 'featured' : 'normal'];

  return (
    <article className={`min-w-0 ${style.frame}`}>
      {photo ? (
        <img
          src={photo}
          alt={sample.dishes.find((dish) => dish.imageUrl)?.name ?? sample.title}
          className={style.image}
        />
      ) : (
        <div className={`${style.image} bg-track`} />
      )}
      <div className={`min-w-0 [overflow-wrap:anywhere] ${style.text}`}>
        <span className="mb-2.5 inline-block rounded-[3px] bg-green px-2.5 py-[3px] text-[11px] font-bold text-white">
          {sample.goal}
        </span>
        <h3 className={style.heading}>{sample.title}</h3>
        <p className={style.names}>{sortedNames(sample)}</p>
      </div>
      <div className={style.footer}>
        <b className="mr-1.5 text-xl font-bold text-green-d tabular-nums">
          {nutrition?.calorie == null ? '-' : Math.round(nutrition.calorie)}
          <small className="ml-0.5 text-[0.55em] font-normal opacity-80">kcal</small>
        </b>
        <span>탄 {percent(nutrition?.carbohydrateRatio)}</span>
        <span>단 {percent(nutrition?.proteinRatio)}</span>
        <span>지 {percent(nutrition?.fatRatio)}</span>
        <Link
          to={paths.mealDetail.replace(':index', '0')}
          onClick={() => saveSampleResult(sample)}
          aria-label={`${sample.title} 보기`}
          className="ml-auto flex self-center text-green after:absolute after:inset-0"
        >
          <ArrowIcon className="size-[18px]" />
        </Link>
      </div>
    </article>
  );
}
