import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import { api } from '@/lib/axios';
import { tokenStore } from '@/lib/tokenStore';

let refreshCalls = 0;

const server = setupServer(
  http.get('*/api/members/me', ({ request }) =>
    request.headers.get('Authorization') === 'Bearer new-access'
      ? HttpResponse.json({ nickname: '밀그램' })
      : HttpResponse.json({}, { status: 401 }),
  ),
  http.post('*/api/auth/refresh', () => {
    refreshCalls += 1;

    return HttpResponse.json({ accessToken: 'new-access' });
  }),
);

beforeAll(() => server.listen({ onUnhandledFrame: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
beforeEach(() => {
  refreshCalls = 0;
  localStorage.clear();
});

describe('axios 인스턴스', () => {
  it('저장된 accessToken을 요청에 붙인다.', async () => {
    tokenStore.setTokens('new-access', 'refresh');

    const response = await api.get('/members/me');

    expect(response.data.nickname).toBe('밀그램');
  });

  it('401을 받으면 재발급한 뒤 같은 요청을 다시 보낸다.', async () => {
    tokenStore.setTokens('old-access', 'refresh');

    const response = await api.get('/members/me');

    expect(response.data.nickname).toBe('밀그램');
    expect(tokenStore.getAccessToken()).toBe('new-access');
  });

  it('동시에 여러 요청이 401을 받아도 재발급은 한 번만 보낸다.', async () => {
    tokenStore.setTokens('old-access', 'refresh');

    await Promise.all([api.get('/members/me'), api.get('/members/me'), api.get('/members/me')]);

    expect(refreshCalls).toBe(1);
  });

  it('재발급에 실패하면 저장된 토큰을 지운다.', async () => {
    tokenStore.setTokens('old-access', 'refresh');
    server.use(http.post('*/api/auth/refresh', () => HttpResponse.json({}, { status: 401 })));

    await expect(api.get('/members/me')).rejects.toBeTruthy();

    expect(tokenStore.getAccessToken()).toBeNull();
    expect(tokenStore.getRefreshToken()).toBeNull();
  });
});
