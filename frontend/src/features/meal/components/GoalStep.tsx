import { DEFAULT_STYLE, genres, goals, RANDOM_GENRE, styles } from '@/features/meal/data/options';
import type { Goal } from '@/features/meal/types/meal';

// 식단 추천 2단계, 목표와 장르

type Props = {
  goal: Goal | '';
  style: string;
  selectedGenres: string[];
  onGoal: (goal: Goal) => void;
  onStyle: (style: string) => void;
  onGenres: (genres: string[]) => void;
  onPrev: () => void;
  onNext: () => void;
};

const choice = 'h-14 rounded-[3px] border text-base';
const on = 'border-green bg-green-soft font-semibold text-green-d';
const off = 'border-rule text-sub';
const buttonBase = 'h-[58px] rounded-[3px] text-base font-semibold';

export default function GoalStep({
  goal,
  style,
  selectedGenres,
  onGoal,
  onStyle,
  onGenres,
  onPrev,
  onNext,
}: Props) {
  const toggleGenre = (genre: string) => {
    if (genre === RANDOM_GENRE) {
      onGenres(selectedGenres.includes(RANDOM_GENRE) ? [] : [RANDOM_GENRE]);

      return;
    }

    const rest = selectedGenres.filter((item) => item !== RANDOM_GENRE);
    onGenres(rest.includes(genre) ? rest.filter((item) => item !== genre) : [...rest, genre]);
  };

  return (
    <div className="mx-auto max-w-[420px] border border-t-2 border-rule border-t-green p-7">
      <div className="mb-[22px]">
        <span className="mb-1.5 block text-[13px] font-semibold">목표 (필수)</span>
        <div className="grid grid-cols-3 gap-2">
          {goals.map((item) => (
            <button
              key={item}
              type="button"
              aria-pressed={goal === item}
              onClick={() => onGoal(item)}
              className={`${choice} ${goal === item ? on : off}`}
            >
              {item}
            </button>
          ))}
        </div>
      </div>
      <div className="mb-[22px]">
        <span className="mb-1.5 block text-[13px] font-semibold">장르 (선택)</span>
        <div className="grid grid-cols-3 gap-2">
          {[...genres, RANDOM_GENRE].map((item) => (
            <button
              key={item}
              type="button"
              aria-pressed={selectedGenres.includes(item)}
              onClick={() => toggleGenre(item)}
              className={`${choice} ${selectedGenres.includes(item) ? on : off}`}
            >
              {item}
            </button>
          ))}
        </div>
      </div>
      <div className="mb-[22px]">
        <span className="mb-1.5 block text-[13px] font-semibold">식단 성향 (선택)</span>
        <div className="grid grid-cols-4 gap-2">
          {styles.map((item) => (
            <button
              key={item}
              type="button"
              aria-pressed={style === item}
              onClick={() => onStyle(style === item ? DEFAULT_STYLE : item)}
              className={`${choice} ${style === item ? on : off}`}
            >
              {item}
            </button>
          ))}
        </div>
      </div>
      <div className="grid grid-cols-2 gap-2">
        <button
          type="button"
          onClick={onPrev}
          className={`${buttonBase} border border-green text-green`}
        >
          이전
        </button>
        <button
          type="button"
          onClick={onNext}
          disabled={goal === ''}
          className={`${buttonBase} bg-green text-white disabled:bg-track disabled:text-disabled`}
        >
          다음
        </button>
      </div>
    </div>
  );
}
