import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import TextField from '@/components/ui/TextField';
import { messages } from '@/config/messages';
import { useResetPassword } from '@/features/auth/api/useResetPassword';
import {
  newPasswordSchema,
  type NewPasswordInput,
} from '@/features/auth/schemas/resetPasswordSchema';
import { errorMessage, getApiError } from '@/lib/apiError';

// 새 비밀번호 폼

type Props = {
  resetToken: string;
  onDone: () => void;
  onRestart: () => void;
};

const fieldOrder = ['newPassword', 'newPasswordConfirm'] as const;

export default function NewPasswordForm({ resetToken, onDone, onRestart }: Props) {
  const [serverMessage, setServerMessage] = useState('');
  const [expired, setExpired] = useState(false);
  const { mutate, isPending } = useResetPassword();
  const {
    register,
    handleSubmit,
    formState: { errors, touchedFields, isSubmitted },
  } = useForm<NewPasswordInput>({
    resolver: zodResolver(newPasswordSchema),
    defaultValues: { newPassword: '', newPasswordConfirm: '' },
    mode: 'onTouched',
  });

  const shownError = fieldOrder.find(
    (field) => errors[field] && (touchedFields[field] || isSubmitted),
  );
  const errorOf = (field: (typeof fieldOrder)[number]) =>
    field === shownError ? errors[field]?.message : undefined;

  const onValid = (values: NewPasswordInput) =>
    mutate(
      { token: resetToken, newPassword: values.newPassword },
      {
        onSuccess: onDone,
        onError: (error) => {
          setExpired(getApiError(error)?.code === 'INVALID_RESET_TOKEN');
          setServerMessage(errorMessage(error, messages.resetFailed));
        },
      },
    );

  return (
    <form
      onSubmit={handleSubmit(onValid)}
      noValidate
      className="border border-t-2 border-rule border-t-green p-7"
    >
      <TextField
        id="newPassword"
        label="새 비밀번호"
        type="password"
        placeholder="영문, 숫자, 특수문자 3가지 8자, 2가지 10자 이상 입력"
        autoComplete="new-password"
        error={errorOf('newPassword')}
        registration={register('newPassword')}
      />
      <TextField
        id="newPasswordConfirm"
        label="새 비밀번호 확인"
        type="password"
        autoComplete="new-password"
        error={errorOf('newPasswordConfirm')}
        registration={register('newPasswordConfirm')}
      />
      {serverMessage && (
        <p role="alert" className="mb-3 text-[13px] text-danger">
          {serverMessage}
        </p>
      )}
      {expired ? (
        <button
          type="button"
          onClick={onRestart}
          className="mt-5 h-[58px] w-full rounded-[3px] border border-green text-base font-semibold text-green"
        >
          처음부터 다시 진행
        </button>
      ) : (
        <button
          type="submit"
          disabled={isPending}
          className="mt-5 h-[58px] w-full rounded-[3px] bg-green text-base font-semibold text-white disabled:opacity-60"
        >
          비밀번호 변경
        </button>
      )}
    </form>
  );
}
