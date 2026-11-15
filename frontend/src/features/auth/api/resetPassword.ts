import { api } from '@/lib/axios';

type VerifyResetCodeResponse = { resetToken: string };

export async function requestResetCode(email: string): Promise<void> {
  await api.post('/auth/forgot-password', { email });
}

export async function verifyResetCode(input: { email: string; code: string }): Promise<string> {
  const { data } = await api.post<VerifyResetCodeResponse>('/auth/verify-reset-code', input);

  return data.resetToken;
}

export async function resetPassword(input: { token: string; newPassword: string }): Promise<void> {
  await api.post('/auth/reset-password', input);
}
