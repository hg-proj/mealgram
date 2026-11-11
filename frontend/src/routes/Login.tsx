import { Navigate, useLocation } from 'react-router';
import { paths } from '@/config/paths';
import LoginForm from '@/features/auth/components/LoginForm';
import { useAuth } from '@/features/auth/hooks/useAuth';

// 로그인 화면

export default function Login() {
  const { isLoggedIn } = useAuth();
  const signedUp = (useLocation().state as { signedUp?: boolean } | null)?.signedUp;

  if (isLoggedIn) {
    return <Navigate to={paths.home} replace />;
  }

  return (
    <section className="flex min-h-[520px] flex-1 items-center justify-center px-4 py-14">
      <div className="w-full max-w-[420px]">
        <h1 className="mb-[26px] text-center font-heading text-2xl leading-[1.1] font-extrabold tracking-[-0.04em]">
          로그인
        </h1>
        {signedUp && (
          <p role="status" className="mb-3.5 text-center text-[13px] text-green">
            가입이 완료되었습니다. 로그인해 주세요.
          </p>
        )}
        <LoginForm />
      </div>
    </section>
  );
}
