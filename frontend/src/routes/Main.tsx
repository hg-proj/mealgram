import HeroSplit from '@/features/home/components/HeroSplit';
import HowToUse from '@/features/home/components/HowToUse';
import SampleMeals from '@/features/meal/components/SampleMeals';

// 메인 화면

export default function Main() {
  return (
    <>
      <HeroSplit />
      <HowToUse />
      <SampleMeals />
    </>
  );
}
