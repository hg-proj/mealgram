import arrowIcon from '@/assets/arrow.svg';
import MaskIcon from '@/components/ui/MaskIcon';

// 오른쪽 화살표 아이콘

export default function ArrowIcon({ className = '' }: { className?: string }) {
  return <MaskIcon src={arrowIcon} className={className} />;
}
