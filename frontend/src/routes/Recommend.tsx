import RecommendFlow from '@/features/meal/components/RecommendFlow';

// 식단 추천 화면

export default function Recommend() {
  return (
    <section className="flex justify-center px-4 py-12">
      <div className="w-full max-w-[720px]">
        <h1 className="mb-[22px] text-center font-heading text-2xl leading-[1.1] font-extrabold tracking-[-0.04em]">
          식단 추천받기
        </h1>
        <RecommendFlow />
      </div>
    </section>
  );
}
