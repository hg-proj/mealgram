import { api } from '@/lib/axios';
import type { MemberProfile } from '@/features/member/types/member';

// 내 정보 조회 요청

export async function getMyProfile(): Promise<MemberProfile> {
  const response = await api.get<MemberProfile>('/members/me');

  return response.data;
}
