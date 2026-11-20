import type { DragEvent } from 'react';

// 재료 칸 모양과 끌기 동작, 식단 추천과 내 재료가 함께 사용

export type BoxState = 'none' | 'sub' | 'required';

const box =
  'inline-flex h-[34px] cursor-grab items-center rounded-[3px] border px-3 text-[13px] select-none';
const boxStyle: Record<BoxState, string> = {
  none: 'border-rule bg-white text-ink',
  sub: 'border-green bg-green-soft font-semibold text-green-d',
  required: 'border-green bg-green font-semibold text-white',
};

export const ingredientBox = (state: BoxState) => `${box} ${boxStyle[state]}`;

// 끌 때 보이는 모양을 이 재료 하나로 한정
export function startIngredientDrag(e: DragEvent<HTMLButtonElement>, id: number) {
  e.dataTransfer.setData('text/plain', String(id));
  e.dataTransfer.effectAllowed = 'move';

  const ghost = e.currentTarget.cloneNode(true) as HTMLElement;
  ghost.style.cssText = 'position:fixed;top:-100px;left:-100px;';
  document.body.appendChild(ghost);
  e.dataTransfer.setDragImage(ghost, ghost.offsetWidth / 2, ghost.offsetHeight / 2);
  setTimeout(() => ghost.remove(), 0);
}
