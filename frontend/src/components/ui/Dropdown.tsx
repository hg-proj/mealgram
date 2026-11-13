import { useEffect, useId, useRef, useState, type KeyboardEvent } from 'react';

// 설명이 붙는 선택 목록

export type DropdownOption = { value: string; label: string; description?: string };

type Props = {
  id: string;
  value: string;
  options: DropdownOption[];
  placeholder: string;
  onChange: (value: string) => void;
  invalid?: boolean;
  ariaLabel?: string;
};

export default function Dropdown({
  id,
  value,
  options,
  placeholder,
  onChange,
  invalid,
  ariaLabel,
}: Props) {
  const [open, setOpen] = useState(false);
  const [active, setActive] = useState(0);
  const root = useRef<HTMLDivElement>(null);
  const listId = useId();
  const selected = options.find((option) => option.value === value);

  useEffect(() => {
    if (!open) {
      return;
    }
    const close = (e: MouseEvent) => {
      if (!root.current?.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', close);

    return () => document.removeEventListener('mousedown', close);
  }, [open]);

  const toggle = () => {
    setActive(
      Math.max(
        0,
        options.findIndex((option) => option.value === value),
      ),
    );
    setOpen((current) => !current);
  };

  const choose = (next: string) => {
    onChange(next);
    setOpen(false);
  };

  const onKeyDown = (e: KeyboardEvent) => {
    if (e.key === 'Escape') {
      setOpen(false);
    } else if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
      e.preventDefault();
      if (!open) {
        toggle();
      } else {
        const step = e.key === 'ArrowDown' ? 1 : -1;
        setActive((current) => (current + step + options.length) % options.length);
      }
    } else if ((e.key === 'Enter' || e.key === ' ') && open) {
      e.preventDefault();
      choose(options[active].value);
    }
  };

  return (
    <div ref={root} className="relative" onKeyDown={onKeyDown}>
      <button
        id={id}
        type="button"
        aria-haspopup="listbox"
        aria-expanded={open}
        aria-controls={listId}
        aria-label={ariaLabel}
        aria-invalid={invalid ? true : undefined}
        onClick={toggle}
        className={`flex h-[56px] w-full items-center justify-between border bg-white px-3.5 text-[15px] select-none ${
          open ? 'rounded-t-[3px]' : 'rounded-[3px]'
        } ${invalid ? 'border-danger' : open ? 'border-green' : 'border-rule'} ${selected ? 'text-ink' : 'text-sub'}`}
      >
        <span>{selected ? selected.label : placeholder}</span>
        <svg
          viewBox="0 0 24 24"
          aria-hidden="true"
          className={`size-3.5 fill-none stroke-2 transition-transform ${open ? 'rotate-180 stroke-green' : 'stroke-sub'}`}
        >
          <path d="M5 9l7 7 7-7" />
        </svg>
      </button>
      {open && (
        <ul
          id={listId}
          role="listbox"
          aria-label={ariaLabel}
          aria-labelledby={ariaLabel ? undefined : id}
          className="max-h-60 overflow-y-auto [scrollbar-width:none] [&::-webkit-scrollbar]:hidden absolute top-full right-0 left-0 z-10 -mt-px rounded-b-[3px] border border-t-0 border-green bg-white py-1 shadow-[0_8px_20px_rgb(29_40_35_/_0.1)] select-none"
        >
          {options.map((option, index) => {
            const isSelected = option.value === value;

            return (
              <li
                key={option.value}
                role="option"
                aria-selected={isSelected}
                onMouseEnter={() => setActive(index)}
                onClick={() => choose(option.value)}
                className={`cursor-pointer px-3.5 py-2.5 ${
                  isSelected ? 'bg-green-soft' : index === active ? 'bg-green-pale' : ''
                }`}
              >
                <b
                  className={`block text-sm ${option.description ? 'font-semibold' : 'font-normal'} ${isSelected ? 'text-green-d' : ''}`}
                >
                  {option.label}
                </b>
                {option.description && (
                  <small className="text-xs text-sub">{option.description}</small>
                )}
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
