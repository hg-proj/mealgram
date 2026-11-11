import { useState } from 'react';
import { Link } from 'react-router';
import ArrowIcon from '@/components/ui/ArrowIcon';
import { paths } from '@/config/paths';
import { heroPlates } from '@/features/home/data/heroPlates';
import { plateStyle } from '@/features/home/utils/plateStyle';

// 메인 첫 화면 두 면

const plateIds = Object.keys(heroPlates).map(Number);

function pickTwo(): [number, number] {
  const first = plateIds[Math.floor(Math.random() * plateIds.length)];
  let second = first;
  while (second === first) {
    second = plateIds[Math.floor(Math.random() * plateIds.length)];
  }

  return [first, second];
}

const panel =
  'relative flex min-w-0 grow shrink basis-1/2 flex-col justify-end overflow-hidden px-[clamp(24px,5vw,80px)] py-9 transition-[flex-basis] duration-[450ms] ease-[cubic-bezier(.3,.7,.2,1)] group-hover/split:basis-[42%] hover:basis-[58%]! max-[760px]:min-h-[340px] max-[760px]:basis-auto max-[760px]:group-hover/split:basis-auto max-[760px]:hover:basis-auto!';
const plate = 'pointer-events-none absolute aspect-square w-[min(62%,520px)] rounded-full bg-white';
const title =
  'relative z-10 font-heading text-[clamp(40px,5.2vw,76px)] leading-[1.04] font-extrabold tracking-[-0.04em]';

export default function HeroSplit() {
  const [firstId, secondId] = useState(pickTwo)[0];

  return (
    <section className="group/split flex h-[clamp(440px,40vw,560px)] overflow-hidden max-[760px]:h-auto max-[760px]:flex-col">
      <Link to={paths.ingredient} className={`${panel} group/panel bg-green text-white`}>
        <span className="absolute top-[34px] left-[clamp(24px,5vw,80px)] z-10 text-[10.5px] font-light tracking-[0.1em]">
          WHAT TO EAT TODAY
        </span>
        <div
          role="img"
          aria-label="추천 요리 사진"
          className={`${plate} top-[16%] right-[-9%]`}
          style={plateStyle(firstId)}
        />
        <span className={title}>
          내 재료로
          <br />
          추천받기
        </span>
        <ArrowIcon className="relative z-10 mt-[26px] size-[30px] transition-transform duration-200 group-hover/panel:translate-x-1.5" />
      </Link>
      <Link to={paths.mealList} className={`${panel} group/panel bg-mist text-ink`}>
        <div
          role="img"
          aria-label="추천 요리 사진"
          className={`${plate} top-[10%] right-[-6%]`}
          style={plateStyle(secondId)}
        />
        <span className={title}>
          빠르게
          <br />
          추천받기
        </span>
        <ArrowIcon className="relative z-10 mt-[26px] size-[30px] transition-transform duration-200 group-hover/panel:translate-x-1.5" />
      </Link>
    </section>
  );
}
