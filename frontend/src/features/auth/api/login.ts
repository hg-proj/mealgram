import type { LoginInput } from '@/features/auth/schemas/loginSchema';
import { api } from '@/lib/axios';
import { tokenStore } from '@/lib/tokenStore';

// 로그인 요청

type LoginResponse = { accessToken: string; refreshToken: string };

export async function login(input: LoginInput): Promise<void> {
  const { data } = await api.post<LoginResponse>('/auth/login', input);

  tokenStore.setTokens(data.accessToken, data.refreshToken);
}
