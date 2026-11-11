import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { act, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { MemoryRouter } from 'react-router';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import Header from '@/components/layout/Header';
import { tokenStore } from '@/lib/tokenStore';

let logoutBody: unknown = null;

const server = setupServer(
  http.post('*/api/auth/logout', async ({ request }) => {
    logoutBody = await request.json();

    return new HttpResponse(null, { status: 204 });
  }),
);

beforeAll(() => server.listen({ onUnhandledFrame: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
beforeEach(() => {
  localStorage.clear();
  logoutBody = null;
});

function renderHeader() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });

  render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <Header />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('Header', () => {
  it('로그인하지 않으면 마이페이지 메뉴를 숨기고 로그인 버튼을 보여준다.', () => {
    renderHeader();

    expect(screen.getByRole('link', { name: '식단 추천' })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: '마이페이지' })).not.toBeInTheDocument();
    expect(screen.getByRole('link', { name: '로그인' })).toHaveAttribute('href', '/login');
  });

  it('로그인하면 마이페이지 메뉴와 로그아웃 버튼을 보여준다.', () => {
    tokenStore.setTokens('access', 'refresh');

    renderHeader();

    expect(screen.getByRole('link', { name: '마이페이지' })).toHaveAttribute('href', '/mypage');
    expect(screen.getByRole('button', { name: '로그아웃' })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: '로그인' })).not.toBeInTheDocument();
  });

  it('화면을 연 채로 로그인하고 토큰을 지우면 메뉴가 바뀐다.', () => {
    renderHeader();

    act(() => tokenStore.setTokens('access', 'refresh'));
    expect(screen.getByRole('link', { name: '마이페이지' })).toBeInTheDocument();

    act(() => tokenStore.clear());
    expect(screen.queryByRole('link', { name: '마이페이지' })).not.toBeInTheDocument();
  });

  it('로그아웃을 누르면 refreshToken을 서버로 보내고 토큰을 지운다.', async () => {
    tokenStore.setTokens('access', 'refresh');
    renderHeader();

    await userEvent.click(screen.getByRole('button', { name: '로그아웃' }));

    await waitFor(() => expect(screen.getByRole('link', { name: '로그인' })).toBeInTheDocument());
    expect(logoutBody).toEqual({ refreshToken: 'refresh' });
    expect(tokenStore.getAccessToken()).toBeNull();
    expect(screen.queryByRole('link', { name: '마이페이지' })).not.toBeInTheDocument();
  });

  it('서버 호출이 실패해도 토큰은 지운다.', async () => {
    server.use(http.post('*/api/auth/logout', () => HttpResponse.json({}, { status: 500 })));
    tokenStore.setTokens('access', 'refresh');
    renderHeader();

    await userEvent.click(screen.getByRole('button', { name: '로그아웃' }));

    await waitFor(() => expect(tokenStore.getRefreshToken()).toBeNull());
    expect(screen.getByRole('link', { name: '로그인' })).toBeInTheDocument();
  });
});
