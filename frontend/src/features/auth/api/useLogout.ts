import { useMutation, useQueryClient } from '@tanstack/react-query';
import { logout } from '@/features/auth/api/logout';

// 로그아웃 훅

export function useLogout() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: logout,
    onSettled: () => queryClient.clear(),
  });
}
