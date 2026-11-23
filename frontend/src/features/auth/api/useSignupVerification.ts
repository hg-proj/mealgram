import { useMutation } from '@tanstack/react-query';
import { requestSignupCode, verifySignupCode } from '@/features/auth/api/signupVerification';

// 회원가입 이메일 인증 훅

export function useRequestSignupCode() {
  return useMutation({ mutationFn: requestSignupCode });
}

export function useVerifySignupCode() {
  return useMutation({ mutationFn: verifySignupCode });
}
