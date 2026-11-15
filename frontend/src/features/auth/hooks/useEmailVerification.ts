import { useState } from 'react';
import { messages } from '@/config/messages';
import { useCountdown } from '@/hooks/useCountdown';
import { errorMessage, getApiError } from '@/lib/apiError';

// 이메일 인증 진행 상태

export type Phase = 'idle' | 'sent' | 'verified';
export type Notice = { tone: 'info' | 'error'; text: string };

const CODE_VALID_SECONDS = 600;
const RESEND_SECONDS = 60;
const CODE_PATTERN = /^[0-9]{6}$/;

type Options<T> = {
  send: (email: string) => Promise<unknown>;
  verify: (email: string, code: string) => Promise<T>;
  emailErrorCodes: string[];
};

export function useEmailVerification<T>({ send, verify, emailErrorCodes }: Options<T>) {
  const [phase, setPhase] = useState<Phase>('idle');
  const [result, setResult] = useState<T | null>(null);
  const [code, setCodeValue] = useState('');
  const [codeError, setCodeError] = useState('');
  const [notice, setNotice] = useState<Notice | null>(null);
  const [sending, setSending] = useState(false);
  const [verifying, setVerifying] = useState(false);
  const expiry = useCountdown();
  const resend = useCountdown();

  const expired = phase === 'sent' && expiry.left === 0;

  const setCode = (value: string) => {
    setCodeValue(value.replace(/[^0-9]/g, ''));
    setCodeError('');
  };

  const restart = () => {
    if (phase === 'idle') {
      return;
    }
    setPhase('idle');
    setResult(null);
    setNotice(null);
    setCode('');
    expiry.stop();
    resend.stop();
  };

  const sendCode = async (email: string, onEmailError: (message: string) => void) => {
    setSending(true);

    try {
      await send(email);
      setPhase('sent');
      setNotice({ tone: 'info', text: messages.notice.resetCodeSent });
      setCode('');
      expiry.start(CODE_VALID_SECONDS);
      resend.start(RESEND_SECONDS);
    } catch (error) {
      const body = getApiError(error);

      if (body && emailErrorCodes.includes(body.code)) {
        onEmailError(body.message);
      } else {
        setNotice({ tone: 'error', text: errorMessage(error, messages.resetSendFailed) });
      }
    } finally {
      setSending(false);
    }
  };

  const confirmCode = async (email: string) => {
    if (!CODE_PATTERN.test(code)) {
      setCodeError(
        code === '' ? messages.validation.resetCodeRequired : messages.validation.resetCode,
      );

      return;
    }
    setVerifying(true);

    try {
      setResult(await verify(email, code));
      setPhase('verified');
      setNotice({ tone: 'info', text: messages.notice.resetCodeVerified });
      expiry.stop();
      resend.stop();
    } catch (error) {
      setNotice({ tone: 'error', text: errorMessage(error, messages.resetVerifyFailed) });
    } finally {
      setVerifying(false);
    }
  };

  const line: Notice | null = expired
    ? { tone: 'error', text: messages.notice.resetCodeExpired }
    : codeError
      ? { tone: 'error', text: codeError }
      : notice;

  return {
    phase,
    result,
    code,
    codeError,
    line,
    expired,
    expiry,
    resend,
    sending,
    verifying,
    setCode,
    restart,
    sendCode,
    confirmCode,
  };
}

export type EmailVerification<T = unknown> = ReturnType<typeof useEmailVerification<T>>;
