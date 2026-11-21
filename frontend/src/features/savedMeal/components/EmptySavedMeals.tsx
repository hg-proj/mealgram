import { Link } from 'react-router';
import forkImage from '@/assets/fork.svg';
import knifeImage from '@/assets/knife.svg';
import { messages } from '@/config/messages';
import { paths } from '@/config/paths';
import PlateImage from '@/features/home/components/PlateImage';

// 저장한 식단이 없을 때 화면

// 포크와 칼 이미지, Phosphor Icons fork-knife fill, MIT
const cutlery = 'h-[clamp(96px,14vw,190px)] w-auto';

export default function EmptySavedMeals() {
  return (
    <section className="group mx-auto flex min-h-[520px] w-full max-w-[1140px] flex-1 flex-col items-center justify-center px-4 py-10 text-center">
      <span className="mb-2.5 block text-[10.5px] tracking-[0.1em] text-green">NO SAVED MEALS</span>
      <div className="flex items-center justify-center gap-[clamp(10px,3vw,34px)]">
        <img src={forkImage} alt="" className={cutlery} />
        <PlateImage className="size-[clamp(86px,14vw,187px)] grayscale-[0.7] brightness-[1.06] transition-[filter] duration-300 group-hover:filter-none motion-safe:animate-[sway_5s_ease-in-out_infinite]" />
        <img src={knifeImage} alt="" className={cutlery} />
      </div>
      <h1 className="mt-[18px] font-heading text-[clamp(24px,2.6vw,34px)] leading-[1.1] font-extrabold tracking-[-0.04em]">
        {messages.notice.noSavedMeals}
      </h1>
      <Link
        to={paths.ingredient}
        className="mt-7 rounded-[3px] bg-green px-[26px] py-[13px] text-sm font-semibold text-white"
      >
        식단 추천받기
      </Link>
    </section>
  );
}
