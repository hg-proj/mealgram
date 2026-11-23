import { z } from 'zod';
import { nicknameSchema } from '@/features/auth/schemas/signupSchema';
import {
  bodyInfoSchema,
  toBodyInfoInput,
  toBodyInfoRequest,
} from '@/features/meal/schemas/bodyInfoSchema';
import type { ProfileRequest } from '@/features/member/api/updateMyProfile';
import type { MemberProfile } from '@/features/member/types/member';

// 내 정보 수정 검증, 신체 정보 규칙에 닉네임을 더함

export const profileSchema = bodyInfoSchema.and(z.object({ nickname: nicknameSchema }));

export type ProfileInput = z.infer<typeof profileSchema>;

export function toProfileInput(profile: MemberProfile): ProfileInput {
  return { nickname: profile.nickname, ...toBodyInfoInput(profile) };
}

export function toProfileRequest(input: ProfileInput): ProfileRequest {
  return { nickname: input.nickname, ...toBodyInfoRequest(input) };
}
