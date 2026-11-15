import { useMutation } from '@tanstack/react-query';
import {
  requestResetCode,
  resetPassword,
  verifyResetCode,
} from '@/features/auth/api/resetPassword';

// 비밀번호 재설정 훅

export function useRequestResetCode() {
  return useMutation({ mutationFn: requestResetCode });
}

export function useVerifyResetCode() {
  return useMutation({ mutationFn: verifyResetCode });
}

export function useResetPassword() {
  return useMutation({ mutationFn: resetPassword });
}
