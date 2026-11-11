import ingredientImage from '@/assets/steps/ingredient.webp';
import recipeImage from '@/assets/steps/recipe.webp';
import recommendImage from '@/assets/steps/recommend.webp';

// 이용 방법 안내

const steps = [
  {
    title: '재료 입력',
    lines: ['냉장고 속 재료를 체크하고', '목표와 장르를 선택합니다.'],
    image: ingredientImage,
    shift: '',
  },
  {
    title: '식단 추천받기',
    lines: ['선택한 조건에 맞춰', '3가지 식단을 알려드립니다.'],
    image: recommendImage,
    shift: 'ml-[clamp(0px,7vw,96px)] max-[960px]:ml-[clamp(0px,8vw,72px)] max-[560px]:ml-0',
  },
  {
    title: '레시피와 장보기',
    lines: ['상세 레시피부터 장보기 목록,', '영양 분석까지 한 번에!'],
    image: recipeImage,
    shift: 'ml-[clamp(0px,14vw,192px)] max-[960px]:ml-[clamp(0px,16vw,144px)] max-[560px]:ml-0',
  },
];

export default function HowToUse() {
  return (
    <section className="bg-band px-4">
      <div className="mx-auto grid max-w-[1140px] grid-cols-[minmax(200px,4fr)_8fr] items-start gap-[clamp(24px,5vw,80px)] py-[72px] max-[960px]:grid-cols-1 max-[960px]:gap-7">
        <div>
          <span className="mb-2 block text-[10.5px] tracking-[0.1em] text-green">HOW TO USE</span>
          <h2 className="font-heading text-[clamp(30px,3.4vw,48px)] leading-[1.05] font-extrabold tracking-[-0.04em] max-[960px]:whitespace-nowrap">
            밀그램의
            <br className="max-[960px]:hidden" /> 식단 추천
          </h2>
        </div>
        <ol>
          {steps.map((step) => (
            <li
              key={step.title}
              className={`flex items-center gap-[clamp(14px,2.4vw,32px)] border-t border-rule py-[26px] last:border-b ${step.shift}`}
            >
              <img
                src={step.image}
                alt=""
                width={560}
                height={440}
                className="h-auto w-[clamp(64px,7vw,100px)] flex-none"
              />
              <div>
                <b className="mb-2 block font-heading text-[clamp(18px,1.8vw,24px)] font-bold tracking-[-0.03em]">
                  {step.title}
                </b>
                <p className="text-sm leading-[1.7] text-sub">
                  {step.lines[0]}
                  <br />
                  {step.lines[1]}
                </p>
              </div>
            </li>
          ))}
        </ol>
      </div>
    </section>
  );
}
