// 약관 문서 구조

export type LegalBlock =
  | { type: 'text'; text: string }
  | { type: 'list'; items: readonly string[] }
  | {
      type: 'table';
      head: readonly [string, string];
      rows: readonly (readonly [string, string])[];
    };

export type LegalSection = {
  title: string;
  blocks: readonly LegalBlock[];
};

export type LegalDocument = {
  title: string;
  dated?: boolean;
  sections: readonly LegalSection[];
};
