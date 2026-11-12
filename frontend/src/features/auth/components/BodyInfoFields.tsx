import type { FieldPath, UseFormRegister } from 'react-hook-form';
import Dropdown from '@/components/ui/Dropdown';
import TextField, { Field } from '@/components/ui/TextField';
import BirthDateField from '@/features/auth/components/BirthDateField';
import type { SignupInput } from '@/features/auth/schemas/signupSchema';

// 회원가입 2단계, 선택 입력인 신체 정보

type Props = {
  register: UseFormRegister<SignupInput>;
  errorOf: (field: FieldPath<SignupInput>) => string | undefined;
  gender: SignupInput['gender'];
  activityLevel: SignupInput['activityLevel'];
  birthYear: string;
  birthMonth: string;
  birthDay: string;
  onGender: (gender: SignupInput['gender']) => void;
  onActivity: (activityLevel: SignupInput['activityLevel']) => void;
  onBirth: (part: 'birthYear' | 'birthMonth' | 'birthDay', value: string) => void;
};

const activityOptions = [
  { value: 'LOW', label: '비활동적', description: '주로 앉아서 생활' },
  { value: 'LIGHT', label: '저활동적', description: '일상적인 걷기 정도' },
  { value: 'MODERATE', label: '활동적', description: '주 3~5회 운동' },
  { value: 'HIGH', label: '매우 활동적', description: '거의 매일 운동하거나 육체노동' },
];

const genders = [
  { value: 'MALE', label: '남자' },
  { value: 'FEMALE', label: '여자' },
] as const;

export default function BodyInfoFields({
  register,
  errorOf,
  gender,
  activityLevel,
  onGender,
  onActivity,
  birthYear,
  birthMonth,
  birthDay,
  onBirth,
}: Props) {
  return (
    <>
      <Field id="activityLevel" label="활동량" error={errorOf('activityLevel')}>
        <Dropdown
          id="activityLevel"
          value={activityLevel}
          options={activityOptions}
          placeholder="활동량 선택"
          onChange={(value) => onActivity(value as SignupInput['activityLevel'])}
          invalid={Boolean(errorOf('activityLevel'))}
        />
      </Field>
      <div className="mb-3.5">
        <span className="mb-1.5 block text-[13px] font-semibold">성별</span>
        <div className="grid grid-cols-2 gap-2">
          {genders.map((option) => (
            <button
              key={option.value}
              type="button"
              aria-pressed={gender === option.value}
              onClick={() => onGender(gender === option.value ? '' : option.value)}
              className={`h-14 rounded-[3px] border ${gender === option.value ? 'border-green bg-green-soft font-semibold text-green-d' : 'border-rule text-sub'}`}
            >
              {option.label}
            </button>
          ))}
        </div>
      </div>
      <BirthDateField
        year={birthYear}
        month={birthMonth}
        day={birthDay}
        error={errorOf('birthYear')}
        onChange={onBirth}
      />
      <div className="grid grid-cols-2 gap-2">
        <TextField
          id="height"
          label="키(cm)"
          placeholder="예: 170"
          inputMode="decimal"
          error={errorOf('height')}
          registration={register('height')}
        />
        <TextField
          id="weight"
          label="몸무게(kg)"
          placeholder="예: 65"
          inputMode="decimal"
          error={errorOf('weight')}
          registration={register('weight')}
        />
      </div>
    </>
  );
}
