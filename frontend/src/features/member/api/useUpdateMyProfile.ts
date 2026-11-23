import { useMutation, useQueryClient } from '@tanstack/react-query';
import { memberKeys } from '@/features/member/api/keys';
import { updateMyProfile } from '@/features/member/api/updateMyProfile';

export function useUpdateMyProfile() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: updateMyProfile,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: memberKeys.me }),
  });
}
