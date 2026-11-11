import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render, screen } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';
import { MemoryRouter } from 'react-router';
import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest';
import SampleMeals from '@/features/meal/components/SampleMeals';
import { handlers } from '@/test/handlers';

const server = setupServer(...handlers);

beforeAll(() => server.listen({ onUnhandledFrame: 'error' }));
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

function renderSection() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } });

  render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <SampleMeals />
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('SampleMeals', () => {
  it('서버가 준 식단을 보여주고 유지 식단을 대표로 둔다.', async () => {
    renderSection();

    expect(await screen.findByText('서버 유지 식단')).toBeInTheDocument();
    expect(screen.getByText('서버 다이어트 식단')).toBeInTheDocument();
    expect(screen.getByText('서버 벌크업 식단')).toBeInTheDocument();
    expect(screen.getByText('601')).toBeInTheDocument();
    expect(screen.getByText('잡곡밥 / 미역국 / 제육보쌈')).toBeInTheDocument();
  });

  it('영양 정보가 없으면 칼로리와 비율을 하이픈으로 보여준다.', async () => {
    renderSection();

    await screen.findByText('서버 유지 식단');

    expect(screen.getAllByText('탄 -').length).toBeGreaterThan(0);
  });

  it('서버 호출이 실패하면 예시 식단을 보여준다.', async () => {
    server.use(http.get('*/api/meals/samples', () => HttpResponse.json({}, { status: 500 })));

    renderSection();

    expect(await screen.findByText('든든한 한 끼 식단')).toBeInTheDocument();
  });
});
