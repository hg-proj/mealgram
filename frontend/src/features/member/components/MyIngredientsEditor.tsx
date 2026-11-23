import { useEffect, useMemo, useState, type DragEvent } from 'react';
import LoadFailed from '@/components/ui/LoadFailed';
import Skeleton from '@/components/ui/Skeleton';
import { messages } from '@/config/messages';
import { useIngredientCatalog } from '@/features/meal/api/useIngredientCatalog';
import IngredientPalette from '@/features/meal/components/IngredientPalette';
import { ingredientBox, startIngredientDrag } from '@/features/meal/lib/ingredientBox';
import { useMyIngredients } from '@/features/member/api/useMyIngredients';
import { useSaveMyIngredients } from '@/features/member/api/useSaveMyIngredients';
import { errorMessage } from '@/lib/apiError';

// 내 재료 편집

const buttonBase = 'h-9 min-w-[84px] rounded-[3px] border px-4 text-[13px] font-bold';

export default function MyIngredientsEditor() {
  const catalog = useIngredientCatalog();
  const mine = useMyIngredients();
  const save = useSaveMyIngredients();
  const [draft, setDraft] = useState<number[] | null>(null);
  const [over, setOver] = useState(false);
  const [saved, setSaved] = useState(false);
  const [failed, setFailed] = useState('');

  useEffect(() => {
    if (!saved) {
      return;
    }
    const timer = setTimeout(() => setSaved(false), 2000);

    return () => clearTimeout(timer);
  }, [saved]);

  const categories = useMemo(() => catalog.data ?? [], [catalog.data]);
  const names = useMemo(
    () => new Map(categories.flatMap((c) => c.ingredients.map((i) => [i.id, i.name] as const))),
    [categories],
  );
  const savedIds = useMemo(() => (mine.data ?? []).map((item) => item.ingredientId), [mine.data]);
  const picked = draft ?? savedIds;
  const changed = picked.length !== savedIds.length || picked.some((id) => !savedIds.includes(id));

  const add = (id: number) => {
    setSaved(false);
    if (!picked.includes(id)) {
      setDraft([...picked, id]);
    }
  };
  const remove = (id: number) => {
    setSaved(false);
    setDraft(picked.filter((item) => item !== id));
  };

  const dropTo = (e: DragEvent, zone: boolean) => {
    e.preventDefault();
    setOver(false);
    const id = Number(e.dataTransfer.getData('text/plain'));

    if (names.has(id)) {
      if (zone) {
        add(id);
      } else {
        remove(id);
      }
    }
  };

  const submit = async () => {
    setFailed('');

    try {
      await save.mutateAsync({ saved: mine.data ?? [], picked });
      setDraft(null);
      setSaved(true);
    } catch (e) {
      setFailed(errorMessage(e, messages.myIngredientsSaveFailed));
    }
  };

  if (mine.isPending) {
    return <Skeleton className="h-[330px]" />;
  }

  if (mine.isError) {
    return (
      <LoadFailed message={messages.myIngredientsFailed} onRetry={() => void mine.refetch()} />
    );
  }

  return (
    <div>
      <IngredientPalette
        categories={categories}
        isPending={catalog.isPending}
        isError={catalog.isError}
        onRetry={() => void catalog.refetch()}
        stateOf={(id) => (picked.includes(id) ? 'sub' : 'none')}
        onToggle={(id, state) => (state === 'none' ? add(id) : remove(id))}
        onDropToList={(e) => dropTo(e, false)}
      >
        <div className="px-3.5 pb-3 select-none">
          <div
            onDragOver={(e) => {
              e.preventDefault();
              setOver(true);
            }}
            onDragLeave={() => setOver(false)}
            onDrop={(e) => dropTo(e, true)}
            className={`flex min-h-[84px] flex-col gap-2 rounded-[3px] border border-rule bg-band px-3 py-2.5 ${over ? 'outline-2 -outline-offset-2 outline-green' : ''}`}
          >
            <span className="text-xs font-semibold text-sub">내 재료</span>
            <div className="flex min-h-[34px] flex-wrap content-start gap-1.5">
              {picked
                .filter((id) => names.has(id))
                .map((id) => (
                  <button
                    key={id}
                    type="button"
                    draggable
                    aria-label={`${names.get(id)} 빼기`}
                    onDragStart={(e) => startIngredientDrag(e, id)}
                    onClick={() => remove(id)}
                    className={ingredientBox('sub')}
                  >
                    {names.get(id)}
                  </button>
                ))}
            </div>
          </div>
        </div>
      </IngredientPalette>
      <p className="mt-2.5 text-[12.5px] text-sub">
        재료를 끌거나 눌러서 내 재료 칸에 놓을 수 있습니다.
      </p>
      {failed !== '' && (
        <p role="alert" className="mt-3 text-[13px] text-danger">
          {failed}
        </p>
      )}
      <div className="mt-3.5 flex justify-end gap-2">
        <button
          type="button"
          disabled={!changed}
          onClick={() => setDraft(null)}
          className={`${buttonBase} border-green bg-white text-green disabled:border-rule disabled:text-disabled`}
        >
          취소
        </button>
        <button
          type="button"
          disabled={!changed || save.isPending}
          onClick={() => void submit()}
          className={`${buttonBase} border-green bg-green text-white disabled:border-rule disabled:bg-band disabled:text-disabled ${saved ? 'disabled:border-green disabled:bg-green disabled:text-white' : ''}`}
        >
          {saved ? '저장 완료' : '저장'}
        </button>
      </div>
    </div>
  );
}
