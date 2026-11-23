import MyIngredientsEditor from '@/features/member/components/MyIngredientsEditor';

// 마이페이지 내 재료 화면

export default function MyIngredients() {
  return (
    <div className="max-w-[760px]">
      <h2 className="mb-3 font-heading text-xl font-extrabold tracking-[-0.03em]">내 재료</h2>
      <MyIngredientsEditor />
    </div>
  );
}
