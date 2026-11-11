import type { CSSProperties } from 'react';
import { heroPlates, PLATE_IMAGE_SIZE } from '@/features/home/data/heroPlates';

// 접시 안쪽만 보이게 자르는 배경 설정

const FIT = 0.95;

export function plateStyle(id: number): CSSProperties {
  const [x, y, r] = heroPlates[id];
  const { width, height } = PLATE_IMAGE_SIZE;
  const scale = 1 / (2 * FIT * r);

  return {
    backgroundImage: `url(/images/recipes/${id}.webp)`,
    backgroundRepeat: 'no-repeat',
    backgroundSize: `${width * scale * 100}% ${height * scale * 100}%`,
    backgroundPosition: `${((0.5 - x * scale) / (1 - width * scale)) * 100}% ${((0.5 - y * scale) / (1 - height * scale)) * 100}%`,
  };
}
