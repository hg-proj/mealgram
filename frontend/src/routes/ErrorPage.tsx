import { useState } from 'react';
import { Link } from 'react-router';
import { paths } from '@/config/paths';
import { heroPlates } from '@/features/home/data/heroPlates';
import { plateStyle } from '@/features/home/utils/plateStyle';

// 에러 화면

const plateIds = Object.keys(heroPlates).map(Number);

const texts: Record<number, { eyebrow: string; title: string; description: string }> = {
  404: {
    eyebrow: 'PAGE NOT FOUND',
    title: '찾으시는 페이지가 없습니다',
    description: '주소가 바뀌었거나 아직 준비 중인 화면입니다.',
  },
  500: {
    eyebrow: 'SOMETHING WENT WRONG',
    title: '화면을 불러오지 못했습니다',
    description: '잠시 후 다시 시도해 주세요.',
  },
};

const fallback = {
  eyebrow: 'ERROR',
  title: '알 수 없는 오류가 발생했습니다',
  description: '잠시 후 다시 시도해 주세요.',
};

export default function ErrorPage({ status }: { status: number }) {
  const [plateId] = useState(() => plateIds[Math.floor(Math.random() * plateIds.length)]);
  const text = texts[status] ?? fallback;
  const digits = String(status).padStart(3, '0');

  return (
    <section className="mx-auto flex min-h-[520px] w-full max-w-[1140px] flex-1 flex-col items-center justify-center px-4 py-10 text-center">
      <span className="mb-2.5 block text-[10.5px] tracking-[0.1em] text-green">{text.eyebrow}</span>
      <div
        className="flex items-center gap-[clamp(6px,1.5vw,18px)] font-heading text-[clamp(110px,18vw,240px)] leading-none font-extrabold tracking-[-0.04em] text-green"
        aria-label={String(status)}
      >
        <span aria-hidden="true">{digits[0]}</span>
        <span
          aria-hidden="true"
          className="size-[0.78em] rounded-full bg-white shadow-plate"
          style={plateStyle(plateId)}
        />
        <span aria-hidden="true">{digits[2]}</span>
      </div>
      <h1 className="mt-[18px] font-heading text-[clamp(24px,2.6vw,34px)] leading-[1.1] font-extrabold tracking-[-0.04em]">
        {text.title}
      </h1>
      <p className="mt-3 text-[15px] text-sub">{text.description}</p>
      <div className="mt-7 flex flex-wrap justify-center gap-2.5">
        <Link
          to={paths.home}
          className="rounded-[3px] bg-green px-[26px] py-[13px] text-sm font-semibold text-white"
        >
          홈으로
        </Link>
        <Link
          to={paths.ingredient}
          className="rounded-[3px] border border-green px-[26px] py-[13px] text-sm font-semibold text-green"
        >
          식단 추천받기
        </Link>
      </div>
    </section>
  );
}
