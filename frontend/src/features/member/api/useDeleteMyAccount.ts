import { useMutation, useQueryClient } from '@tanstack/react-query';
import { deleteMyAccount } from '@/features/member/api/deleteMyAccount';
import { tokenStore } from '@/lib/tokenStore';

export function useDeleteMyAccount() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: deleteMyAccount,
    onSuccess: () => {
      tokenStore.clear();
      queryClient.clear();
    },
  });
}
