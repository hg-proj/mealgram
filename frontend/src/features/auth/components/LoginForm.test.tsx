import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { MemoryRouter } from 'react-router';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import LoginForm from '@/features/auth/components/LoginForm';
import { tokenStore } from '@/lib/tokenStore';

let loginBody: unknown = null;

const server = setupServer(
  http.post('*/api/auth/login', async ({ request }) => {
    loginBody = await request.json();

    return HttpResponse.json({ accessToken: 'access', refreshToken: 'refresh' });
  }),
);

beforeAll(() => server.listen({ onUnhandledFrame: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
beforeEach(() => {
  localStorage.clear();
  loginBody = null;
});

function renderForm() {
  const client = new QueryClient({ defaultOptions: { mutations: { retry: false } } });

  render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <LoginForm />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('LoginForm', () => {
  it('빈 값으로 제출하면 요청을 보내지 않고 처음 문제 하나만 보여준다.', async () => {
    renderForm();

    await userEvent.click(screen.getByRole('button', { name: '로그인' }));

    expect(await screen.findByText('아이디를 입력해 주세요.')).toBeInTheDocument();
    expect(screen.queryByText('비밀번호를 입력해 주세요.')).not.toBeInTheDocument();
    expect(loginBody).toBeNull();
  });

  it('로그인하면 토큰을 저장한다.', async () => {
    renderForm();

    await userEvent.type(screen.getByLabelText('아이디'), 'tester');
    await userEvent.type(screen.getByLabelText('비밀번호'), 'password1!');
    await userEvent.click(screen.getByRole('button', { name: '로그인' }));

    await waitFor(() => expect(tokenStore.getAccessToken()).toBe('access'));
    expect(tokenStore.getRefreshToken()).toBe('refresh');
    expect(loginBody).toEqual({ loginId: 'tester', password: 'password1!' });
  });

  it('서버가 거절하면 서버 안내 문구를 보여주고 토큰은 저장하지 않는다.', async () => {
    server.use(
      http.post('*/api/auth/login', () =>
        HttpResponse.json(
          {
            success: false,
            error: {
              code: 'INVALID_CREDENTIALS',
              message: '아이디 또는 비밀번호가 일치하지 않습니다.',
            },
          },
          { status: 401 },
        ),
      ),
    );
    renderForm();

    await userEvent.type(screen.getByLabelText('아이디'), 'tester');
    await userEvent.type(screen.getByLabelText('비밀번호'), 'wrong');
    await userEvent.click(screen.getByRole('button', { name: '로그인' }));

    expect(await screen.findByRole('alert')).toHaveTextContent(
      '아이디 또는 비밀번호가 일치하지 않습니다.',
    );
    expect(tokenStore.getAccessToken()).toBeNull();
  });
});
