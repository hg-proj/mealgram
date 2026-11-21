import { Link, useNavigate, useParams } from 'react-router';
import ArrowIcon from '@/components/ui/ArrowIcon';
import LoadFailed from '@/components/ui/LoadFailed';
import Skeleton from '@/components/ui/Skeleton';
import { messages } from '@/config/messages';
import { paths } from '@/config/paths';
import MealDetailPanel from '@/features/meal/components/MealDetailPanel';
import { contextBadge } from '@/features/meal/lib/contextBadge';
import { useSavedMeals } from '@/features/savedMeal/api/useSavedMeals';
import DeleteMealDialog from '@/features/savedMeal/components/DeleteMealDialog';
import { useDeleteMealDialog } from '@/features/savedMeal/hooks/useDeleteMealDialog';

export default function MyMealDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const meals = useSavedMeals();
  const dialog = useDeleteMealDialog(() => navigate(paths.myMeals));
  const meal = meals.data?.find((item) => String(item.id) === id);

  return (
    <section className="px-4 pt-9 pb-16">
      <div className="mx-auto max-w-[1140px]">
        {meals.isPending ? (
          <Skeleton className="h-[260px]" />
        ) : meal === undefined ? (
          <LoadFailed
            message={meals.isError ? messages.savedMealsFailed : messages.notice.savedMealNotFound}
            onRetry={meals.isError ? () => void meals.refetch() : undefined}
          />
        ) : (
          <>
            <MealDetailPanel
              candidate={meal}
              badge={contextBadge(meal.context)}
              action={
                <button
                  type="button"
                  onClick={() => dialog.ask(meal)}
                  className="h-[42px] w-full rounded-[3px] border border-green bg-white text-sm font-bold text-green"
                >
                  삭제
                </button>
              }
            />
          </>
        )}
        <div className="mt-8 text-[13px] text-sub max-[620px]:text-center">
          <Link to={paths.myMeals} className="inline-flex items-center gap-1.5">
            <ArrowIcon className="size-[15px] -scale-x-100" />내 식단으로 돌아가기
          </Link>
        </div>
      </div>
      {dialog.target !== null && (
        <DeleteMealDialog
          meal={dialog.target}
          pending={dialog.pending}
          error={dialog.error}
          onConfirm={() => void dialog.confirm()}
          onClose={dialog.close}
        />
      )}
    </section>
  );
}
