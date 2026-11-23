import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { createMemoryRouter, RouterProvider } from 'react-router';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import MyIngredients from '@/routes/MyIngredients';
import MyPage from '@/routes/MyPage';
import MyProfile from '@/routes/MyProfile';
import { tokenStore } from '@/lib/tokenStore';

const profile = {
  nickname: '한결',
  loginId: 'hangyeol',
  email: 'hangyeol@example.com',
  birthDate: '1995-03-02',
  gender: 'MALE',
  height: 175,
  weight: 70,
  activityLevel: 'LIGHT',
};

let patched: Record<string, unknown> | null = null;
let withdrawn = false;
let mine: { id: number; ingredientId: number; name: string }[] = [];
let added: number[] = [];
let removed: string[] = [];

const server = setupServer(
  http.get('*/api/members/me', () => HttpResponse.json(profile)),
  http.patch('*/api/members/me', async ({ request }) => {
    patched = (await request.json()) as Record<string, unknown>;

    return HttpResponse.json(profile);
  }),
  http.delete('*/api/members/me', () => {
    withdrawn = true;

    return new HttpResponse(null, { status: 204 });
  }),
  http.get('*/api/ingredients/catalog', () =>
    HttpResponse.json([
      {
        category: '채소',
        ingredients: [
          { id: 1, name: '양파' },
          { id: 2, name: '당근' },
        ],
      },
    ]),
  ),
  http.get('*/api/members/me/ingredients', () => HttpResponse.json(mine)),
  http.post('*/api/members/me/ingredients', async ({ request }) => {
    const body = (await request.json()) as { ingredientId: number };
    added.push(body.ingredientId);

    return HttpResponse.json({ id: 9 }, { status: 201 });
  }),
  http.delete('*/api/members/me/ingredients/:id', ({ params }) => {
    removed.push(String(params.id));

    return new HttpResponse(null, { status: 204 });
  }),
);

beforeAll(() => server.listen({ onUnhandledFrame: 'error' }));
beforeEach(() => tokenStore.setTokens('access'));
afterEach(() => {
  server.resetHandlers();
  tokenStore.clear();
  patched = null;
  withdrawn = false;
  mine = [];
  added = [];
  removed = [];
});
afterAll(() => server.close());

function renderAt(path: string) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  const router = createMemoryRouter(
    [
      {
        path: '/mypage',
        element: <MyPage />,
        children: [
          { index: true, element: <MyProfile /> },
          { path: 'ingredients', element: <MyIngredients /> },
        ],
      },
      { path: '/', element: <p>홈 화면</p> },
    ],
    { initialEntries: [path] },
  );

  render(
    <QueryClientProvider client={client}>
      <RouterProvider router={router} />
    </QueryClientProvider>,
  );
}

describe('마이페이지 내 정보', () => {
  it('저장된 내 정보를 표로 보여 줍니다', async () => {
    renderAt('/mypage');

    expect(await screen.findByText('hangyeol@example.com')).toBeInTheDocument();
    expect(screen.getByText('1995년 3월 2일')).toBeInTheDocument();
    expect(screen.getByText('175cm')).toBeInTheDocument();
    expect(screen.getByText('저활동적')).toBeInTheDocument();
    expect(screen.queryByText('일상적인 걷기 정도')).not.toBeInTheDocument();
  });

  it('수정하기에서 바꾼 값이 없으면 저장이 비활성이고 바꾸면 저장됩니다', async () => {
    const user = userEvent.setup();
    renderAt('/mypage');

    await user.click(await screen.findByRole('button', { name: '수정하기' }));

    const save = screen.getByRole('button', { name: '저장' });
    expect(save).toBeDisabled();

    const nickname = screen.getByLabelText('닉네임');
    await user.clear(nickname);
    await user.type(nickname, '새이름');
    expect(save).toBeEnabled();
    await user.click(save);

    await waitFor(() => expect(patched).not.toBeNull());
    expect(patched).toMatchObject({ nickname: '새이름', height: 175, weight: 70 });
    expect(await screen.findByRole('button', { name: '수정하기' })).toBeInTheDocument();
  });

  it('닉네임이 너무 짧으면 저장하지 않고 안내합니다', async () => {
    const user = userEvent.setup();
    renderAt('/mypage');

    await user.click(await screen.findByRole('button', { name: '수정하기' }));
    const nickname = screen.getByLabelText('닉네임');
    await user.clear(nickname);
    await user.type(nickname, '가');
    await user.click(screen.getByRole('button', { name: '저장' }));

    expect(await screen.findByText(/2자 이상 10자 이하/)).toBeInTheDocument();
    expect(patched).toBeNull();
  });

  it('회원탈퇴를 확인하면 탈퇴하고 로그인 정보를 지웁니다', async () => {
    const user = userEvent.setup();
    renderAt('/mypage');

    await user.click(await screen.findByRole('button', { name: '회원탈퇴' }));
    expect(screen.getByText('정말 탈퇴하시겠습니까?')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: '탈퇴하기' }));

    await waitFor(() => expect(withdrawn).toBe(true));
    expect(tokenStore.getAccessToken()).toBeNull();
  });
});

describe('마이페이지 내 재료', () => {
  it('재료를 담아 저장하면 새 재료만 등록합니다', async () => {
    mine = [{ id: 5, ingredientId: 1, name: '양파' }];
    const user = userEvent.setup();
    renderAt('/mypage/ingredients');

    expect(await screen.findByRole('button', { name: '양파 빼기' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '저장' })).toBeDisabled();

    await user.click(await screen.findByRole('button', { name: '당근' }));
    await user.click(screen.getByRole('button', { name: '저장' }));

    await waitFor(() => expect(added).toEqual([2]));
    expect(removed).toEqual([]);
    expect(await screen.findByRole('button', { name: '저장 완료' })).toBeInTheDocument();
  });

  it('내 재료에서 빼고 저장하면 삭제합니다', async () => {
    mine = [{ id: 5, ingredientId: 1, name: '양파' }];
    const user = userEvent.setup();
    renderAt('/mypage/ingredients');

    await user.click(await screen.findByRole('button', { name: '양파 빼기' }));
    await user.click(screen.getByRole('button', { name: '저장' }));

    await waitFor(() => expect(removed).toEqual(['5']));
    expect(added).toEqual([]);
  });
});
