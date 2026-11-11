import Skeleton from '@/components/ui/Skeleton';
import { useMealSamples } from '@/features/meal/api/useMealSamples';
import SampleMealCard from '@/features/meal/components/SampleMealCard';
import { sampleFallback } from '@/features/meal/data/sampleFallback';

// 오늘의 추천 식단 구간

const FEATURED_GOAL = '유지';

export default function SampleMeals() {
  const { data, isPending } = useMealSamples();
  const samples = data && data.length > 0 ? data : sampleFallback;
  const featured = samples.find((sample) => sample.goal === FEATURED_GOAL) ?? samples[0];
  const others = samples.filter((sample) => sample !== featured);

  return (
    <section className="px-4">
      <div className="mx-auto max-w-[1140px] pt-[72px] pb-14">
        <span className="mb-2 block text-[10.5px] tracking-[0.1em] text-sub">TODAY'S MEAL</span>
        <h2 className="mb-9 font-heading text-[clamp(30px,3.4vw,48px)] leading-[1.05] font-extrabold tracking-[-0.04em]">
          오늘의 추천 식단
        </h2>
        {isPending ? (
          <div className="grid grid-cols-2 gap-[18px] max-[860px]:grid-cols-1" aria-busy="true">
            <Skeleton className="row-span-2 min-h-[420px] max-[860px]:row-auto max-[860px]:min-h-[160px]" />
            <Skeleton className="min-h-[160px]" />
            <Skeleton className="min-h-[160px]" />
          </div>
        ) : (
          <div className="grid grid-cols-2 grid-rows-2 gap-[18px] max-[860px]:grid-cols-1 max-[860px]:grid-rows-none">
            <SampleMealCard sample={featured} featured />
            {others.map((sample) => (
              <SampleMealCard key={sample.goal} sample={sample} />
            ))}
          </div>
        )}
      </div>
    </section>
  );
}
