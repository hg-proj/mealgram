import { useSaveCandidate } from '@/features/meal/api/useSaveCandidate';

type Props = {
  mealId: string | null;
  index: number;
  goal?: string | null;
  genre?: string | null;
};

export default function SaveMealButton({ mealId, index, goal, genre }: Props) {
  const save = useSaveCandidate(mealId, index, goal, genre);

  return (
    <>
      <button
        type="button"
        onClick={save.onSave}
        disabled={!save.canSave || save.saved || save.pending}
        className="h-[42px] w-full rounded-[3px] border border-green bg-green text-sm font-bold text-white disabled:border-rule disabled:bg-white disabled:text-disabled"
      >
        {save.saved ? '식단 저장 완료' : '내 식단에 저장'}
      </button>
      {save.error !== '' && (
        <p role="alert" className="mt-2 text-xs text-danger">
          {save.error}
        </p>
      )}
    </>
  );
}
