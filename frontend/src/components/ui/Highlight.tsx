import type { ReactNode } from 'react';

export default function Highlight({ text, keyword }: { text: string; keyword: string }) {
  const needle = keyword.trim().toLowerCase();

  if (needle === '') {
    return <>{text}</>;
  }

  const lower = text.toLowerCase();
  const parts: ReactNode[] = [];
  let from = 0;

  for (let at = lower.indexOf(needle); at !== -1; at = lower.indexOf(needle, from)) {
    parts.push(text.slice(from, at));
    from = at + needle.length;
    parts.push(
      <b key={at} className="bg-green-soft font-extrabold text-green-d">
        {text.slice(at, from)}
      </b>,
    );
  }
  parts.push(text.slice(from));

  return <>{parts}</>;
}
