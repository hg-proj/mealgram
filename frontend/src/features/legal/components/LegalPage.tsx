import { NavLink } from 'react-router';
import { paths } from '@/config/paths';
import { site } from '@/config/site';
import type { LegalBlock, LegalDocument } from '@/features/legal/types/legal';

// 서비스 소개, 이용약관, 개인정보처리방침 공통 문서 화면

const tabs = [
  { to: paths.about, label: '서비스 소개' },
  { to: paths.terms, label: '이용약관' },
  { to: paths.privacy, label: '개인정보처리방침' },
];

function Block({ block }: { block: LegalBlock }) {
  if (block.type === 'list') {
    return (
      <ul className="list-disc pl-5 text-sm leading-[1.85]">
        {block.items.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
    );
  }

  if (block.type === 'table') {
    return (
      <table className="my-2.5 w-full border-collapse text-[13px]">
        <thead>
          <tr>
            {block.head.map((cell, index) => (
              <th
                key={cell}
                className={`border border-rule bg-band px-3 py-[9px] text-left font-semibold ${index === 0 ? 'w-[90px]' : ''}`}
              >
                {cell}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {block.rows.map((row) => (
            <tr key={row[0]}>
              {row.map((cell) => (
                <td key={cell} className="border border-rule px-3 py-[9px] align-top">
                  {cell}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    );
  }

  return <p className="text-sm leading-[1.85]">{block.text}</p>;
}

export default function LegalPage({ document }: { document: LegalDocument }) {
  return (
    <section className="mx-auto w-full max-w-[720px] px-6 pt-12 pb-[72px]">
      <nav aria-label="약관 종류" className="mb-[30px] flex flex-wrap border-b border-rule">
        {tabs.map((tab) => (
          <NavLink
            key={tab.to}
            to={tab.to}
            className={({ isActive }) =>
              `-mb-px border-b-2 px-5 py-3 text-[15px] max-[420px]:px-3 max-[420px]:text-sm font-semibold ${isActive ? 'border-green text-green' : 'border-transparent text-sub'}`
            }
          >
            {tab.label}
          </NavLink>
        ))}
      </nav>
      <h1 className="font-heading text-[28px] leading-[1.1] font-extrabold tracking-[-0.04em]">
        {document.title}
      </h1>
      {document.dated && <p className="mt-2 text-[12.5px] text-sub">시행일 {site.effectiveDate}</p>}
      {document.sections.map((section) => (
        <div key={section.title}>
          <h2 className="mt-[30px] mb-2 text-base font-bold">{section.title}</h2>
          {section.blocks.map((block, index) => (
            <Block key={index} block={block} />
          ))}
        </div>
      ))}
    </section>
  );
}
