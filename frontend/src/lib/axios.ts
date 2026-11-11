import axios, { type InternalAxiosRequestConfig } from 'axios';
import { tokenStore } from '@/lib/tokenStore';

// axios 인스턴스, 토큰 첨부, 401 재발급

type RetriableConfig = InternalAxiosRequestConfig & { retried?: boolean };

export const api = axios.create({ baseURL: '/api' });

api.interceptors.request.use((config) => {
  const token = tokenStore.getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let reissuing: Promise<string> | null = null;

function reissueAccessToken(): Promise<string> {
  reissuing ??= axios
    .post<{ accessToken: string }>('/api/auth/refresh', {
      refreshToken: tokenStore.getRefreshToken(),
    })
    .then((res) => {
      tokenStore.setTokens(res.data.accessToken);
      return res.data.accessToken;
    })
    .finally(() => {
      reissuing = null;
    });

  return reissuing;
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const config = error.config as RetriableConfig | undefined;
    const refreshable = tokenStore.getRefreshToken() !== null;

    if (error.response?.status === 401 && config && !config.retried && refreshable) {
      config.retried = true;
      try {
        const token = await reissueAccessToken();
        config.headers.Authorization = `Bearer ${token}`;
        return api(config);
      } catch {
        tokenStore.clear();
      }
    }

    return Promise.reject(error);
  },
);
