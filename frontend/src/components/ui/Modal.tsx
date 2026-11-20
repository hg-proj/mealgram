import { useEffect, type ReactNode } from 'react';

type Props = {
  titleId: string;
  onClose: () => void;
  children: ReactNode;
  widthClass?: string;
};

export default function Modal({ titleId, onClose, children, widthClass = 'max-w-[500px]' }: Props) {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose();
      }
    };

    window.addEventListener('keydown', onKey);

    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-ink/45 px-4"
      onClick={onClose}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        className={`w-full bg-white px-6 pt-[26px] pb-[22px] ${widthClass}`}
        onClick={(e) => e.stopPropagation()}
      >
        {children}
      </div>
    </div>
  );
}
