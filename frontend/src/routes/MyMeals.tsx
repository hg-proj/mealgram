import LoadFailed from '@/components/ui/LoadFailed';
import Skeleton from '@/components/ui/Skeleton';
import { messages } from '@/config/messages';
import { useSavedMeals } from '@/features/savedMeal/api/useSavedMeals';
import DeleteMealDialog from '@/features/savedMeal/components/DeleteMealDialog';
import EmptySavedMeals from '@/features/savedMeal/components/EmptySavedMeals';
import SavedMealList from '@/features/savedMeal/components/SavedMealList';
import { useDeleteMealDialog } from '@/features/savedMeal/hooks/useDeleteMealDialog';

export default function MyMeals() {
  const meals = useSavedMeals();
  const dialog = useDeleteMealDialog();

  if (meals.data?.length === 0) {
    return <EmptySavedMeals />;
  }

  return (
    <section className="px-4 pt-9 pb-16">
      <div className="mx-auto max-w-[1140px]">
        <h1 className="mb-6 font-heading text-[clamp(21px,2.4vw,26px)] font-extrabold tracking-[-0.04em]">
          내 식단
        </h1>
        {meals.isPending ? (
          <div
            aria-busy="true"
            className="grid grid-cols-4 gap-4 max-[1000px]:grid-cols-3 max-[900px]:grid-cols-1"
          >
            {[0, 1, 2, 3].map((item) => (
              <Skeleton key={item} className="h-[280px] max-[900px]:h-[150px]" />
            ))}
          </div>
        ) : meals.isError ? (
          <LoadFailed message={messages.savedMealsFailed} onRetry={() => void meals.refetch()} />
        ) : (
          <SavedMealList meals={meals.data} onDelete={dialog.ask} />
        )}
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
