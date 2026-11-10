// 토큰 저장과 삭제

const ACCESS_KEY = 'accessToken';
const REFRESH_KEY = 'refreshToken';

function read(key: string): string | null {
  try {
    return localStorage.getItem(key);
  } catch {
    return null;
  }
}

function write(key: string, value: string | null) {
  try {
    if (value === null) {
      localStorage.removeItem(key);
    } else {
      localStorage.setItem(key, value);
    }
  } catch {
    // 저장소를 쓸 수 없는 환경에서는 저장하지 않음
  }
}

const listeners = new Set<() => void>();

function notify() {
  listeners.forEach((listener) => listener());
}

export const tokenStore = {
  getAccessToken: () => read(ACCESS_KEY),
  getRefreshToken: () => read(REFRESH_KEY),
  setTokens(accessToken: string, refreshToken?: string) {
    write(ACCESS_KEY, accessToken);
    if (refreshToken) {
      write(REFRESH_KEY, refreshToken);
    }
    notify();
  },
  clear() {
    write(ACCESS_KEY, null);
    write(REFRESH_KEY, null);
    notify();
  },
  subscribe(listener: () => void) {
    listeners.add(listener);
    window.addEventListener('storage', listener);

    return () => {
      listeners.delete(listener);
      window.removeEventListener('storage', listener);
    };
  },
};
