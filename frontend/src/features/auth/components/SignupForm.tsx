import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState, type KeyboardEvent } from 'react';
import { useForm, useWatch, type FieldErrors, type FieldPath } from 'react-hook-form';
import { useNavigate } from 'react-router';
import TextField from '@/components/ui/TextField';
import { inputStyle } from '@/components/ui/inputStyle';
import { messages } from '@/config/messages';
import { paths } from '@/config/paths';
import { useSignup } from '@/features/auth/api/useSignup';
import {
  useRequestSignupCode,
  useVerifySignupCode,
} from '@/features/auth/api/useSignupVerification';
import BodyInfoFields from '@/features/auth/components/BodyInfoFields';
import EmailVerificationFields from '@/features/auth/components/EmailVerificationFields';
import TermsAgreement from '@/features/auth/components/TermsAgreement';
import { useEmailVerification } from '@/features/auth/hooks/useEmailVerification';
import {
  signupDefaults,
  signupSchema,
  type SignupInput,
} from '@/features/auth/schemas/signupSchema';
import { errorMessage, getApiError } from '@/lib/apiError';

// 회원가입 폼, 1단계 계정 정보와 2단계 신체 정보

type Field = FieldPath<SignupInput>;

const accountFields: Field[] = [
  'nickname',
  'loginId',
  'password',
  'passwordConfirm',
  'email',
  'agreeTerms',
  'agreePrivacy',
];

const fieldOrder: Field[] = [...accountFields, 'activityLevel', 'birthYear', 'height', 'weight'];

const buttonBase = 'h-[58px] rounded-[3px] text-base font-semibold';
const solid = `${buttonBase} bg-green text-white disabled:opacity-60`;
const outline = `${buttonBase} border border-green text-green disabled:opacity-60`;

