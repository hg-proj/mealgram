type Props = {
  src: string;
  className?: string;
};

export default function MaskIcon({ src, className = '' }: Props) {
  return (
    <span
      aria-hidden="true"
      className={`inline-block bg-current [mask-position:center] [mask-repeat:no-repeat] [mask-size:contain] ${className}`}
      style={{ maskImage: `url("${src}")`, WebkitMaskImage: `url("${src}")` }}
    />
  );
}
