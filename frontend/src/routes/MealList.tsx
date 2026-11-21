import { messages } from '@/config/messages';
import DetailNotice from '@/features/meal/components/DetailNotice';
import MealCandidateCard from '@/features/meal/components/MealCandidateCard';
import NoMealNotice from '@/features/meal/components/NoMealNotice';
import { resultStore } from '@/features/meal/lib/resultStore';

// 추천 식단 목록 화면

export default function MealList() {
  const result = resultStore.read();
  const context = resultStore.readContext();

  if (result === null || result.candidates.length === 0) {
    return <NoMealNotice />;
  }

  return (
    <section className="px-4 py-12">
      <div className="mx-auto max-w-[1140px]">
        <h1 className="mb-2 text-center font-heading text-2xl font-extrabold tracking-[-0.04em]">
          추천 식단
        </h1>
        {result.relaxed && (
          <p className="mb-2 text-center text-[13px] text-sub">{messages.notice.relaxedMeal}</p>
        )}
        <div className="mt-8 grid grid-cols-3 gap-[18px] max-[960px]:grid-cols-2 max-[620px]:grid-cols-1">
          {result.candidates.map((candidate, index) => (
            <MealCandidateCard
              key={candidate.style + index}
              candidate={candidate}
              index={index}
              mealId={result.id}
              context={context}
            />
          ))}
        </div>
        <div className="mt-8 text-center text-[13px] text-sub">
          <DetailNotice />
        </div>
      </div>
    </section>
  );
}
