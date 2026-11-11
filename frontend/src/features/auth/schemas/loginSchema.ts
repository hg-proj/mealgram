import { z } from 'zod';
import { messages } from '@/config/messages';

// 로그인 입력 검증

const required = (message: string) => z.string().refine((value) => value.trim() !== '', message);

export const loginSchema = z.object({
  loginId: required(messages.validation.loginIdRequired),
  password: required(messages.validation.passwordRequired),
});

export type LoginInput = z.infer<typeof loginSchema>;
