import { zodResolver } from '@hookform/resolvers/zod';
import type { FormEvent } from 'react';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router';
import { inputStyle } from '@/components/ui/inputStyle';
import { paths } from '@/config/paths';
import { useRequestResetCode, useVerifyResetCode } from '@/features/auth/api/useResetPassword';
import EmailVerificationFields from '@/features/auth/components/EmailVerificationFields';
import { useEmailVerification } from '@/features/auth/hooks/useEmailVerification';
import { verifyEmailSchema } from '@/features/auth/schemas/resetPasswordSchema';

// 비밀번호 찾기 이메일 인증 폼, 인증번호 받기와 확인

const emailSchema = verifyEmailSchema.pick({ email: true });

export default function VerifyEmailForm({
  onVerified,
}: {
  onVerified: (resetToken: string) => void;
}) {
  const requestCode = useRequestResetCode();
  const verifyCode = useVerifyResetCode();
  const verification = useEmailVerification<string>({
    send: requestCode.mutateAsync,
    verify: (email, code) => verifyCode.mutateAsync({ email, code }),
    emailErrorCodes: ['EMAIL_NOT_REGISTERED'],
  });
  const {
    register,
    trigger,
    getValues,
    setError,
    formState: { errors },
  } = useForm<{ email: string }>({
    resolver: zodResolver(emailSchema),
    defaultValues: { email: '' },
    mode: 'onTouched',
  });

  const sendCode = async () => {
    if (!(await trigger('email'))) {
      return;
    }

    await verification.sendCode(getValues('email'), (message) => setError('email', { message }));
  };

  const confirmCode = () => verification.confirmCode(getValues('email'));

  const onSubmit = (e: FormEvent) => {
    e.preventDefault();

    if (verification.phase === 'idle') {
      void sendCode();
    } else if (verification.phase === 'sent') {
      void confirmCode();
    } else {
      onVerified(verification.result ?? '');
    }
  };

  return (
    <div>
      <form
        onSubmit={onSubmit}
        noValidate
        className="border border-t-2 border-rule border-t-green p-7"
      >
        <EmailVerificationFields
          verification={verification}
          emailError={errors.email?.message}
          onSend={() => void sendCode()}
          onConfirm={() => void confirmCode()}
          emailInput={
            <input
              id="email"
              type="email"
              placeholder="가입한 이메일"
              autoComplete="email"
              aria-invalid={errors.email ? true : undefined}
              className={inputStyle}
              {...register('email', { onChange: verification.restart })}
            />
          }
        />

        <button
          type="submit"
          disabled={verification.phase !== 'verified'}
          className="mt-[22px] h-[58px] w-full rounded-[3px] bg-green text-base font-semibold text-white disabled:bg-track disabled:text-disabled"
        >
          다음
        </button>
      </form>
      <Link to={paths.login} className="mt-4 block text-center text-[12.5px] text-sub">
        로그인으로 돌아가기
      </Link>
    </div>
  );
}
