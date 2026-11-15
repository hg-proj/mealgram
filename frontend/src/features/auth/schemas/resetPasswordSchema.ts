import { z } from 'zod';
import { messages } from '@/config/messages';
import { isValidPassword } from '@/features/auth/schemas/signupSchema';

// 비밀번호 재설정 입력 검증

export const verifyEmailSchema = z.object({
  email: z
    .string()
    .min(1, messages.validation.emailRequired)
    .pipe(z.email(messages.validation.email)),
  code: z
    .string()
    .min(1, messages.validation.resetCodeRequired)
    .refine((v) => /^[0-9]{6}$/.test(v), messages.validation.resetCode),
});

export type VerifyEmailInput = z.infer<typeof verifyEmailSchema>;

export const newPasswordSchema = z
  .object({
    newPassword: z
      .string()
      .min(1, messages.validation.passwordRequired)
      .refine((v) => !/\s/.test(v), messages.validation.passwordSpace)
      .refine((v) => v.length <= 20, messages.validation.passwordMax)
      .refine(isValidPassword, messages.validation.password),
    newPasswordConfirm: z.string().min(1, messages.validation.passwordConfirmRequired),
  })
  .refine((v) => v.newPassword === v.newPasswordConfirm, {
    path: ['newPasswordConfirm'],
    message: messages.validation.passwordMismatch,
  });

export type NewPasswordInput = z.infer<typeof newPasswordSchema>;
