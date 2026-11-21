import { useEffect, useRef } from 'react';
import Modal from '@/components/ui/Modal';
import type { SavedMeal } from '@/features/savedMeal/types/savedMeal';

// 저장한 식단 삭제 확인 창

type Props = {
  meal: SavedMeal;
  pending: boolean;
  error: string;
  onConfirm: () => void;
  onClose: () => void;
};

export default function DeleteMealDialog({ meal, pending, error, onConfirm, onClose }: Props) {
  const cancelButton = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    cancelButton.current?.focus();
  }, []);

  return (
    <Modal titleId="delete-meal-title" onClose={onClose} widthClass="max-w-[400px]">
      <h2 id="delete-meal-title" className="font-heading text-lg font-extrabold tracking-[-0.03em]">
        식단을 삭제하시겠습니까?
      </h2>
      <p className="mt-2.5 mb-[22px] text-sm font-bold break-keep">{meal.style}</p>
      {error !== '' && (
        <p role="alert" className="mb-3.5 text-[13px] text-danger">
          {error}
        </p>
      )}
      <div className="flex justify-end gap-2">
        <button
          ref={cancelButton}
          type="button"
          onClick={onClose}
          className="h-9 min-w-[84px] rounded-[3px] border border-green bg-white px-4 text-[13px] font-bold text-green"
        >
          취소
        </button>
        <button
          type="button"
          onClick={onConfirm}
          disabled={pending}
          className="h-9 min-w-[84px] rounded-[3px] border border-green bg-green px-4 text-[13px] font-bold text-white disabled:border-rule disabled:bg-band disabled:text-disabled"
        >
          삭제
        </button>
      </div>
    </Modal>
  );
}
