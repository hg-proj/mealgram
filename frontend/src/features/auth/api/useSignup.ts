import { useMutation } from '@tanstack/react-query';
import { signup } from '@/features/auth/api/signup';

// 회원가입 훅

export function useSignup() {
  return useMutation({ mutationFn: signup });
}
