import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router';
import { inputStyle } from '@/components/ui/inputStyle';
import { messages } from '@/config/messages';
import { paths } from '@/config/paths';
import { useLogin } from '@/features/auth/api/useLogin';
import { loginSchema, type LoginInput } from '@/features/auth/schemas/loginSchema';
import { errorMessage } from '@/lib/apiError';

// 로그인 폼

export default function LoginForm() {
  const { mutate, isPending, error } = useLogin();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginInput>({ resolver: zodResolver(loginSchema) });
  const firstError = errors.loginId ? 'loginId' : errors.password ? 'password' : null;

  return (
    <div className="border border-t-2 border-rule border-t-green">
      <form
        onSubmit={handleSubmit((values) => mutate(values))}
        noValidate
        className="px-7 pt-[30px] pb-[26px]"
      >
        <label htmlFor="loginId" className="sr-only">
          아이디
        </label>
        <input
          id="loginId"
          type="text"
          autoComplete="username"
          placeholder="아이디"
          aria-invalid={firstError === 'loginId' ? true : undefined}
          className={inputStyle}
          {...register('loginId')}
        />
        {firstError === 'loginId' && (
          <p className="mt-1.5 text-xs text-danger">{errors.loginId?.message}</p>
        )}
        <label htmlFor="password" className="sr-only">
          비밀번호
        </label>
        <input
          id="password"
          type="password"
          autoComplete="current-password"
          placeholder="비밀번호"
          aria-invalid={firstError === 'password' ? true : undefined}
          className={`${inputStyle} mt-2`}
          {...register('password')}
        />
        {firstError === 'password' && (
          <p className="mt-1.5 text-xs text-danger">{errors.password?.message}</p>
        )}
        {error && (
          <p role="alert" className="mt-3 text-[13px] text-danger">
            {errorMessage(error, messages.loginFailed)}
          </p>
        )}
        <button
          type="submit"
          disabled={isPending}
          className="mt-3.5 h-[58px] w-full rounded-[3px] bg-green text-base font-semibold text-white disabled:opacity-60"
        >
          로그인
        </button>
        <div className="mt-3.5 flex items-center justify-end gap-2 text-[12.5px] text-sub">
          <Link to={paths.forgotPassword}>비밀번호 찾기</Link>
          <span aria-hidden="true" className="h-2.5 w-px bg-rule" />
          <Link to={paths.signup}>회원가입</Link>
        </div>
      </form>
    </div>
  );
}
