import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { MemoryRouter } from 'react-router';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import MyMeals from '@/routes/MyMeals';
import type { SavedMeal } from '@/features/savedMeal/types/savedMeal';
import { tokenStore } from '@/lib/tokenStore';

const nutrition = {
  calorie: 667,
  carbohydrate: 80,
  protein: 30,
  fat: 20,
  carbohydrateRatio: 57,
  proteinRatio: 21,
  fatRatio: 23,
  targetCalorie: 700,
  verified: true,
  passed: true,
  issues: [],
};

function meal(id: number, style: string, dishNames: string[]): SavedMeal {
  return {
    id,
    style,
    dishes: dishNames.map((name, i) => ({
      id: id * 10 + i,
      name,
      category: i === 0 ? '밥' : '국',
      imageUrl: null,
      grams: 100,
      calorie: 100,
    })),
    nutrition,
    context: { goal: '유지', style: '균형', genre: '한식', requiredName: null, ownedNames: [] },
    createdAt: '2026-11-10T12:00:00',
  };
}

let saved: SavedMeal[] = [];
let deletedId: string | null = null;
let deleteStatus = 204;

const server = setupServer(
  http.get('*/api/saved-meals', () => HttpResponse.json(saved)),
  http.delete('*/api/saved-meals/:id', ({ params }) => {
    if (deleteStatus !== 204) {
      return HttpResponse.json(
        { success: false, error: { code: 'ERROR', message: '삭제 실패' } },
        { status: deleteStatus },
      );
    }
    deletedId = String(params.id);
    saved = saved.filter((item) => String(item.id) !== deletedId);

    return new HttpResponse(null, { status: 204 });
  }),
);

beforeAll(() => server.listen({ onUnhandledFrame: 'error' }));
beforeEach(() => {
  tokenStore.setTokens('access');
  saved = [
    meal(1, '든든한 한식 한 상', ['흑미밥', '된장찌개']),
    meal(2, '가벼운 한 그릇 식단', ['잡곡밥', '미역국']),
  ];
  deletedId = null;
  deleteStatus = 204;
});
afterEach(() => {
  server.resetHandlers();
  tokenStore.clear();
});
afterAll(() => server.close());

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });

  render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <MyMeals />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('MyMeals', () => {
  it('저장한 식단 이름과 요리를 보여준다.', async () => {
    renderPage();

    expect(await screen.findByText('든든한 한식 한 상')).toBeInTheDocument();
    expect(screen.getByText('가벼운 한 그릇 식단')).toBeInTheDocument();
    expect(screen.getByText('흑미밥, 된장찌개')).toBeInTheDocument();
  });

  it('저장한 식단이 없으면 안내와 추천 링크가 보인다.', async () => {
    saved = [];
    renderPage();

    expect(await screen.findByText('아직 담은 식단이 없습니다.')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: '식단 추천받기' })).toBeInTheDocument();
  });

  it('검색하면 이름이나 요리 이름이 맞는 식단만 남고 맞는 글자가 굵게 보인다.', async () => {
    renderPage();
    await screen.findByText('든든한 한식 한 상');

    await userEvent.type(screen.getByRole('textbox', { name: '식단 검색' }), '된장');

    expect(screen.queryByText('가벼운 한 그릇 식단')).not.toBeInTheDocument();
    expect(screen.getByText('든든한 한식 한 상')).toBeInTheDocument();
    expect(screen.getByText('된장', { selector: 'b' })).toBeInTheDocument();
  });

  it('맞는 식단이 없으면 검색 결과 없음을 알린다.', async () => {
    renderPage();
    await screen.findByText('든든한 한식 한 상');

    await userEvent.type(screen.getByRole('textbox', { name: '식단 검색' }), '없는요리');

    expect(screen.getByText('검색 결과가 없습니다.')).toBeInTheDocument();
  });

  it('삭제를 누르면 확인 창이 뜨고 취소하면 식단이 남는다.', async () => {
    renderPage();
    await userEvent.click(await screen.findByRole('button', { name: '든든한 한식 한 상 삭제' }));

    const dialog = screen.getByRole('dialog');
    expect(within(dialog).getByText('식단을 삭제하시겠습니까?')).toBeInTheDocument();

    await userEvent.click(within(dialog).getByRole('button', { name: '취소' }));

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    expect(deletedId).toBeNull();
    expect(screen.getByText('든든한 한식 한 상')).toBeInTheDocument();
  });

  it('확인 창에서 삭제하면 서버에 요청하고 목록에서 사라진다.', async () => {
    renderPage();
    await userEvent.click(await screen.findByRole('button', { name: '든든한 한식 한 상 삭제' }));

    await userEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: '삭제' }));

    await waitFor(() => expect(screen.queryByText('든든한 한식 한 상')).not.toBeInTheDocument());
    expect(deletedId).toBe('1');
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('삭제에 실패하면 창을 닫지 않고 오류를 보여준다.', async () => {
    deleteStatus = 500;
    renderPage();
    await userEvent.click(await screen.findByRole('button', { name: '든든한 한식 한 상 삭제' }));

    await userEvent.click(within(screen.getByRole('dialog')).getByRole('button', { name: '삭제' }));

    expect(await screen.findByRole('alert')).toBeInTheDocument();
    expect(screen.getByRole('dialog')).toBeInTheDocument();
  });
});
