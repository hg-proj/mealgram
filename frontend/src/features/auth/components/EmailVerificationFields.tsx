import type { ReactNode } from 'react';
import { Field } from '@/components/ui/TextField';
import { inputStyle } from '@/components/ui/inputStyle';
import type { EmailVerification } from '@/features/auth/hooks/useEmailVerification';
import { formatClock } from '@/hooks/useCountdown';

// 이메일 칸과 인증 버튼과 인증번호 칸

type Props = {
  verification: EmailVerification;
  emailInput: ReactNode;
  emailError?: string;
  onSend: () => void;
  onConfirm: () => void;
};

const smallButton =
  'h-[56px] w-[88px] rounded-[3px] border border-green text-sm font-semibold text-green disabled:border-track disabled:bg-track disabled:text-disabled';
const smallFilledButton = `${smallButton} bg-green text-white`;
const lockedInput =
  'disabled:border-track disabled:bg-track disabled:text-disabled disabled:placeholder:text-disabled read-only:border-track read-only:bg-track read-only:text-disabled';

export default function EmailVerificationFields({
  verification: v,
  emailInput,
  emailError,
  onSend,
  onConfirm,
}: Props) {
  return (
    <>
      <Field id="email" label="이메일" error={emailError}>
        <div className="grid grid-cols-[1fr_88px] gap-2">
          {emailInput}
          <button
            type="button"
            onClick={onSend}
            disabled={v.phase !== 'idle' || v.sending}
            className={smallButton}
          >
            인증
          </button>
        </div>
      </Field>

      <div className="mt-[18px]">
        <label htmlFor="code" className="mb-1.5 block text-[13px] font-semibold">
          인증번호
        </label>
        <div className="grid grid-cols-[1fr_88px] gap-2">
          <div className="relative">
            <input
              id="code"
              type="text"
              inputMode="numeric"
              maxLength={6}
              autoComplete="one-time-code"
              placeholder="인증번호 6자리 입력"
              value={v.code}
              onChange={(e) => v.setCode(e.target.value)}
              disabled={v.phase === 'idle'}
              readOnly={v.phase === 'verified'}
              aria-invalid={v.codeError ? true : undefined}
              className={`${inputStyle} ${lockedInput} ${v.phase === 'sent' ? 'pr-16' : ''}`}
            />
            {v.phase === 'sent' && (
              <span className="absolute inset-y-0 right-3.5 flex items-center text-[13px] font-semibold text-danger tabular-nums">
                {formatClock(v.expiry.left)}
              </span>
            )}
          </div>
          <button
            type="button"
            onClick={onConfirm}
            disabled={v.phase !== 'sent' || v.expired || v.verifying}
            className={smallFilledButton}
          >
            확인
          </button>
        </div>
        <div className="mt-2 flex min-h-5 items-center justify-between gap-2 text-[12.5px]">
          <p
            role={v.line?.tone === 'error' ? 'alert' : undefined}
            className={v.line?.tone === 'error' ? 'text-danger' : 'text-green'}
          >
            {v.line?.text}
          </p>
          {v.phase === 'sent' &&
            (v.resend.left > 0 ? (
              <span className="whitespace-nowrap text-sub tabular-nums">
                다시 보내기 ({v.resend.left})
              </span>
            ) : (
              <button
                type="button"
                onClick={onSend}
                disabled={v.sending}
                className="whitespace-nowrap text-green underline"
              >
                다시 보내기
              </button>
            ))}
        </div>
      </div>
    </>
  );
}
