import { Link } from 'react-router';
import { paths } from '@/config/paths';

// 추천받은 식단이 없을 때 안내

export default function NoMealNotice() {
  return (
    <section className="flex justify-center px-4 py-20">
      <div className="text-center">
        <h1 className="mb-3 font-heading text-2xl font-extrabold tracking-[-0.04em]">
          추천받은 식단이 없습니다
        </h1>
        <p className="mb-6 text-sm text-sub">
          신체 정보와 목표, 재료를 고르면 식단을 추천해 드립니다.
        </p>
        <Link
          to={paths.ingredient}
          className="inline-block rounded-[3px] bg-green px-6 py-3 text-sm font-semibold text-white"
        >
          식단 추천받기
        </Link>
      </div>
    </section>
  );
}
