import { api } from '@/lib/axios';
import { tokenStore } from '@/lib/tokenStore';

// 로그아웃 요청

export async function logout(): Promise<void> {
  const refreshToken = tokenStore.getRefreshToken();

  try {
    if (refreshToken) {
      await api.post('/auth/logout', { refreshToken });
    }
  } finally {
    tokenStore.clear();
  }
}
