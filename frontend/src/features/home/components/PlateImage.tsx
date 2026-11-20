import { useState } from 'react';
import { heroPlates } from '@/features/home/data/heroPlates';
import { plateStyle } from '@/features/home/utils/plateStyle';

// 음식 사진 속 접시를 동그랗게 잘라 보여줌

const plateIds = Object.keys(heroPlates).map(Number);

export default function PlateImage({ className = '' }: { className?: string }) {
  const [plateId] = useState(() => plateIds[Math.floor(Math.random() * plateIds.length)]);

  return (
    <span
      aria-hidden="true"
      className={`rounded-full bg-white shadow-plate ${className}`}
      style={plateStyle(plateId)}
    />
  );
}
