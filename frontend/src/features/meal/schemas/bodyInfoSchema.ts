import { z } from 'zod';
import { messages } from '@/config/messages';
import { birthDateIssue, inRange } from '@/features/auth/schemas/signupSchema';
import type { MemberProfile } from '@/features/member/types/member';

// 신체 정보 입력 검증, 회원가입의 신체 정보와 같은 규칙

export const bodyInfoSchema = z
  .object({
    birthYear: z.string(),
    birthMonth: z.string(),
    birthDay: z.string(),
    gender: z.enum(['', 'MALE', 'FEMALE']),
    height: z.string().refine(inRange(50, 250), messages.validation.height),
    weight: z.string().refine(inRange(10, 300), messages.validation.weight),
    activityLevel: z.enum(['', 'LOW', 'LIGHT', 'MODERATE', 'HIGH']),
  })
  .superRefine((v, ctx) => {
    const issue = birthDateIssue(v.birthYear, v.birthMonth, v.birthDay);

    if (issue) {
      ctx.addIssue({ code: 'custom', path: ['birthYear'], message: messages.validation[issue] });
    }
  });

export type BodyInfoInput = z.infer<typeof bodyInfoSchema>;

export const bodyInfoDefaults: BodyInfoInput = {
  birthYear: '',
  birthMonth: '',
  birthDay: '',
  gender: '',
  height: '',
  weight: '',
  activityLevel: '',
};

export type BodyInfoRequest = {
  birthDate?: string;
  gender?: 'MALE' | 'FEMALE';
  height?: number;
  weight?: number;
  activityLevel?: 'LOW' | 'LIGHT' | 'MODERATE' | 'HIGH';
};

export function hasBodyInfo(input: BodyInfoInput): boolean {
  return Object.values(input).some((value) => value !== '');
}

export function toBodyInfoRequest(input: BodyInfoInput): BodyInfoRequest {
  const request: BodyInfoRequest = {};

  if (input.birthYear !== '') {
    request.birthDate = [
      input.birthYear,
      input.birthMonth.padStart(2, '0'),
      input.birthDay.padStart(2, '0'),
    ].join('-');
  }
  if (input.gender !== '') request.gender = input.gender;
  if (input.height !== '') request.height = Number(input.height);
  if (input.weight !== '') request.weight = Number(input.weight);
  if (input.activityLevel !== '') request.activityLevel = input.activityLevel;

  return request;
}

// 저장된 내 정보를 입력칸 값으로 변환
export function toBodyInfoInput(profile: MemberProfile): BodyInfoInput {
  const [year = '', month = '', day = ''] = profile.birthDate?.split('-') ?? [];

  return {
    birthYear: year,
    birthMonth: month === '' ? '' : String(Number(month)),
    birthDay: day === '' ? '' : String(Number(day)),
    gender: profile.gender ?? '',
    height: profile.height === null ? '' : String(profile.height),
    weight: profile.weight === null ? '' : String(profile.weight),
    activityLevel: profile.activityLevel ?? '',
  };
}
