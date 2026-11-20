import type { Gender } from '@/features/member/types/member';

type Props = {
  value: Gender | '';
  onChange: (value: Gender | '') => void;
  heightClass?: string;
};

const genders = [
  { value: 'MALE', label: '남자' },
  { value: 'FEMALE', label: '여자' },
] as const;

export default function GenderButtons({ value, onChange, heightClass = 'h-14' }: Props) {
  return (
    <div className="grid grid-cols-2 gap-2">
      {genders.map((option) => (
        <button
          key={option.value}
          type="button"
          aria-pressed={value === option.value}
          onClick={() => onChange(value === option.value ? '' : option.value)}
          className={`${heightClass} rounded-[3px] border ${value === option.value ? 'border-green bg-green-soft font-semibold text-green-d' : 'border-rule text-sub'}`}
        >
          {option.label}
        </button>
      ))}
    </div>
  );
}
