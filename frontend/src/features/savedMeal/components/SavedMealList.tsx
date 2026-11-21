import { useState } from 'react';
import searchIcon from '@/assets/search.svg';
import MaskIcon from '@/components/ui/MaskIcon';
import { messages } from '@/config/messages';
import SavedMealCard from '@/features/savedMeal/components/SavedMealCard';
import { matchesKeyword } from '@/features/savedMeal/lib/savedMealText';
import type { SavedMeal } from '@/features/savedMeal/types/savedMeal';

// 내 식단 검색과 목록

type Props = {
  meals: SavedMeal[];
  onDelete: (meal: SavedMeal) => void;
};

export default function SavedMealList({ meals, onDelete }: Props) {
  const [keyword, setKeyword] = useState('');
  const shown = meals.filter((meal) => matchesKeyword(meal, keyword));

  return (
    <div>
      <div className="relative mb-3">
        <MaskIcon
          src={searchIcon}
          className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-sub"
        />
        <input
          type="text"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          placeholder="식단 이름이나 요리 이름으로 검색"
          aria-label="식단 검색"
          className="h-[42px] w-full rounded-[3px] border border-rule bg-white pr-3 pl-[38px] text-sm outline-none focus:border-green"
        />
      </div>
      {shown.length === 0 ? (
        <p className="border border-line bg-band px-4 py-14 text-center text-sm text-sub">
          {messages.notice.noSearchResult}
        </p>
      ) : (
        <div className="grid grid-cols-4 gap-4 max-[1000px]:grid-cols-3 max-[900px]:grid-cols-1 max-[900px]:gap-3">
          {shown.map((meal) => (
            <SavedMealCard key={meal.id} meal={meal} keyword={keyword} onDelete={onDelete} />
          ))}
        </div>
      )}
    </div>
  );
}
