import { useEffect, useRef } from 'react';
import Modal from '@/components/ui/Modal';

// 회원탈퇴 확인 창

type Props = {
  pending: boolean;
  error: string;
  onConfirm: () => void;
  onClose: () => void;
};

export default function WithdrawDialog({ pending, error, onConfirm, onClose }: Props) {
  const cancelButton = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    cancelButton.current?.focus();
  }, []);

  return (
    <Modal titleId="withdraw-title" onClose={onClose} widthClass="max-w-[400px]">
      <h2 id="withdraw-title" className="font-heading text-lg font-extrabold tracking-[-0.03em]">
        정말 탈퇴하시겠습니까?
      </h2>
      <p className="mt-2.5 mb-[22px] text-sm break-keep">
        탈퇴하면 저장한 식단과 내 정보가 모두 삭제됩니다.
      </p>
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
          탈퇴하기
        </button>
      </div>
    </Modal>
  );
}
