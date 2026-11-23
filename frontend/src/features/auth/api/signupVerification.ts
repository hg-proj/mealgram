import { api } from '@/lib/axios';

export async function requestSignupCode(input: {
  email: string;
  nickname?: string;
}): Promise<void> {
  await api.post('/auth/signup/send-code', input);
}

export async function verifySignupCode(input: { email: string; code: string }): Promise<void> {
  await api.post('/auth/signup/verify-code', input);
}
