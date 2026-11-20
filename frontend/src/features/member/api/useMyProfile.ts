import { useQuery } from '@tanstack/react-query';
import { getMyProfile } from '@/features/member/api/getMyProfile';
import { memberKeys } from '@/features/member/api/keys';

// 내 정보 조회 훅

export function useMyProfile(enabled: boolean) {
  return useQuery({
    queryKey: memberKeys.me,
    queryFn: getMyProfile,
    enabled,
  });
}
