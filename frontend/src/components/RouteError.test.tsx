import { render, screen } from '@testing-library/react';
import { createMemoryRouter, RouterProvider } from 'react-router';
import { describe, expect, it } from 'vitest';
import RouteError from '@/components/RouteError';

function renderWithError(error: Error) {
  const router = createMemoryRouter(
    [
      {
        path: '/',
        errorElement: <RouteError />,
        loader: () => {
          throw error;
        },
        element: <p>정상 화면</p>,
      },
    ],
    { initialEntries: ['/'] },
  );

  render(<RouterProvider router={router} />);
}

describe('화면 오류', () => {
  it('화면 파일을 받지 못하면 새로고침을 안내한다.', async () => {
    renderWithError(new TypeError('Failed to fetch dynamically imported module: /assets/Main.js'));

    expect(await screen.findByText('새 버전이 배포되었습니다')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '새로고침' })).toBeInTheDocument();
  });

  it('그 밖의 오류는 일반 오류 화면을 보여 준다.', async () => {
    renderWithError(new Error('알 수 없는 오류'));

    expect(await screen.findByText('화면을 불러오지 못했습니다')).toBeInTheDocument();
  });
});
