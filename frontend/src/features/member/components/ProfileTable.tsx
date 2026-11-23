import type { ReactNode } from 'react';

type Row = { label: string; htmlFor?: string; content: ReactNode };

export default function ProfileTable({ rows }: { rows: Row[] }) {
  return (
    <div className="border-b border-line">
      {rows.map((row) => (
        <div
          key={row.label}
          className="grid grid-cols-[160px_1fr] border-t border-line first:border-t-0 max-[620px]:grid-cols-[96px_1fr]"
        >
          <div className="flex items-center bg-band px-5 py-4 text-[13.5px] font-semibold max-[620px]:px-3">
            {row.htmlFor ? <label htmlFor={row.htmlFor}>{row.label}</label> : row.label}
          </div>
          <div className="flex min-h-[56px] min-w-0 flex-col justify-center [overflow-wrap:anywhere] px-5 py-2.5 text-[15px] max-[620px]:px-3">
            {row.content}
          </div>
        </div>
      ))}
    </div>
  );
}
