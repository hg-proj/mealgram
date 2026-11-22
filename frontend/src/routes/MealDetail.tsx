import { Link, useParams } from 'react-router';
import ArrowIcon from '@/components/ui/ArrowIcon';
import { paths } from '@/config/paths';
import MealDetailPanel from '@/features/meal/components/MealDetailPanel';
import DetailNotice from '@/features/meal/components/DetailNotice';
import NoMealNotice from '@/features/meal/components/NoMealNotice';
import SaveMealButton from '@/features/meal/components/SaveMealButton';
import ShoppingBox from '@/features/meal/components/ShoppingBox';
import { contextBadge } from '@/features/meal/lib/contextBadge';
import { resultStore } from '@/features/meal/lib/resultStore';

export default function MealDetail() {
  const { index } = useParams();
  const result = resultStore.read();
  const context = resultStore.readContext();
  const candidate = result?.candidates[Number(index)];

  if (result === null || candidate === undefined) {
    return <NoMealNotice />;
  }

  const isSample = result.sample === true;

  return (
    <section className="px-4 pt-9 pb-16">
      <div className="mx-auto max-w-[1140px]">
        <MealDetailPanel
          candidate={candidate}
          badge={contextBadge(context, candidate.goal, candidate.genre)}
          action={
            <SaveMealButton
              mealId={result.id}
              index={Number(index)}
              goal={candidate.goal}
              genre={candidate.genre}
            />
          }
        />
        <ShoppingBox
          dishes={candidate.dishes}
          requiredName={context?.requiredName ?? null}
          ownedNames={context?.ownedNames ?? []}
        />
        <div className="mt-8 grid grid-cols-[1fr_auto_1fr] items-baseline gap-x-4 text-[13px] text-sub max-[620px]:grid-cols-1 max-[620px]:gap-y-2 max-[620px]:text-center">
          <Link
            to={isSample ? paths.home : paths.mealList}
            className="inline-flex items-center gap-1.5 justify-self-start max-[620px]:justify-self-center"
          >
            <ArrowIcon className="size-[15px] -scale-x-100" />
            {isSample ? '메인으로 돌아가기' : '추천 식단으로 돌아가기'}
          </Link>
          <DetailNotice />
        </div>
      </div>
    </section>
  );
}
