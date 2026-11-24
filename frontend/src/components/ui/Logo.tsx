import leaf from '@/assets/logo-leaf.png';

export default function Logo({ className = '' }: { className?: string }) {
  return (
    <span
      className={`flex items-start font-logo leading-none font-semibold tracking-[-0.01em] text-green ${className}`}
    >
      mealgram
      <img src={leaf} alt="" className="-mt-[0.1em] ml-px size-[0.5em] object-contain" />
    </span>
  );
}
