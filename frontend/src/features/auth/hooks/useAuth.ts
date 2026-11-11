import { useSyncExternalStore } from 'react';
import { tokenStore } from '@/lib/tokenStore';

// 로그인 상태 조회 훅

export function useAuth() {
  const isLoggedIn = useSyncExternalStore(
    tokenStore.subscribe,
    () => tokenStore.getAccessToken() !== null,
  );

  return { isLoggedIn };
}
