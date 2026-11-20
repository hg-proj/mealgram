import { zodResolver } from '@hookform/resolvers/zod';
import { useForm, useWatch, type FieldPath } from 'react-hook-form';
import BodyInfoFields from '@/features/auth/components/BodyInfoFields';
import {
  bodyInfoDefaults,
  bodyInfoSchema,
  hasBodyInfo,
  type BodyInfoInput,
} from '@/features/meal/schemas/bodyInfoSchema';

// 식단 추천 1단계, 신체 정보

type Field = FieldPath<BodyInfoInput>;

type Props = {
  defaultValues: BodyInfoInput;
  showSave: boolean;
  save: boolean;
  onSave: (save: boolean) => void;
  onNext: (values: BodyInfoInput) => void;
};

const fieldOrder: Field[] = ['activityLevel', 'birthYear', 'height', 'weight'];

const buttonBase = 'h-[58px] rounded-[3px] text-base font-semibold';
const solid = `${buttonBase} bg-green text-white disabled:bg-track disabled:text-disabled`;
const outline = `${buttonBase} border border-green text-green`;

export default function BodyInfoStep({ defaultValues, showSave, save, onSave, onNext }: Props) {
  const {
    register,
    handleSubmit,
    setValue,
    control,
    formState: { errors, touchedFields, isSubmitted },
  } = useForm<BodyInfoInput>({
    resolver: zodResolver(bodyInfoSchema),
    defaultValues,
    mode: 'onTouched',
  });
  const values = useWatch({ control }) as BodyInfoInput;

  const shownError = fieldOrder.find(
    (field) => errors[field] && (touchedFields[field] || isSubmitted),
  );
  const errorOf = (field: Field) => (field === shownError ? errors[field]?.message : undefined);

  return (
    <form
      onSubmit={handleSubmit(onNext)}
      noValidate
      className="mx-auto max-w-[420px] border border-t-2 border-rule border-t-green p-7"
    >
      <BodyInfoFields
        register={register}
        errorOf={errorOf}
        gender={values.gender}
        activityLevel={values.activityLevel}
        onGender={(value) => setValue('gender', value, { shouldDirty: true })}
        onActivity={(value) => setValue('activityLevel', value, { shouldDirty: true })}
        birthYear={values.birthYear}
        birthMonth={values.birthMonth}
        birthDay={values.birthDay}
        onBirth={(part, value) =>
          setValue(part, value, { shouldDirty: true, shouldValidate: Boolean(errors.birthYear) })
        }
      />
      {showSave && (
        <label className="mb-[22px] flex items-center gap-2 text-[13px]">
          <input
            type="checkbox"
            checked={save}
            onChange={(e) => onSave(e.target.checked)}
            className="size-4 accent-green"
          />
          입력한 내용을 내 정보에 저장
        </label>
      )}
      <div className="grid grid-cols-2 gap-2">
        <button type="button" onClick={() => onNext(bodyInfoDefaults)} className={outline}>
          건너뛰기
        </button>
        <button type="submit" disabled={!hasBodyInfo(values)} className={solid}>
          다음
        </button>
      </div>
      <p className="mt-3.5 text-center text-[12.5px] text-sub">
        식단 추천에 사용합니다. 미기재 시 기본 열량으로 추천해 드립니다.
      </p>
    </form>
  );
}
