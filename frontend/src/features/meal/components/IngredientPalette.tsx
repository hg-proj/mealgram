import { useState, type DragEvent, type ReactNode } from 'react';
import LoadFailed from '@/components/ui/LoadFailed';
import { messages } from '@/config/messages';
import {
  ingredientBox,
  startIngredientDrag,
  type BoxState,
} from '@/features/meal/lib/ingredientBox';
import type { IngredientCategory } from '@/features/meal/types/ingredient';

// 재료 고르는 영역

type Props = {
  categories: IngredientCategory[];
  isPending: boolean;
  isError: boolean;
  onRetry: () => void;
  stateOf: (id: number) => BoxState;
  onToggle: (id: number, state: BoxState) => void;
  onDropToList: (e: DragEvent) => void;
  children: ReactNode;
};

export default function IngredientPalette({
  categories,
  isPending,
  isError,
  onRetry,
  stateOf,
  onToggle,
  onDropToList,
  children,
}: Props) {
  const [category, setCategory] = useState('');
  const [keyword, setKeyword] = useState('');

  const activeCategory = category || categories[0]?.category;
  const trimmed = keyword.trim();
  const list = trimmed
    ? categories.flatMap((c) => c.ingredients).filter((i) => i.name.includes(trimmed))
    : (categories.find((c) => c.category === activeCategory)?.ingredients ?? []);

  if (isPending) {
    return (
      <div className="h-[330px] animate-pulse border border-t-2 border-rule border-t-green bg-band" />
    );
  }

  if (isError) {
    return <LoadFailed message={messages.catalogFailed} onRetry={onRetry} />;
  }

  return (
    <div className="border border-t-2 border-rule border-t-green">
      <div className="flex h-[52px] items-center border-b border-line">
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="재료 검색"
          aria-label="재료 검색"
          className="h-full min-w-0 flex-1 px-4 text-[15px] outline-none"
        />
      </div>
      <div className="flex flex-wrap border-b border-line px-0.5">
        {categories.map((c) => (
          <button
            key={c.category}
            type="button"
            aria-pressed={!trimmed && c.category === activeCategory}
            onClick={() => {
              setCategory(c.category);
              setKeyword('');
            }}
            className={`px-3 py-[11px] text-[13.5px] whitespace-nowrap ${
              !trimmed && c.category === activeCategory
                ? 'font-bold text-green shadow-[inset_0_-2px_0_var(--color-green)]'
                : 'text-sub'
            }`}
          >
            {c.category}
          </button>
        ))}
      </div>
      <div
        onDragOver={(e) => e.preventDefault()}
        onDrop={onDropToList}
        className="flex min-h-[176px] flex-wrap content-start gap-2 px-4 py-[18px] select-none"
      >
        {list.map((item) => (
          <button
            key={item.id}
            type="button"
            draggable
            onDragStart={(e) => startIngredientDrag(e, item.id)}
            onClick={() => onToggle(item.id, stateOf(item.id))}
            className={ingredientBox(stateOf(item.id))}
          >
            {item.name}
          </button>
        ))}
      </div>
      {children}
    </div>
  );
}
