import { z } from 'zod';
import { messages } from '@/config/messages';

// 회원가입 입력 검증, 백엔드 SignupRequest 규칙과 동일

const isLetter = (c: string) => /[A-Za-z]/.test(c);
const isDigit = (c: string) => /[0-9]/.test(c);

export function isValidPassword(password: string): boolean {
  if (password.length > 20 || /\s/.test(password)) {
    return false;
  }

  const chars = [...password];
  const types =
    Number(chars.some(isLetter)) +
    Number(chars.some(isDigit)) +
    Number(
      chars.some((c) => {
        const code = c.charCodeAt(0);
        return code > 32 && code < 127 && !isLetter(c) && !isDigit(c);
      }),
    );

  return types >= 3 ? password.length >= 8 : types === 2 && password.length >= 10;
}

const inRange = (min: number, max: number) => (value: string) => {
  if (value === '') {
    return true;
  }
  const n = Number(value);

  return Number.isFinite(n) && n >= min && n <= max;
};

const MIN_AGE = 3;
const MAX_AGE = 120;

// 생년월일 문제 종류, 문제가 없으면 null
export function birthDateIssue(
  year: string,
  month: string,
  day: string,
  today = new Date(),
): 'birthIncomplete' | 'birthInvalid' | 'birthRange' | null {
  const filled = [year, month, day].filter((part) => part !== '').length;

  if (filled === 0) {
    return null;
  }
  if (filled < 3) {
    return 'birthIncomplete';
  }

  const y = Number(year);
  const m = Number(month);
  const d = Number(day);
  const date = new Date(y, m - 1, d);

  if (date.getFullYear() !== y || date.getMonth() !== m - 1 || date.getDate() !== d) {
    return 'birthInvalid';
  }

  let age = today.getFullYear() - y;
  if (today.getMonth() < m - 1 || (today.getMonth() === m - 1 && today.getDate() < d)) {
    age -= 1;
  }

  return age < MIN_AGE || age > MAX_AGE ? 'birthRange' : null;
}

export const signupSchema = z
  .object({
    nickname: z
      .string()
      .min(1, messages.validation.nicknameRequired)
      .refine((v) => v.length >= 2 && v.length <= 10, messages.validation.nickname),
    loginId: z
      .string()
      .min(1, messages.validation.loginIdRequired)
      .refine((v) => /^[a-z0-9]{6,20}$/.test(v), messages.validation.loginId),
    password: z
      .string()
      .min(1, messages.validation.passwordRequired)
      .refine((v) => !/\s/.test(v), messages.validation.passwordSpace)
      .refine((v) => v.length <= 20, messages.validation.passwordMax)
      .refine(isValidPassword, messages.validation.password),
    passwordConfirm: z.string().min(1, messages.validation.passwordConfirmRequired),
    email: z
      .string()
      .min(1, messages.validation.emailRequired)
      .pipe(z.email(messages.validation.email)),
    agreeTerms: z.boolean().refine((v) => v, messages.validation.agreeTerms),
    agreePrivacy: z.boolean().refine((v) => v, messages.validation.agreePrivacy),
    birthYear: z.string(),
    birthMonth: z.string(),
    birthDay: z.string(),
    gender: z.enum(['', 'MALE', 'FEMALE']),
    height: z.string().refine(inRange(50, 250), messages.validation.height),
    weight: z.string().refine(inRange(10, 300), messages.validation.weight),
    activityLevel: z.enum(['', 'LOW', 'LIGHT', 'MODERATE', 'HIGH']),
  })
  .refine((v) => v.password === v.passwordConfirm, {
    path: ['passwordConfirm'],
    message: messages.validation.passwordMismatch,
  })
  .superRefine((v, ctx) => {
    const issue = birthDateIssue(v.birthYear, v.birthMonth, v.birthDay);

    if (issue) {
      ctx.addIssue({ code: 'custom', path: ['birthYear'], message: messages.validation[issue] });
    }
  });

export type SignupInput = z.infer<typeof signupSchema>;

export const signupDefaults: SignupInput = {
  nickname: '',
  loginId: '',
  password: '',
  passwordConfirm: '',
  email: '',
  agreeTerms: false,
  agreePrivacy: false,
  birthYear: '',
  birthMonth: '',
  birthDay: '',
  gender: '',
  height: '',
  weight: '',
  activityLevel: '',
};
