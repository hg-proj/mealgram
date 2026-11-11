import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router';
import { describe, expect, it } from 'vitest';
import HeroSplit from '@/features/home/components/HeroSplit';

describe('HeroSplit', () => {
  it('두 면이 각각 알맞은 주소로 연결된다.', () => {
    render(
      <MemoryRouter>
        <HeroSplit />
      </MemoryRouter>,
    );

    expect(screen.getByRole('link', { name: /내 재료로/ })).toHaveAttribute('href', '/ingredients');
    expect(screen.getByRole('link', { name: /빠르게/ })).toHaveAttribute('href', '/meals');
  });

  it('두 면에 서로 다른 접시 사진을 보여준다.', () => {
    render(
      <MemoryRouter>
        <HeroSplit />
      </MemoryRouter>,
    );

    const plates = screen.getAllByRole('img', { name: '추천 요리 사진' });

    expect(plates).toHaveLength(2);
    expect((plates[0] as HTMLElement).style.backgroundImage).not.toBe(
      (plates[1] as HTMLElement).style.backgroundImage,
    );
  });
});
