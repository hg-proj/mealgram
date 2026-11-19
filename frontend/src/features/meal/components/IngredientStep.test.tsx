import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import IngredientStep from '@/features/meal/components/IngredientStep';
import { tokenStore } from '@/lib/tokenStore';

let mine: { id: number; ingredientId: number; name: string }[] = [];

const server = setupServer(
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
  http.post('*/api/members/me/ingredients', () => HttpResponse.json({ id: 1 }, { status: 201 })),
);

beforeAll(() => server.listen({ onUnhandledFrame: 'error' }));
afterEach(() => {
  server.resetHandlers();
  tokenStore.clear();
  mine = [];
});
afterAll(() => server.close());

function renderStep() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });

  render(
    <QueryClientProvider client={client}>
      <IngredientStep
        picked={{ requiredId: null, subIds: [1] }}
        onPick={vi.fn()}
        onPrev={vi.fn()}
        onSubmit={vi.fn()}
        pending={false}
        error=""
      />
    </QueryClientProvider>,
  );
}

describe('IngredientStep', () => {
  it('저장한 내 재료가 없으면 버튼 글자가 잠시 내 재료 미존재로 바뀐다.', async () => {
    tokenStore.setTokens('access');
    renderStep();

    await userEvent.click(await screen.findByRole('button', { name: '내 재료 불러오기' }));

    expect(await screen.findByRole('button', { name: '내 재료 미존재' })).toBeInTheDocument();
  });

  it('내 재료를 불러오면 버튼 글자가 잠시 내 재료 불러오기 완료로 바뀐다.', async () => {
    tokenStore.setTokens('access');
    mine = [{ id: 5, ingredientId: 2, name: '당근' }];
    renderStep();

    await userEvent.click(await screen.findByRole('button', { name: '내 재료 불러오기' }));

    expect(
      await screen.findByRole('button', { name: '내 재료 불러오기 완료' }),
    ).toBeInTheDocument();
  });

  it('재료를 저장하면 버튼 글자가 잠시 재료 저장 완료로 바뀐다.', async () => {
    tokenStore.setTokens('access');
    renderStep();

    await userEvent.click(await screen.findByRole('button', { name: '재료 저장하기' }));

    expect(await screen.findByRole('button', { name: '재료 저장 완료' })).toBeInTheDocument();
  });
});
