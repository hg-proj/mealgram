import type { SignupInput } from '@/features/auth/schemas/signupSchema';
import { api } from '@/lib/axios';

// 회원가입 요청

export type SignupRequest = {
  nickname: string;
  loginId: string;
  password: string;
  email: string;
  birthDate?: string;
  gender?: 'MALE' | 'FEMALE';
  height?: number;
  weight?: number;
  activityLevel?: 'LOW' | 'LIGHT' | 'MODERATE' | 'HIGH';
};

export function toSignupRequest(input: SignupInput): SignupRequest {
  const request: SignupRequest = {
    nickname: input.nickname,
    loginId: input.loginId,
    password: input.password,
    email: input.email,
  };

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

export async function signup(input: SignupInput): Promise<void> {
  await api.post('/auth/signup', toSignupRequest(input));
}
