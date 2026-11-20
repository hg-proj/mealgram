import type { BodyInfoRequest } from '@/features/meal/schemas/bodyInfoSchema';
import { api } from '@/lib/axios';

// 내 정보 수정 요청

export type ProfileRequest = BodyInfoRequest & { nickname?: string };

export async function updateMyProfile(input: ProfileRequest): Promise<void> {
  await api.patch('/members/me', input);
}
