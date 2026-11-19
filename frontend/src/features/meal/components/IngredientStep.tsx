import { useEffect, useMemo, useState, type DragEvent } from 'react';
import { messages } from '@/config/messages';
import { useAuth } from '@/features/auth/hooks/useAuth';
import { addMyIngredient, getMyIngredients } from '@/features/meal/api/myIngredients';
import { useIngredientCatalog } from '@/features/meal/api/useIngredientCatalog';
import IngredientPalette from '@/features/meal/components/IngredientPalette';
import { ingredientBox, startIngredientDrag } from '@/features/meal/lib/ingredientBox';
import type { PickedIngredients } from '@/features/meal/types/ingredient';
import { errorMessage } from '@/lib/apiError';

// 식단 추천 3단계, 재료를 끌어서 필수 재료와 서브 재료 칸에 담음

type Zone = 'required' | 'sub';

type Props = {
  picked: PickedIngredients;
  onPick: (next: PickedIngredients) => void;
  onPrev: () => void;
  onSubmit: () => void;
  pending: boolean;
  error: string;
};

const textButton = 'text-[13px] font-semibold underline';
const buttonBase = 'h-[58px] rounded-[3px] text-base font-semibold';

export default function IngredientStep({
  picked,
  onPick,
  onPrev,
  onSubmit,
  pending,
  error,
}: Props) {
  const { isLoggedIn } = useAuth();
  const catalog = useIngredientCatalog();
  const [over, setOver] = useState<Zone | null>(null);
  const [notice, setNotice] = useState('');
  const [saved, setSaved] = useState(false);
  const [loaded, setLoaded] = useState<'done' | 'none' | null>(null);

  useEffect(() => {
    if (!saved) {
      return;
    }
    const timer = setTimeout(() => setSaved(false), 2000);

    return () => clearTimeout(timer);
  }, [saved]);

  useEffect(() => {
    if (loaded === null) {
      return;
    }
    const timer = setTimeout(() => setLoaded(null), 2000);

    return () => clearTimeout(timer);
  }, [loaded]);

  const categories = useMemo(() => catalog.data ?? [], [catalog.data]);
  const names = useMemo(
    () => new Map(categories.flatMap((c) => c.ingredients.map((i) => [i.id, i.name] as const))),
    [categories],
  );
  const zoneOf = (id: number): 'none' | 'sub' | 'required' =>
    picked.requiredId === id ? 'required' : picked.subIds.includes(id) ? 'sub' : 'none';

  const remove = (id: number) =>
    onPick({
      requiredId: picked.requiredId === id ? null : picked.requiredId,
      subIds: picked.subIds.filter((item) => item !== id),
    });

  const place = (id: number, zone: Zone) => {
    setNotice('');

    if (zone === 'required') {
      if (picked.requiredId === id) {
        return;
      }
      if (picked.requiredId !== null) {
        setNotice(messages.notice.requiredOnly);

        return;
      }
      onPick({ requiredId: id, subIds: picked.subIds.filter((item) => item !== id) });

      return;
    }

    if (!picked.subIds.includes(id)) {
      onPick({
        requiredId: picked.requiredId === id ? null : picked.requiredId,
        subIds: [...picked.subIds, id],
      });
    }
  };

  const dropTo = (e: DragEvent, zone: Zone | null) => {
    e.preventDefault();
    setOver(null);
    const id = Number(e.dataTransfer.getData('text/plain'));

    if (!names.has(id)) {
      return;
    }
    if (zone === null) {
      remove(id);
    } else {
      place(id, zone);
    }
  };

  const loadMine = async () => {
    if (!isLoggedIn) {
      setNotice(messages.notice.loginForMyIngredients);

      return;
    }

    try {
      const mine = await getMyIngredients();
      const addable = mine
        .map((item) => item.ingredientId)
        .filter((id) => names.has(id) && id !== picked.requiredId && !picked.subIds.includes(id));

      setNotice('');
      setLoaded(mine.length === 0 ? 'none' : 'done');
      onPick({ ...picked, subIds: [...picked.subIds, ...addable] });
    } catch (e) {
      setNotice(errorMessage(e, messages.catalogFailed));
    }
  };

  const saveMine = async () => {
    if (!isLoggedIn) {
      setNotice(messages.notice.loginForMyIngredients);

      return;
    }

    const ids = picked.requiredId === null ? picked.subIds : [picked.requiredId, ...picked.subIds];
    await Promise.allSettled(ids.map(addMyIngredient));
    setNotice('');
    setSaved(true);
  };

  const zoneProps = (zone: Zone) => ({
    onDragOver: (e: DragEvent) => {
      e.preventDefault();
      setOver(zone);
    },
    onDragLeave: () => setOver(null),
    onDrop: (e: DragEvent) => dropTo(e, zone),
  });

  const chip = (id: number) => (
    <button
      key={id}
      type="button"
      draggable
      aria-label={`${names.get(id)} 빼기`}
      onDragStart={(e) => startIngredientDrag(e, id)}
      onClick={() => remove(id)}
      className={ingredientBox(zoneOf(id))}
    >
      {names.get(id)}
    </button>
  );

  return (
    <div>
      <div className="mb-3 flex items-end justify-between">
        <b className="text-[17px]">재료</b>
        <span className="flex gap-3.5">
          <button type="button" onClick={loadMine} className={`${textButton} text-green`}>
            {loaded === 'done'
              ? '내 재료 불러오기 완료'
              : loaded === 'none'
                ? '내 재료 미존재'
                : '내 재료 불러오기'}
          </button>
          <button type="button" onClick={saveMine} className={`${textButton} font-medium text-sub`}>
            {saved ? '재료 저장 완료' : '재료 저장하기'}
          </button>
        </span>
      </div>

      <IngredientPalette
        categories={categories}
        isPending={catalog.isPending}
        isError={catalog.isError}
        onRetry={() => void catalog.refetch()}
        stateOf={zoneOf}
        onToggle={(id, state) => (state === 'none' ? place(id, 'sub') : remove(id))}
        onDropToList={(e) => dropTo(e, null)}
      >
        <div className="grid grid-cols-[124px_1fr] gap-2 px-3.5 pb-3 select-none max-[560px]:grid-cols-1">
          <div
            {...zoneProps('required')}
            className={`flex min-h-[84px] flex-col gap-2 rounded-[3px] border-2 border-green bg-white px-3 py-2.5 ${over === 'required' ? 'outline-2 -outline-offset-2 outline-green' : ''}`}
          >
            <span className="text-xs font-semibold text-green">필수 재료</span>
            <div className="flex min-h-[34px] flex-wrap content-start gap-1.5">
              {picked.requiredId !== null && chip(picked.requiredId)}
            </div>
          </div>
          <div
            {...zoneProps('sub')}
            className={`flex min-h-[84px] flex-col gap-2 rounded-[3px] border border-rule bg-band px-3 py-2.5 ${over === 'sub' ? 'outline-2 -outline-offset-2 outline-green' : ''}`}
          >
            <span className="text-xs font-semibold text-sub">서브 재료</span>
            <div className="flex min-h-[34px] flex-wrap content-start gap-1.5">
              {picked.subIds.map(chip)}
            </div>
          </div>
        </div>
      </IngredientPalette>
      <p className="mt-2.5 text-[12.5px] text-sub">
        재료를 끌거나 눌러서 필수 재료나 서브 재료 칸에 놓을 수 있습니다.
      </p>
      {notice && (
        <p role="status" className="mt-1.5 text-[12.5px] text-green">
          {notice}
        </p>
      )}
      {error && (
        <p role="alert" className="mt-3 text-[13px] text-danger">
          {error}
        </p>
      )}
      <div className="mt-[22px] grid grid-cols-2 gap-2">
        <button
          type="button"
          onClick={onPrev}
          className={`${buttonBase} border border-green text-green`}
        >
          이전
        </button>
        <button
          type="button"
          onClick={onSubmit}
          disabled={pending}
          className={`${buttonBase} bg-green text-white disabled:opacity-60`}
        >
          {pending ? '식단을 만드는 중' : '식단 추천받기'}
        </button>
      </div>
    </div>
  );
}
