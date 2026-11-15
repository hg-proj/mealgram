import { useState } from 'react';
import { Link, Navigate } from 'react-router';
import { paths } from '@/config/paths';
import NewPasswordForm from '@/features/auth/components/NewPasswordForm';
import VerifyEmailForm from '@/features/auth/components/VerifyEmailForm';
import { useAuth } from '@/features/auth/hooks/useAuth';

type Stage = 'verify' | 'password' | 'done';

const titles: Record<Stage, string> = {
  verify: '비밀번호 찾기',
  password: '새 비밀번호 설정',
  done: '비밀번호 변경',
};

export default function ForgotPassword() {
  const { isLoggedIn } = useAuth();
  const [stage, setStage] = useState<Stage>('verify');
  const [resetToken, setResetToken] = useState('');

  if (isLoggedIn && stage !== 'done') {
    return <Navigate to={paths.home} replace />;
  }

  return (
    <section className="flex justify-center px-4 py-12">
      <div className="w-full max-w-[420px]">
        <h1 className="mb-[22px] text-center font-heading text-2xl leading-[1.1] font-extrabold tracking-[-0.04em]">
          {titles[stage]}
        </h1>
        {stage === 'verify' && (
          <VerifyEmailForm
            onVerified={(token) => {
              setResetToken(token);
              setStage('password');
            }}
          />
        )}
        {stage === 'password' && (
          <NewPasswordForm
            resetToken={resetToken}
            onDone={() => setStage('done')}
            onRestart={() => {
              setResetToken('');
              setStage('verify');
            }}
          />
        )}
        {stage === 'done' && (
          <div className="border border-t-2 border-rule border-t-green p-7">
            <p className="mb-2.5 font-heading text-xl leading-[1.1] font-extrabold tracking-[-0.03em]">
              비밀번호를 변경했습니다.
            </p>
            <p className="text-sm leading-[1.7]">새 비밀번호로 로그인해 주세요.</p>
            <Link
              to={paths.login}
              className="mt-[22px] block h-[58px] rounded-[3px] bg-green text-center text-base leading-[58px] font-semibold text-white"
            >
              로그인
            </Link>
          </div>
        )}
      </div>
    </section>
  );
}
