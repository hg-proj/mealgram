import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { MemoryRouter } from 'react-router';
import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import QuickRecommendDialog from '@/features/meal/components/QuickRecommendDialog';
import { tokenStore } from '@/lib/tokenStore';

const server = setupServer();

beforeAll(() => server.listen({ onUnhandledFrame: 'error' }));
afterEach(() => {
  server.resetHandlers();
  tokenStore.clear();
  sessionStorage.clear();
});
afterAll(() => server.close());

function renderDialog() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });

  render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <QuickRecommendDialog onClose={vi.fn()} />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

const profile = {
  nickname: '한결',
  loginId: 'hangyeol',
  email: 'a@b.com',
  birthDate: '1995-03-02',
  gender: 'MALE',
  height: 175,
  weight: 70,
  activityLevel: 'LIGHT',
};

describe('QuickRecommendDialog', () => {
  it('비회원은 신체정보 버튼이 비활성이고 안내 문구가 보인다.', () => {
    renderDialog();

    expect(screen.getByRole('button', { name: '완전 랜덤으로 추천받기' })).toBeEnabled();
    expect(screen.getByRole('button', { name: '내 신체정보로 추천받기' })).toBeDisabled();
    expect(screen.getByText('로그인하면 신체정보로 추천받을 수 있습니다.')).toBeInTheDocument();
  });

  it('완전 랜덤은 신체정보 없이 균형 성향과 랜덤 목표로 요청한다.', async () => {
    let body: Record<string, unknown> | null = null;
    server.use(
      http.post('*/api/meals', async ({ request }) => {
        body = (await request.json()) as Record<string, unknown>;

        return HttpResponse.json({ id: null, relaxed: false, candidates: [] }, { status: 201 });
      }),
    );
    renderDialog();

    await userEvent.click(screen.getByRole('button', { name: '완전 랜덤으로 추천받기' }));

    await waitFor(() => expect(body).not.toBeNull());
    expect(body).toMatchObject({ subIds: [], style: '균형' });
    expect(body!.goal).toBe('랜덤');
    expect(body).not.toHaveProperty('birthDate');
    expect(sessionStorage.getItem('mealContext')).toContain('"genre":null');
  });

  it('저장된 신체정보가 있는 회원은 그 정보로 요청한다.', async () => {
    tokenStore.setTokens('access');
    let body: Record<string, unknown> | null = null;
    server.use(
      http.get('*/api/members/me', () => HttpResponse.json(profile)),
      http.post('*/api/meals', async ({ request }) => {
        body = (await request.json()) as Record<string, unknown>;

        return HttpResponse.json({ id: 'm1', relaxed: false, candidates: [] }, { status: 201 });
      }),
    );
    renderDialog();

    const button = screen.getByRole('button', { name: '내 신체정보로 추천받기' });
    await waitFor(() => expect(button).toBeEnabled());
    await userEvent.click(button);

    await waitFor(() => expect(body).not.toBeNull());
    expect(body).toMatchObject({ height: 175, weight: 70, gender: 'MALE', activityLevel: 'LIGHT' });
  });

  it('저장된 신체정보가 없는 회원은 안내 문구가 보인다.', async () => {
    tokenStore.setTokens('access');
    server.use(
      http.get('*/api/members/me', () =>
        HttpResponse.json({
          ...profile,
          birthDate: null,
          gender: null,
          height: null,
          weight: null,
          activityLevel: null,
        }),
      ),
    );
    renderDialog();

    expect(await screen.findByText('저장된 신체정보가 없습니다.')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '내 신체정보로 추천받기' })).toBeDisabled();
  });
});
