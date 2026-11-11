import { useMutation } from '@tanstack/react-query';
import { login } from '@/features/auth/api/login';

// 로그인 훅

export function useLogin() {
  return useMutation({ mutationFn: login });
}
