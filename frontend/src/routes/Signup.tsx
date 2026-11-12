import { Navigate } from 'react-router';
import { paths } from '@/config/paths';
import SignupForm from '@/features/auth/components/SignupForm';
import { useAuth } from '@/features/auth/hooks/useAuth';

// 회원가입 화면

export default function Signup() {
  const { isLoggedIn } = useAuth();

  if (isLoggedIn) {
    return <Navigate to={paths.home} replace />;
  }

  return (
    <section className="flex justify-center px-4 py-12">
      <div className="w-full max-w-[420px]">
        <h1 className="mb-[22px] text-center font-heading text-2xl leading-[1.1] font-extrabold tracking-[-0.04em]">
          회원가입
        </h1>
        <SignupForm />
      </div>
    </section>
  );
}