export default function SignupForm() {
  const navigate = useNavigate();
  const [step, setStep] = useState<1 | 2>(1);
  const [serverMessage, setServerMessage] = useState('');
  const [flagged, setFlagged] = useState<Field | null>(null);
  const { mutate, isPending } = useSignup();
  const {
    register,
    handleSubmit,
    setError,
    clearErrors,
    getValues,
    trigger,
    setValue,
    control,
    formState: { errors, touchedFields },
  } = useForm<SignupInput>({
    resolver: zodResolver(signupSchema),
    defaultValues: signupDefaults,
    mode: 'onTouched',
  });

  const requestCode = useRequestSignupCode();
  const verifyCode = useVerifySignupCode();
  const verification = useEmailVerification<void>({
    send: (email) =>
      requestCode.mutateAsync({ email, nickname: getValues('nickname').trim() || undefined }),
    verify: (email, code) => verifyCode.mutateAsync({ email, code }),
    emailErrorCodes: ['DUPLICATE_EMAIL'],
  });

  const [agreeTerms, agreePrivacy, gender, activityLevel, birthYear, birthMonth, birthDay] =
    useWatch({
      control,
      name: [
        'agreeTerms',
        'agreePrivacy',
        'gender',
        'activityLevel',
        'birthYear',
        'birthMonth',
        'birthDay',
      ],
    });

  const shownError = fieldOrder.find(
    (field) => errors[field] && (touchedFields[field] || flagged === field),
  );
  const errorOf = (field: Field) => (field === shownError ? errors[field]?.message : undefined);

  useEffect(() => {
    if (verification.phase === 'verified') {
      clearErrors('email');
    }
  }, [verification.phase, clearErrors]);

  const sendCode = async () => {
    if (!(await trigger('email'))) {
      setFlagged('email');

      return;
    }

    await verification.sendCode(getValues('email'), (message) => {
      setFlagged('email');
      setError('email', { message });
    });
  };

  const confirmCode = () => verification.confirmCode(getValues('email'));

  const requireVerified = () => {
    if (verification.phase === 'verified') {
      return true;
    }
    setStep(1);
    setFlagged('email');
    setError('email', { message: messages.validation.emailNotVerified });

    return false;
  };

  const toggleAll = (checked: boolean) => {
    setValue('agreeTerms', checked, { shouldValidate: true });
    setValue('agreePrivacy', checked, { shouldValidate: true });
  };

  const onInvalid = (invalid: FieldErrors<SignupInput>) => {
    const first = fieldOrder.find((field) => invalid[field]) ?? null;
    setFlagged(first);

    if (first && !accountFields.includes(first)) {
      setStep(2);
    }
  };

  const showServerError = (error: unknown) => {
    const body = getApiError(error);
    setServerMessage('');

    if (body?.code === 'DUPLICATE_LOGIN_ID') {
      setStep(1);
      setFlagged('loginId');
      setError('loginId', { message: body.message });
    } else if (body?.code === 'EMAIL_NOT_VERIFIED') {
      verification.restart();
      setStep(1);
      setFlagged('email');
      setError('email', { message: body.message });
    } else if (body?.code === 'DUPLICATE_EMAIL') {
      setStep(1);
      setFlagged('email');
      setError('email', { message: body.message });
    } else if (body?.fieldErrors?.length) {
      body.fieldErrors.forEach(({ field, message }) => setError(field as Field, { message }));
      setFlagged(body.fieldErrors[0].field as Field);
      setStep(accountFields.includes(body.fieldErrors[0].field as Field) ? 1 : 2);
    } else {
      setServerMessage(errorMessage(error, messages.signupFailed));
    }
  };

  const goNext = handleSubmit(() => {
    if (requireVerified()) {
      setStep(2);
    }
  }, onInvalid);

  const onEnter = (e: KeyboardEvent<HTMLFormElement>) => {
    const target = e.target;
    if (e.key !== 'Enter' || !(target instanceof HTMLInputElement) || target.type === 'checkbox') {
      return;
    }
    e.preventDefault();

    if (target.id === 'email' && verification.phase !== 'verified') {
      if (verification.phase === 'idle') {
        void sendCode();
      }

      return;
    }
    if (target.id === 'code' && verification.phase === 'sent') {
      void confirmCode();

      return;
    }

    const inputs = [
      ...e.currentTarget.querySelectorAll<HTMLInputElement>(
        `[data-step="${step}"] input:not([type="checkbox"])`,
      ),
    ];
    const next = inputs[inputs.indexOf(target) + 1];

    if (next) {
      next.focus();
    } else if (step === 1) {
      void goNext();
    } else {
      void handleSubmit(onValid, onInvalid)();
    }
  };

  const onValid = (values: SignupInput) => {
    if (!requireVerified()) {
      return;
    }

    mutate(values, {
      onSuccess: () => navigate(paths.login, { state: { signedUp: true } }),
      onError: showServerError,
    });
  };

  return (
    <div>
      <div className="mb-[18px] flex justify-center gap-2 text-[12.5px] text-sub">
        <span className={step === 1 ? 'font-semibold text-green' : ''}>1 계정 정보</span>
        <span aria-hidden="true">›</span>
        <span className={step === 2 ? 'font-semibold text-green' : ''}>2 신체 정보 (선택)</span>
      </div>
      <form
        onSubmit={handleSubmit(onValid, onInvalid)}
        onKeyDown={onEnter}
        noValidate
        className="border border-t-2 border-rule border-t-green p-7"
      >
        <div data-step="1" className={step === 1 ? '' : 'hidden'}>
          <TextField
            id="nickname"
            label="닉네임"
            placeholder="2자 이상 입력"
            error={errorOf('nickname')}
            registration={register('nickname')}
          />
          <TextField
            id="loginId"
            label="아이디"
            placeholder="영문 소문자, 숫자 6자 이상 입력"
            autoComplete="username"
            error={errorOf('loginId')}
            registration={register('loginId')}
          />
          <TextField
            id="password"
            label="비밀번호"
            type="password"
            placeholder="영문, 숫자, 특수문자 3가지 8자, 2가지 10자 이상 입력"
            autoComplete="new-password"
            error={errorOf('password')}
            registration={register('password')}
          />
          <TextField
            id="passwordConfirm"
            label="비밀번호 확인"
            type="password"
            autoComplete="new-password"
            error={errorOf('passwordConfirm')}
            registration={register('passwordConfirm')}
          />
          <div className="mb-3.5">
            <EmailVerificationFields
              verification={verification}
              emailError={errorOf('email')}
              onSend={() => void sendCode()}
              onConfirm={() => void confirmCode()}
              emailInput={
                <input
                  id="email"
                  type="email"
                  placeholder="비밀번호 찾기에 사용"
                  autoComplete="email"
                  aria-invalid={errorOf('email') ? true : undefined}
                  className={inputStyle}
                  {...register('email', { onChange: verification.restart })}
                />
              }
            />
          </div>
          <TermsAgreement
            register={register}
            allChecked={agreeTerms && agreePrivacy}
            onToggleAll={toggleAll}
            error={errorOf('agreeTerms') ?? errorOf('agreePrivacy')}
          />
        </div>

        <div data-step="2" className={step === 2 ? '' : 'hidden'}>
          <BodyInfoFields
            register={register}
            errorOf={errorOf}
            gender={gender}
            activityLevel={activityLevel}
            onGender={(value) => setValue('gender', value)}
            onActivity={(value) => setValue('activityLevel', value)}
            birthYear={birthYear}
            birthMonth={birthMonth}
            birthDay={birthDay}
            onBirth={(part, value) =>
              setValue(part, value, { shouldValidate: Boolean(errors.birthYear) })
            }
          />
        </div>

        {serverMessage && (
          <p role="alert" className="mb-3 text-[13px] text-danger">
            {serverMessage}
          </p>
        )}
        {step === 1 ? (
          <>
            <div className="grid grid-cols-2 gap-2">
              <button type="submit" disabled={isPending} className={outline}>
                가입
              </button>
              <button type="button" onClick={goNext} className={solid}>
                다음
              </button>
            </div>
            <p className="mt-3.5 text-center text-[12.5px] text-sub">
              신체 정보는 마이페이지에서도 입력할 수 있습니다.
            </p>
          </>
        ) : (
          <>
            <div className="grid grid-cols-2 gap-2">
              <button type="button" onClick={() => setStep(1)} className={outline}>
                이전
              </button>
              <button type="submit" disabled={isPending} className={solid}>
                가입
              </button>
            </div>
            <p className="mt-3.5 text-center text-[12.5px] text-sub">
              식단 추천에 사용합니다. 미기재 시 기본 열량으로 추천해 드립니다.
            </p>
          </>
        )}
      </form>
    </div>
  );
}
