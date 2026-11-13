import type { HTMLInputTypeAttribute, ReactNode } from 'react';
import type { UseFormRegisterReturn } from 'react-hook-form';
import { inputStyle } from '@/components/ui/inputStyle';

// 라벨, 입력칸, 안내 문구 묶음

export function Field({
  id,
  label,
  hint,
  error,
  children,
}: {
  id: string;
  label: string;
  hint?: string;
  error?: string;
  children: ReactNode;
}) {
  return (
    <div className="mb-3.5">
      <label htmlFor={id} className="mb-1.5 block text-[13px] font-semibold">
        {label}
      </label>
      {children}
      {error ? (
        <p className="mt-1.5 text-xs text-danger">{error}</p>
      ) : (
        hint && <p className="mt-1.5 text-xs text-sub">{hint}</p>
      )}
    </div>
  );
}

type TextFieldProps = {
  id: string;
  label: string;
  hint?: string;
  error?: string;
  type?: HTMLInputTypeAttribute;
  placeholder?: string;
  autoComplete?: string;
  inputMode?: 'numeric' | 'decimal';
  registration: UseFormRegisterReturn;
};

export default function TextField({
  id,
  label,
  hint,
  error,
  type = 'text',
  placeholder,
  autoComplete,
  inputMode,
  registration,
}: TextFieldProps) {
  return (
    <Field id={id} label={label} hint={hint} error={error}>
      <input
        id={id}
        type={type}
        placeholder={placeholder}
        autoComplete={autoComplete}
        inputMode={inputMode}
        aria-invalid={error ? true : undefined}
        className={inputStyle}
        {...registration}
      />
    </Field>
  );
}
