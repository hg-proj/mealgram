import { useMutation, useQueryClient } from '@tanstack/react-query';
import { login } from '@/features/auth/api/login';

// 로그인 훅, 성공하면 이전 계정의 조회 결과를 비움

export function useLogin() {
  const queryClient = useQueryClient();

  return useMutation({ mutationFn: login, onSuccess: () => queryClient.clear() });
}
