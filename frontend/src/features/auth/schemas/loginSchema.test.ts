import { describe, expect, it } from 'vitest';
import { loginSchema } from '@/features/auth/schemas/loginSchema';

describe('loginSchema', () => {
  it('아이디와 비밀번호가 있으면 통과한다.', () => {
    expect(loginSchema.safeParse({ loginId: 'tester', password: 'pw' }).success).toBe(true);
  });

  it('빈 값이면 항목별 안내 문구를 돌려준다.', () => {
    const result = loginSchema.safeParse({ loginId: '', password: '' });

    expect(result.success).toBe(false);
    expect(result.error?.issues.map((issue) => issue.message)).toEqual([
      '아이디를 입력해 주세요.',
      '비밀번호를 입력해 주세요.',
    ]);
  });

  it('공백만 입력하면 통과하지 못한다.', () => {
    expect(loginSchema.safeParse({ loginId: '  ', password: ' ' }).success).toBe(false);
  });
});
