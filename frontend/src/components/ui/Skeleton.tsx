// 로딩 자리 표시

export default function Skeleton({ className = '' }: { className?: string }) {
  return <div aria-hidden="true" className={`animate-pulse rounded-[3px] bg-track ${className}`} />;
}
