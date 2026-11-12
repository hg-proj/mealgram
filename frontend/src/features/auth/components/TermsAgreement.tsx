import type { UseFormRegister } from 'react-hook-form';
import { Link } from 'react-router';
import { paths } from '@/config/paths';
import type { SignupInput } from '@/features/auth/schemas/signupSchema';

// 약관 동의 묶음

type Props = {
  register: UseFormRegister<SignupInput>;
  allChecked: boolean;
  onToggleAll: (checked: boolean) => void;
  error?: string;
};

const items = [
  { name: 'agreeTerms', label: '이용약관', to: paths.terms },
  { name: 'agreePrivacy', label: '개인정보 수집 및 이용', to: paths.privacy },
] as const;

export default function TermsAgreement({ register, allChecked, onToggleAll, error }: Props) {
  return (
    <fieldset
      className={`mt-1.5 mb-[22px] rounded-[3px] border p-4 ${error ? 'border-danger' : 'border-rule'}`}
    >
      <legend className="sr-only">약관 동의</legend>
      <label className="mb-3 flex items-center gap-2 border-b border-line pb-3 text-[13.5px] font-bold">
        <input
          type="checkbox"
          checked={allChecked}
          onChange={(e) => onToggleAll(e.target.checked)}
          className="size-4 accent-green"
        />
        전체 약관동의
      </label>
      {items.map((item) => (
        <div
          key={item.name}
          className="mt-2 flex items-center justify-between text-[13px] first-of-type:mt-0"
        >
          <label className="flex items-center gap-2">
            <input type="checkbox" className="size-4 accent-green" {...register(item.name)} />
            <span>
              {item.label} <span className="text-danger">(필수)</span>
            </span>
          </label>
          <Link to={item.to} className="text-xs text-sub underline">
            보기
          </Link>
        </div>
      ))}
      {error && <p className="mt-2.5 text-xs text-danger">{error}</p>}
    </fieldset>
  );
}
