import Dropdown from '@/components/ui/Dropdown';
import { Field } from '@/components/ui/TextField';

// 생년월일 선택, 연 월 일 드롭다운

type Part = 'birthYear' | 'birthMonth' | 'birthDay';

type Props = {
  year: string;
  month: string;
  day: string;
  error?: string;
  onChange: (part: Part, value: string) => void;
};

const MAX_AGE = 120;

const range = (from: number, to: number) =>
  Array.from({ length: Math.abs(to - from) + 1 }, (_, i) => (from <= to ? from + i : from - i));

export default function BirthDateField({ year, month, day, error, onChange }: Props) {
  const thisYear = new Date().getFullYear();
  const years = range(thisYear, thisYear - MAX_AGE).map((y) => ({
    value: String(y),
    label: `${y}년`,
  }));
  const months = range(1, 12).map((m) => ({ value: String(m), label: `${m}월` }));
  const lastDay = year && month ? new Date(Number(year), Number(month), 0).getDate() : 31;
  const days = range(1, lastDay).map((d) => ({ value: String(d), label: `${d}일` }));

  const change = (part: 'birthYear' | 'birthMonth', value: string) => {
    onChange(part, value);

    const nextYear = part === 'birthYear' ? value : year;
    const nextMonth = part === 'birthMonth' ? value : month;
    const nextLast =
      nextYear && nextMonth ? new Date(Number(nextYear), Number(nextMonth), 0).getDate() : 31;

    if (day && Number(day) > nextLast) {
      onChange('birthDay', '');
    }
  };

  return (
    <Field id="birthYear" label="생년월일" error={error}>
      <div className="grid grid-cols-3 gap-2">
        <Dropdown
          id="birthYear"
          ariaLabel="출생 연도"
          value={year}
          options={years}
          placeholder="년"
          onChange={(value) => change('birthYear', value)}
          invalid={Boolean(error)}
        />
        <Dropdown
          id="birthMonth"
          ariaLabel="출생 월"
          value={month}
          options={months}
          placeholder="월"
          onChange={(value) => change('birthMonth', value)}
          invalid={Boolean(error)}
        />
        <Dropdown
          id="birthDay"
          ariaLabel="출생 일"
          value={day}
          options={days}
          placeholder="일"
          onChange={(value) => onChange('birthDay', value)}
          invalid={Boolean(error)}
        />
      </div>
    </Field>
  );
}
