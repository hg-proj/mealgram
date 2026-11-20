import type { FieldPath, UseFormRegister } from 'react-hook-form';
import Dropdown from '@/components/ui/Dropdown';
import TextField, { Field } from '@/components/ui/TextField';
import BirthDateField from '@/features/auth/components/BirthDateField';
import GenderButtons from '@/features/auth/components/GenderButtons';
import { activityOptions } from '@/features/meal/data/options';
import type { BodyInfoInput } from '@/features/meal/schemas/bodyInfoSchema';

// 신체 정보 입력칸, 회원가입과 식단 추천에서 함께 사용

type Props = {
  register: UseFormRegister<BodyInfoInput>;
  errorOf: (field: FieldPath<BodyInfoInput>) => string | undefined;
  gender: BodyInfoInput['gender'];
  activityLevel: BodyInfoInput['activityLevel'];
  birthYear: string;
  birthMonth: string;
  birthDay: string;
  onGender: (gender: BodyInfoInput['gender']) => void;
  onActivity: (activityLevel: BodyInfoInput['activityLevel']) => void;
  onBirth: (part: 'birthYear' | 'birthMonth' | 'birthDay', value: string) => void;
};

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
          onChange={(value) => onActivity(value as BodyInfoInput['activityLevel'])}
          invalid={Boolean(errorOf('activityLevel'))}
        />
      </Field>
      <div className="mb-3.5">
        <span className="mb-1.5 block text-[13px] font-semibold">성별</span>
        <GenderButtons value={gender} onChange={onGender} />
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
