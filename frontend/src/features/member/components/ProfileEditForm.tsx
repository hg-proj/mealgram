import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm, useWatch, type FieldPath } from 'react-hook-form';
import Dropdown from '@/components/ui/Dropdown';
import BirthDateField from '@/features/auth/components/BirthDateField';
import GenderButtons from '@/features/auth/components/GenderButtons';
import { messages } from '@/config/messages';
import { activityOptions } from '@/features/meal/data/options';
import { useUpdateMyProfile } from '@/features/member/api/useUpdateMyProfile';
import ProfileTable from '@/features/member/components/ProfileTable';
import {
  profileSchema,
  toProfileInput,
  toProfileRequest,
  type ProfileInput,
} from '@/features/member/schemas/profileSchema';
import type { MemberProfile } from '@/features/member/types/member';
import { errorMessage } from '@/lib/apiError';

type Props = {
  profile: MemberProfile;
  onClose: () => void;
};

const inputStyle =
  'h-12 w-full rounded-[3px] border border-rule bg-white px-3.5 text-[15px] text-ink outline-none focus:border-green aria-invalid:border-danger';
const buttonStyle = 'h-[52px] rounded-[3px] text-base font-semibold';

export default function ProfileEditForm({ profile, onClose }: Props) {
  const update = useUpdateMyProfile();
  const [failed, setFailed] = useState('');
  const {
    register,
    handleSubmit,
    setValue,
    control,
    formState: { errors, isDirty },
  } = useForm<ProfileInput>({
    resolver: zodResolver(profileSchema),
    defaultValues: toProfileInput(profile),
    mode: 'onTouched',
  });
  const values = useWatch({ control }) as ProfileInput;

  const errorOf = (field: FieldPath<ProfileInput>) => errors[field]?.message;
  const setField = <K extends FieldPath<ProfileInput>>(field: K, value: string, validate = false) =>
    setValue(field, value as never, { shouldDirty: true, shouldValidate: validate });

  const submit = handleSubmit(async (input) => {
    setFailed('');

    try {
      await update.mutateAsync(toProfileRequest(input));
      onClose();
    } catch (e) {
      setFailed(errorMessage(e, messages.profileSaveFailed));
    }
  });

  const withError = (field: FieldPath<ProfileInput>, control: React.ReactNode) => (
    <>
      {control}
      {errorOf(field) && <p className="mt-1.5 text-xs text-danger">{errorOf(field)}</p>}
    </>
  );

  return (
    <form onSubmit={submit} noValidate>
      <ProfileTable
        rows={[
          {
            label: '닉네임',
            htmlFor: 'nickname',
            content: withError(
              'nickname',
              <input
                id="nickname"
                aria-invalid={errorOf('nickname') ? true : undefined}
                className={`${inputStyle} max-w-[360px]`}
                {...register('nickname')}
              />,
            ),
          },
          {
            label: '아이디',
            htmlFor: 'loginId',
            content: (
              <input
                id="loginId"
                value={profile.loginId}
                readOnly
                className={`${inputStyle} max-w-[360px] bg-band text-sub`}
              />
            ),
          },
          {
            label: '이메일',
            htmlFor: 'email',
            content: (
              <input
                id="email"
                value={profile.email}
                readOnly
                className={`${inputStyle} max-w-[360px] bg-band text-sub`}
              />
            ),
          },
          {
            label: '활동량',
            htmlFor: 'activityLevel',
            content: (
              <div className="max-w-[360px]">
                <Dropdown
                  id="activityLevel"
                  value={values.activityLevel}
                  options={activityOptions}
                  placeholder="활동량 선택"
                  heightClass="h-12"
                  onChange={(value) => setField('activityLevel', value)}
                />
              </div>
            ),
          },
          {
            label: '성별',
            content: (
              <div className="max-w-[360px]">
                <GenderButtons
                  value={values.gender}
                  heightClass="h-12"
                  onChange={(value) => setField('gender', value)}
                />
              </div>
            ),
          },
          {
            label: '생년월일',
            content: withError(
              'birthYear',
              <div className="max-w-[360px]">
                <BirthDateField
                  bare
                  heightClass="h-12"
                  year={values.birthYear}
                  month={values.birthMonth}
                  day={values.birthDay}
                  error={errorOf('birthYear')}
                  onChange={(part, value) => setField(part, value, Boolean(errors.birthYear))}
                />
              </div>,
            ),
          },
          {
            label: '키',
            htmlFor: 'height',
            content: withError(
              'height',
              <div className="flex items-center gap-2 text-[13px] text-sub">
                <input
                  id="height"
                  inputMode="decimal"
                  aria-invalid={errorOf('height') ? true : undefined}
                  className={`${inputStyle} max-w-[160px]`}
                  {...register('height')}
                />
                cm
              </div>,
            ),
          },
          {
            label: '몸무게',
            htmlFor: 'weight',
            content: withError(
              'weight',
              <div className="flex items-center gap-2 text-[13px] text-sub">
                <input
                  id="weight"
                  inputMode="decimal"
                  aria-invalid={errorOf('weight') ? true : undefined}
                  className={`${inputStyle} max-w-[160px]`}
                  {...register('weight')}
                />
                kg
              </div>,
            ),
          },
        ]}
      />
      {failed !== '' && (
        <p role="alert" className="mt-4 text-center text-[13px] text-danger">
          {failed}
        </p>
      )}
      <div className="mx-auto mt-[26px] grid max-w-[360px] grid-cols-2 gap-2">
        <button
          type="button"
          onClick={onClose}
          className={`${buttonStyle} border border-green text-green`}
        >
          취소
        </button>
        <button
          type="submit"
          disabled={!isDirty || update.isPending}
          className={`${buttonStyle} bg-green text-white disabled:bg-track disabled:text-disabled`}
        >
          저장
        </button>
      </div>
    </form>
  );
}
